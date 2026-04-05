package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.model.dto.request.CartAddRequest;
import xyz.yaungyue.secondhand.model.dto.request.StockCheckRequest;
import xyz.yaungyue.secondhand.model.dto.request.StockLockRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.CartVO;
import xyz.yaungyue.secondhand.model.dto.response.StockCheckResult;
import xyz.yaungyue.secondhand.model.dto.response.StockLockResult;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.CartService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.util.List;

/**
 * 购物车Controller
 *
 * @author yaung
 * @date 2026-02-26
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "购物车管理", description = "购物车相关接口")
public class CartController {

    private final CartService cartService;

    /**
     * 获取购物车商品总数量
     * @return 商品总数量
     */
    @GetMapping("/count")
    @SaCheckPermission(value = "user:cart:*", type = "user")
    @Operation(summary = "获取购物车商品总数量")
    public ApiResponse<Integer> getCartCount() {
        User currentUser = SaTokenUtil.getCurrentUser();
        Integer count = cartService.getCartCount(currentUser.getId());
        return ApiResponse.success(count);
    }
    /**
     * 添加商品到购物车
     * @param request 购物车添加请求
     * @return 购物车信息
     */
    @PostMapping
    @SaCheckPermission(value = "user:cart:*", type = "user")
    @Operation(summary = "添加商品到购物车")
    public ApiResponse<CartVO> addToCart(@RequestBody @Valid CartAddRequest request) {
        User currentUser = SaTokenUtil.getCurrentUser();
        CartVO cartVO = cartService.addToCart(request, currentUser.getId());
        return ApiResponse.success(cartVO);
    }

    /**
     * 获取购物车列表
     * @return 购物车列表
     */
    @GetMapping
    @SaCheckPermission(value = "user:cart:*", type = "user")
    @Operation(summary = "获取购物车列表")
    public ApiResponse<List<CartVO>> getCartList() {
        User currentUser = SaTokenUtil.getCurrentUser();
        List<CartVO> list = cartService.getCartList(currentUser.getId());
        return ApiResponse.success(list);
    }

    /**
     * 修改购物车商品数量
     * @param id 购物车 ID
     * @param quantity 商品数量
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @SaCheckPermission(value = "user:cart:*", type = "user")
    @Operation(summary = "修改购物车商品数量")
    public ApiResponse<Void> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        User currentUser = SaTokenUtil.getCurrentUser();
        cartService.updateQuantity(id, quantity, currentUser.getId());
        return ApiResponse.success();
    }

    /**
     * 删除购物车商品
     * @param id 购物车 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = "user:cart:*", type = "user")
    @Operation(summary = "删除购物车商品")
    public ApiResponse<Void> removeFromCart(@PathVariable Long id) {
        User currentUser = SaTokenUtil.getCurrentUser();
        cartService.removeFromCart(id, currentUser.getId());
        return ApiResponse.success();
    }

    /**
     * 清空购物车
     * @return 操作结果
     */
    @DeleteMapping
    @SaCheckPermission(value = "user:cart:*", type = "user")
    @Operation(summary = "清空购物车")
    public ApiResponse<Void> clearCart() {
        User currentUser = SaTokenUtil.getCurrentUser();
        cartService.clearCart(currentUser.getId());
        return ApiResponse.success();
    }
}
