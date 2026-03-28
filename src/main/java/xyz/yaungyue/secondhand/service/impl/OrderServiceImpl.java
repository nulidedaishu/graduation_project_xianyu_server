package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.constant.OrderStatus;
import xyz.yaungyue.secondhand.constant.ProductStatus;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.exception.ErrorCode;
import xyz.yaungyue.secondhand.mapper.OrderItemMapper;
import xyz.yaungyue.secondhand.mapper.OrderMapper;
import xyz.yaungyue.secondhand.model.dto.request.OrderCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.OrderItemRequest;
import xyz.yaungyue.secondhand.model.dto.response.CartVO;
import xyz.yaungyue.secondhand.model.dto.response.OrderItemVO;
import xyz.yaungyue.secondhand.model.dto.response.OrderVO;
import xyz.yaungyue.secondhand.model.entity.Order;
import xyz.yaungyue.secondhand.model.entity.OrderItem;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.CartService;
import xyz.yaungyue.secondhand.service.OrderDelayMessageService;
import xyz.yaungyue.secondhand.service.OrderService;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.util.OrderSnUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单Service实现
 *
 * @author yaung
 * @date 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;
    private final CartService cartService;
    private final UserService userService;
    private final OrderDelayMessageService orderDelayMessageService;

    // 状态流转图定义
    private static final Map<Integer, List<Integer>> STATUS_TRANSITION_MAP = Map.of(
            OrderStatus.PENDING_PAYMENT, List.of(
                    OrderStatus.CANCELLED,
                    OrderStatus.CLOSED,
                    OrderStatus.PENDING_SHIPMENT
            ),
            OrderStatus.PENDING_SHIPMENT, List.of(OrderStatus.PENDING_RECEIPT),
            OrderStatus.PENDING_RECEIPT, List.of(OrderStatus.PENDING_REVIEW),
            OrderStatus.PENDING_REVIEW, List.of(OrderStatus.COMPLETED)
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateRequest request, Long userId) {
        log.info("开始创建订单，userId={}", userId);

        // 1. 获取商品列表（从购物车或直接从请求）
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        List<Long> cartIds = request.getCartIds();

        if (cartIds != null && !cartIds.isEmpty()) {
            // 从购物车创建订单
            List<CartVO> cartVOs = cartService.getCartByIds(cartIds, userId);
            if (cartVOs.size() != cartIds.size()) {
                throw new BusinessException(ErrorCode.PARAM_VALIDATE_FAILED.getCode(), "部分购物车商品不存在或无权访问");
            }

            for (CartVO cart : cartVOs) {
                OrderItemRequest item = new OrderItemRequest();
                item.setProductId(cart.getProductId());
                item.setQuantity(cart.getQuantity());
                itemRequests.add(item);
            }
        } else if (request.getItems() != null && !request.getItems().isEmpty()) {
            // 直接购买
            itemRequests = request.getItems();
        } else {
            throw new BusinessException(ErrorCode.PARAM_VALIDATE_FAILED.getCode(), "购物车ID列表或商品列表不能同时为空");
        }

        // 2. 验证商品并锁定库存
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productService.getById(itemRequest.getProductId());

            // 验证商品状态
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (product.getStatus() != ProductStatus.APPROVED) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT.getCode(),
                        "商品【" + product.getTitle() + "】已下架或不可用");
            }

            // 验证库存并锁定
            boolean locked = lockStock(product.getId(), itemRequest.getQuantity());
            if (!locked) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT.getCode(),
                        "商品【" + product.getTitle() + "】库存不足");
            }

            // 构建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getTitle());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSellerId(product.getUserId());

            // 查询卖家昵称
            User seller = userService.getById(product.getUserId());
            if (seller != null) {
                orderItem.setSellerName(seller.getNickname());
            }

            orderItems.add(orderItem);

            // 计算总金额
            BigDecimal itemAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);
        }

        // 3. 创建订单
        Order order = new Order();
        order.setOrderSn(OrderSnUtil.generateOrderSn());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30)); // 30分钟支付超时
        order.setRemark(request.getRemark());
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);

        // 4. 保存订单项
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 5. 清空已购买的购物车商品
        if (cartIds != null && !cartIds.isEmpty()) {
            cartService.removeByIds(cartIds, userId);
        }

        // 6. 发送 RabbitMQ 延时消息（用于超时自动关闭）
        try {
            orderDelayMessageService.sendOrderDelayMessage(order.getId());
        } catch (Exception e) {
            log.error("发送订单延时消息失败，orderId={}", order.getId(), e);
            // 降级：依赖定时任务兜底（保留原定时任务作为备份）
        }

        log.info("订单创建成功，orderId={}, orderSn={}, userId={}, totalAmount={}",
                order.getId(), order.getOrderSn(), userId, totalAmount);

        return convertToVO(order);
    }

    /**
     * 锁定库存
     *
     * @param productId 商品 ID
     * @param quantity  数量
     * @return 是否成功
     */
    private boolean lockStock(Long productId, Integer quantity) {
        // 调用 ProductService 的锁定库存方法
        return productService.lockStock(productId, quantity);
    }
    
    /**
     * 释放库存
     *
     * @param productId 商品 ID
     * @param quantity  数量
     */
    private void releaseStock(Long productId, Integer quantity) {
        // 调用 ProductService 的释放库存方法
        productService.releaseStock(productId, quantity);
    }
    
    /**
     * 确认扣减库存（支付成功后）
     *
     * @param productId 商品 ID
     * @param quantity  数量
     */
    private void confirmDeductStock(Long productId, Integer quantity) {
        // 调用 ProductService 的确认扣减库存方法
        productService.confirmDeductStock(productId, quantity);
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 权限校验：买家或卖家可以查看
        boolean isBuyer = order.getUserId().equals(userId);
        boolean isSeller = isOrderSeller(orderId, userId);

        if (!isBuyer && !isSeller) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权查看此订单");
        }

        return convertToVO(order);
    }

    /**
     * 判断用户是否为订单的卖家
     */
    private boolean isOrderSeller(Long orderId, Long userId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId)
                .eq(OrderItem::getSellerId, userId)
                .last("LIMIT 1");
        return orderItemMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<OrderVO> getMyOrders(Long userId, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<OrderVO> getSoldOrders(Long sellerId, Integer status) {
        // 通过订单项关联查询卖家卖出的订单
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getSellerId, sellerId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        if (items.isEmpty()) {
            return List.of();
        }

        // 获取订单ID列表
        List<Long> orderIds = items.stream()
                .map(OrderItem::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        // 查询订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(Order::getId, orderIds);
        if (status != null) {
            orderWrapper.eq(Order::getStatus, status);
        }
        orderWrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(orderWrapper);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 只能取消自己的订单
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权取消此订单");
        }

        // 只能取消待付款的订单
        if (!validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR.getCode(),
                    "订单状态不允许取消，当前状态：" + OrderStatus.getDescription(order.getStatus()));
        }

        // 释放库存
        releaseOrderStock(orderId);

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        order.setRemark("买家取消订单");
        orderMapper.updateById(order);

        log.info("订单已取消，orderId={}, userId={}", orderId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(Long orderId, Integer payType) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("支付回调：订单不存在，orderId={}", orderId);
            return;
        }

        // 幂等性处理：已支付直接返回
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("支付回调：订单状态不是待付款，orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        // 确认扣减库存
        confirmDeductOrderStock(orderId);

        // 更新订单状态
        order.setStatus(OrderStatus.PENDING_SHIPMENT);
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 取消延时消息（实际为消费时忽略）
        orderDelayMessageService.cancelOrderDelayMessage(orderId);

        log.info("订单支付成功，orderId={}, payType={}", orderId, payType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, Long sellerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 验证是否为订单的卖家
        if (!isOrderSeller(orderId, sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权操作此订单");
        }

        // 验证状态流转
        if (!validateStatusTransition(order.getStatus(), OrderStatus.PENDING_RECEIPT)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR.getCode(),
                    "订单状态不允许发货，当前状态：" + OrderStatus.getDescription(order.getStatus()));
        }

        order.setStatus(OrderStatus.PENDING_RECEIPT);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单已发货，orderId={}, sellerId={}", orderId, sellerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 只能确认自己的订单
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权操作此订单");
        }

        // 验证状态流转
        if (!validateStatusTransition(order.getStatus(), OrderStatus.PENDING_REVIEW)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR.getCode(),
                    "订单状态不允许确认收货，当前状态：" + OrderStatus.getDescription(order.getStatus()));
        }

        order.setStatus(OrderStatus.PENDING_REVIEW);
        order.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单已确认收货，orderId={}, userId={}", orderId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("关闭订单：订单不存在，orderId={}", orderId);
            return;
        }

        // 幂等性处理
        if (order.getStatus() == OrderStatus.CLOSED) {
            log.warn("关闭订单：订单已关闭，orderId={}", orderId);
            return;
        }

        // 验证状态流转
        if (!validateStatusTransition(order.getStatus(), OrderStatus.CLOSED)) {
            log.warn("关闭订单：订单状态不允许关闭，orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        // 释放库存
        releaseOrderStock(orderId);

        // 更新订单状态
        order.setStatus(OrderStatus.CLOSED);
        order.setRemark(reason);
        order.setCloseTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单已关闭，orderId={}, reason={}", orderId, reason);
    }

    /**
     * 释放订单库存
     */
    private void releaseOrderStock(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        for (OrderItem item : items) {
            releaseStock(item.getProductId(), item.getQuantity());
        }
    }

    /**
     * 确认扣减订单库存
     */
    private void confirmDeductOrderStock(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        for (OrderItem item : items) {
            confirmDeductStock(item.getProductId(), item.getQuantity());
        }
    }

    @Override
    public boolean validateStatusTransition(Integer currentStatus, Integer newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }
        List<Integer> allowed = STATUS_TRANSITION_MAP.get(currentStatus);
        return allowed != null && allowed.contains(newStatus);
    }

    @Override
    public Map<Integer, List<Integer>> getStatusTransitionMap() {
        return Collections.unmodifiableMap(STATUS_TRANSITION_MAP);
    }

    /**
     * 转换为VO
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderSn(order.getOrderSn());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(OrderStatus.getDescription(order.getStatus()));
        vo.setPayType(order.getPayType());
        vo.setPayTime(order.getPayTime());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setReceiveTime(order.getReceiveTime());
        vo.setExpireTime(order.getExpireTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());

        // 查询订单项
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setProductImage(item.getProductImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSellerId(item.getSellerId());
            itemVO.setSellerName(item.getSellerName());
            return itemVO;
        }).collect(Collectors.toList());

        vo.setItems(itemVOs);

        return vo;
    }
}
