package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.exception.ErrorCode;
import xyz.yaungyue.secondhand.mapper.CartMapper;
import xyz.yaungyue.secondhand.model.dto.request.CartAddRequest;
import xyz.yaungyue.secondhand.model.dto.request.StockCheckRequest;
import xyz.yaungyue.secondhand.model.dto.request.StockLockRequest;
import xyz.yaungyue.secondhand.model.dto.response.CartVO;
import xyz.yaungyue.secondhand.model.dto.response.StockCheckResult;
import xyz.yaungyue.secondhand.model.dto.response.StockLockResult;
import xyz.yaungyue.secondhand.model.entity.Cart;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.service.CartService;
import xyz.yaungyue.secondhand.service.ProductService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车Service实现
 *
 * @author yaung
 * @date 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final CartMapper cartMapper;
    private final ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartVO addToCart(CartAddRequest request, Long userId) {
        // 1. 验证商品是否存在且上架
        Product product = productService.getById(request.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 2. 检查是否已经在购物车中
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, request.getProductId());
        Cart existingCart = cartMapper.selectOne(wrapper);

        if (existingCart != null) {
            // 更新数量
            existingCart.setQuantity(existingCart.getQuantity() + request.getQuantity());
            existingCart.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existingCart);
            log.info("更新购物车数量，cartId={}, userId={}, quantity={}",
                    existingCart.getId(), userId, existingCart.getQuantity());
            return convertToVO(existingCart);
        }

        // 3. 新增购物车记录
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(request.getProductId());
        cart.setQuantity(request.getQuantity());
        cart.setCreateTime(LocalDateTime.now());
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.insert(cart);

        log.info("添加商品到购物车，cartId={}, userId={}, productId={}",
                cart.getId(), userId, request.getProductId());

        return convertToVO(cart);
    }

    @Override
    public List<CartVO> getCartList(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime);

        List<Cart> carts = cartMapper.selectList(wrapper);
        return carts.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long cartId, Integer quantity, Long userId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "购物车记录不存在");
        }

        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权操作此购物车");
        }

        cart.setQuantity(quantity);
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);

        log.info("更新购物车数量，cartId={}, userId={}, quantity={}", cartId, userId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromCart(Long cartId, Long userId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            return; // 幂等删除
        }

        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权操作此购物车");
        }

        cartMapper.deleteById(cartId);
        log.info("删除购物车商品，cartId={}, userId={}", cartId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
        log.info("清空购物车，userId={}", userId);
    }

    @Override
    public List<CartVO> getCartByIds(List<Long> cartIds, Long userId) {
        if (cartIds == null || cartIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Cart::getId, cartIds)
                .eq(Cart::getUserId, userId);

        List<Cart> carts = cartMapper.selectList(wrapper);
        return carts.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> cartIds, Long userId) {
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Cart::getId, cartIds)
                .eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);

        log.info("批量删除购物车商品，cartIds={}, userId={}", cartIds, userId);
    }

    @Override
    public Integer getCartCount(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        
        // 查询该用户的所有购物车记录
        List<Cart> carts = cartMapper.selectList(wrapper);
        
        // 计算总数量
        int totalCount = carts.stream()
                .mapToInt(Cart::getQuantity)
                .sum();
        
        log.info("获取购物车数量，userId={}, totalCount={}", userId, totalCount);
        return totalCount;
    }



    /**
     * 转换为VO
     */
    private CartVO convertToVO(Cart cart) {
        CartVO vo = new CartVO();
        vo.setId(cart.getId());
        vo.setProductId(cart.getProductId());
        vo.setQuantity(cart.getQuantity());
        vo.setCreateTime(cart.getCreateTime());
        vo.setUpdateTime(cart.getUpdateTime());

        // 查询商品信息
        Product product = productService.getById(cart.getProductId());
        if (product != null) {
            vo.setProductName(product.getTitle());
            vo.setProductImage(product.getMainImage());
            vo.setPrice(product.getPrice());
            vo.setStock(product.getStock());
        }

        return vo;
    }
}
