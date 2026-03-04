package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.dto.request.CartAddRequest;
import xyz.yaungyue.secondhand.model.dto.request.StockCheckRequest;
import xyz.yaungyue.secondhand.model.dto.request.StockLockRequest;
import xyz.yaungyue.secondhand.model.dto.response.CartVO;
import xyz.yaungyue.secondhand.model.dto.response.StockCheckResult;
import xyz.yaungyue.secondhand.model.dto.response.StockLockResult;
import xyz.yaungyue.secondhand.model.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 购物车Service
 *
 * @author yaung
 * @date 2026-02-26
 */
public interface CartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     *
     * @param request 添加请求
     * @param userId  用户ID
     * @return 购物车VO
     */
    CartVO addToCart(CartAddRequest request, Long userId);

    /**
     * 获取用户的购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<CartVO> getCartList(Long userId);

    /**
     * 更新购物车商品数量
     *
     * @param cartId   购物车ID
     * @param quantity 数量
     * @param userId   用户ID
     */
    void updateQuantity(Long cartId, Integer quantity, Long userId);

    /**
     * 删除购物车商品
     *
     * @param cartId 购物车ID
     * @param userId 用户ID
     */
    void removeFromCart(Long cartId, Long userId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 根据ID列表获取购物车商品
     *
     * @param cartIds 购物车ID列表
     * @param userId  用户ID
     * @return 购物车列表
     */
    List<CartVO> getCartByIds(List<Long> cartIds, Long userId);

    /**
     * 删除指定 ID 列表的购物车商品
     *
     * @param cartIds 购物车 ID 列表
     * @param userId  用户 ID
     */
    void removeByIds(List<Long> cartIds, Long userId);
    
    /**
     * 获取用户购物车商品总数量
     *
     * @param userId 用户 ID
     * @return 购物车商品总数量
     */
    Integer getCartCount(Long userId);
}
