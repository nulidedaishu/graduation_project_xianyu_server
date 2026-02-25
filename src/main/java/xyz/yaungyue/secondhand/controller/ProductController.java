package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.constant.ProductStatus;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.request.ProductCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductQueryRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductReviewRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.service.impl.ProductServiceImpl;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.util.List;

/**
 * 商品管理控制器
 *
 * 安全架构说明：
 * 1. Spring Security 负责认证（Authentication）- 验证用户是否登录
 *    - 在 SecurityConfig 中配置，公共接口无需登录，其他接口需要认证
 *
 * 2. Sa-Token 负责授权（Authorization）- 验证用户是否有权限执行操作
 *    - 使用 @SaCheckLogin 确保已登录
 *    - 使用 @SaCheckRole 检查角色
 *    - 使用 @SaCheckPermission 检查权限
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品的发布、查询、审核等接口")
public class ProductController {

    private final ProductServiceImpl productService;

    /**
     * 发布商品
     * 需要登录，普通用户即可发布
     */
    @PostMapping
    @SaCheckLogin(type = "user")
    @Operation(summary = "发布商品", description = "用户发布新商品，需要管理员审核")
    public ApiResponse<ProductVO> createProduct(@RequestBody @Valid ProductCreateRequest request) {
        Long userId = SaTokenUtil.getCurrentUserId();
        log.info("用户发布商品，用户ID: {}", userId);
        ProductVO product = productService.createProduct(request, userId);
        return ApiResponse.success(product);
    }

    /**
     * 获取商品列表（分页）
     * 公开接口，无需登录
     */
    @GetMapping
    @Operation(summary = "获取商品列表", description = "获取已上架的商品列表，支持分页")
    public ApiResponse<IPage<ProductVO>> getProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        IPage<ProductVO> products = productService.getApprovedProducts(page, size);
        return ApiResponse.success(products);
    }

    /**
     * 搜索商品
     * 公开接口，无需登录
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据关键词、分类等条件搜索商品")
    public ApiResponse<IPage<ProductVO>> searchProducts(ProductQueryRequest request) {
        IPage<ProductVO> products = productService.searchProducts(request);
        return ApiResponse.success(products);
    }

    /**
     * 获取商品详情
     * 公开接口，无需登录
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据商品ID获取详细信息")
    public ApiResponse<ProductVO> getProductById(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long id) {
        ProductVO product = productService.getProductById(id);
        return ApiResponse.success(product);
    }

    /**
     * 根据分类获取商品
     * 公开接口，无需登录
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类查询商品", description = "获取指定分类下的商品列表")
    public ApiResponse<IPage<ProductVO>> getProductsByCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long categoryId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        IPage<ProductVO> products = productService.getProductsByCategory(categoryId, page, size);
        return ApiResponse.success(products);
    }

    /**
     * 获取我发布的商品
     * 需要登录
     */
    @GetMapping("/my")
    @SaCheckLogin(type = "user")
    @Operation(summary = "我的商品", description = "获取当前登录用户发布的所有商品")
    public ApiResponse<List<ProductVO>> getMyProducts() {
        Long userId = SaTokenUtil.getCurrentUserId();
        List<ProductVO> products = productService.getProductsByUser(userId);
        return ApiResponse.success(products);
    }

    /**
     * 下架商品
     * 需要登录，且是商品所有者
     */
    @PostMapping("/{id}/offline")
    @SaCheckLogin(type = "user")
    @Operation(summary = "下架商品", description = "卖家下架已上架的商品")
    public ApiResponse<ProductVO> offlineProduct(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long id) {
        Long userId = SaTokenUtil.getCurrentUserId();
        ProductVO product = productService.offlineProduct(id, userId);
        return ApiResponse.success(product);
    }

    /**
     * 重新上架商品
     * 需要登录，且是商品所有者
     * 被驳回或已下架的商品重新提交审核
     */
    @PostMapping("/{id}/online")
    @SaCheckLogin(type = "user")
    @Operation(summary = "重新上架商品", description = "卖家重新提交审核（适用于已下架或被驳回的商品）")
    public ApiResponse<ProductVO> onlineProduct(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long id) {
        Long userId = SaTokenUtil.getCurrentUserId();
        ProductVO product = productService.onlineProduct(id, userId);
        return ApiResponse.success(product);
    }

    /**
     * 获取待审核商品列表（管理员）
     * 需要管理员角色
     */
    @GetMapping("/pending")
    @SaCheckRole(value = "admin", type = "admin")
    @Operation(summary = "待审核商品", description = "管理员获取待审核的商品列表")
    public ApiResponse<List<ProductVO>> getPendingProducts() {
        List<ProductVO> products = productService.getProductsByStatus(ProductStatus.PENDING);
        return ApiResponse.success(products);
    }

    /**
     * 审核商品（管理员）
     * 需要管理员角色
     */
    @PostMapping("/{id}/review")
    @SaCheckRole(value = "admin", type = "admin")
    @Operation(summary = "审核商品", description = "管理员审核商品（通过或驳回）")
    public ApiResponse<ProductVO> reviewProduct(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long id,
            @RequestBody @Valid ProductReviewRequest request) {
        // 验证路径参数和请求体中的商品ID一致
        if (!id.equals(request.productId())) {
            throw new BusinessException(400, "商品ID不一致");
        }

        Long adminId = SaTokenUtil.getCurrentUserId();
        ProductVO product = productService.reviewProduct(id, request, adminId);
        return ApiResponse.success(product);
    }
}
