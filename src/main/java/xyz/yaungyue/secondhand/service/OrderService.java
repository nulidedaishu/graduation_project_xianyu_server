package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.dto.request.OrderCreateRequest;
import xyz.yaungyue.secondhand.model.dto.response.OrderVO;
import xyz.yaungyue.secondhand.model.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 订单Service
 *
 * @author yaung
 * @date 2026-02-26
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     *
     * @param request 创建请求
     * @param userId  买家ID
     * @return 订单VO
     */
    OrderVO createOrder(OrderCreateRequest request, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param userId  用户ID（用于权限校验）
     * @return 订单VO
     */
    OrderVO getOrderDetail(Long orderId, Long userId);

    /**
     * 获取我的订单列表
     *
     * @param userId 用户ID
     * @param status 状态筛选（可选）
     * @return 订单列表
     */
    List<OrderVO> getMyOrders(Long userId, Integer status);

    /**
     * 获取我卖出的订单列表
     *
     * @param sellerId 卖家ID
     * @param status   状态筛选（可选）
     * @return 订单列表
     */
    List<OrderVO> getSoldOrders(Long sellerId, Integer status);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 支付成功回调
     *
     * @param orderId 订单ID
     * @param payType 支付方式
     */
    void paySuccess(Long orderId, Integer payType);

    /**
     * 卖家发货
     *
     * @param orderId    订单ID
     * @param sellerId   卖家ID
     */
    void shipOrder(Long orderId, Long sellerId);

    /**
     * 买家确认收货
     *
     * @param orderId 订单ID
     * @param userId  买家ID
     */
    void confirmReceive(Long orderId, Long userId);

    /**
     * 关闭超时未支付订单
     *
     * @param orderId 订单ID
     * @param reason  关闭原因
     */
    void closeOrder(Long orderId, String reason);

    /**
     * 验证订单状态流转是否合法
     *
     * @param currentStatus 当前状态
     * @param newStatus     新状态
     * @return 是否合法
     */
    boolean validateStatusTransition(Integer currentStatus, Integer newStatus);

    /**
     * 获取状态流转图
     *
     * @return 状态流转图
     */
    Map<Integer, List<Integer>> getStatusTransitionMap();
}
