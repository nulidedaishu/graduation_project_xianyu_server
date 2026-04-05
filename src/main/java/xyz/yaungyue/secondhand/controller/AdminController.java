package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.constant.ProductStatus;
import xyz.yaungyue.secondhand.constant.UserStatus;
import xyz.yaungyue.secondhand.model.dto.request.ProductReviewRequest;
import xyz.yaungyue.secondhand.model.dto.request.UserQueryRequest;
import xyz.yaungyue.secondhand.model.dto.request.UserStatusUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.DashboardStatisticsVO;
import xyz.yaungyue.secondhand.model.dto.response.PageResponse;
import xyz.yaungyue.secondhand.model.dto.response.ProductListVO;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.service.UserManageService;

import java.util.List;

/**
 * 后台管理控制器
 * <p>
 * 提供管理员使用的用户管理、商品管理等功能
 * 所有接口都需要管理员角色权限
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "后台管理", description = "管理员使用的用户管理、商品管理等接口")
public class AdminController {

    private final UserManageService userManageService;
    private final ProductService productService;

    // ==================== 用户管理接口 ====================

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/users")
    @SaCheckPermission(value = "admin:user:list", type = "admin")
    @Operation(summary = "获取用户列表", description = "分页查询用户列表，支持按关键字搜索和状态筛选")
    public ApiResponse<PageResponse<User>> getUserList(UserQueryRequest request) {
        log.info("管理员查询用户列表，操作人: {}", StpUtil.getLoginId());
        PageResponse<User> result = userManageService.getUserList(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    @SaCheckPermission(value = "admin:user:detail", type = "admin")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ApiResponse<User> getUserDetail(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        log.info("管理员查询用户详情，用户ID: {}, 操作人: {}", userId, StpUtil.getLoginId());
        User user = userManageService.getUserDetail(userId);
        return ApiResponse.success(user);
    }

    /**
     * 启用用户
     */
    @PostMapping("/users/{userId}/enable")
    @SaCheckPermission(value = "admin:user:enable", type = "admin")
    @Operation(summary = "启用用户", description = "启用被禁用的用户账号")
    public ApiResponse<Void> enableUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        log.info("管理员启用用户，用户ID: {}, 操作人: {}", userId, StpUtil.getLoginId());
        boolean success = userManageService.updateUserStatus(userId, UserStatus.ENABLED);
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "启用用户失败");
    }

    /**
     * 禁用用户
     */
    @PostMapping("/users/{userId}/disable")
    @SaCheckPermission(value = "admin:user:disable", type = "admin")
    @Operation(summary = "禁用用户", description = "禁用用户账号，禁用后用户将无法登录")
    public ApiResponse<Void> disableUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        log.info("管理员禁用用户，用户ID: {}, 操作人: {}", userId, StpUtil.getLoginId());
        boolean success = userManageService.updateUserStatus(userId, UserStatus.DISABLED);
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "禁用用户失败");
    }

    /**
     * 更新用户状态（通用接口）
     */
    @PutMapping("/users/{userId}/status")
    @SaCheckPermission(value = "admin:user:status", type = "admin")
    @Operation(summary = "更新用户状态", description = "更新用户状态（0-禁用，1-正常）")
    public ApiResponse<Void> updateUserStatus(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @RequestBody @Valid UserStatusUpdateRequest request) {
        log.info("管理员更新用户状态，用户ID: {}, 状态: {}, 操作人: {}",
                userId, request.getStatus(), StpUtil.getLoginId());
        boolean success = userManageService.updateUserStatus(userId, request.getStatus());
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "更新用户状态失败");
    }

    /**
     * 获取所有用户（不分页，用于导出等场景）
     */
    @GetMapping("/users/all")
    @SaCheckPermission(value = "admin:user:all", type = "admin")
    @Operation(summary = "获取所有用户", description = "获取所有用户列表，不分页")
    public ApiResponse<List<User>> getAllUsers() {
        log.info("管理员查询所有用户，操作人: {}", StpUtil.getLoginId());
        List<User> users = userManageService.getAllUsers();
        return ApiResponse.success(users);
    }

    // ==================== 商品管理接口 ====================

    /**
     * 获取商品列表（管理员视角，包含所有状态）
     */
    @GetMapping("/products")
    @SaCheckPermission(value = "admin:product:list", type = "admin")
    @Operation(summary = "获取商品列表", description = "管理员获取所有商品列表，支持按状态筛选和搜索")
    public ApiResponse<PageResponse<ProductListVO>> getProductList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "商品状态（0-待审核，1-已上架，2-审核驳回，3-已下架，4-已售出，5-已删除）")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键字（商品标题）") @RequestParam(required = false) String keyword) {
        log.info("管理员查询商品列表，状态: {}, 关键字: {}, 操作人: {}",
                status, keyword, StpUtil.getLoginId());

        var result = productService.getAllProductsForAdmin(page, size, status, keyword);
        PageResponse<ProductListVO> response = PageResponse.of(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
        return ApiResponse.success(response);
    }

    /**
     * 获取待审核商品列表
     */
    @GetMapping("/products/pending")
    @SaCheckPermission(value = "admin:product:pending", type = "admin")
    @Operation(summary = "获取待审核商品", description = "获取待审核的商品列表")
    public ApiResponse<PageResponse<ProductVO>> getPendingProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        log.info("管理员查询待审核商品，操作人: {}", StpUtil.getLoginId());
        var result = productService.getPendingProducts(page, size);
        PageResponse<ProductVO> response = PageResponse.of(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
        return ApiResponse.success(response);
    }

    /**
     * 审核商品
     */
    @PostMapping("/products/{productId}/review")
    @SaCheckPermission(value = "admin:product:audit", type = "admin")
    @Operation(summary = "审核商品", description = "审核商品（通过或驳回）")
    public ApiResponse<ProductVO> reviewProduct(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long productId,
            @RequestBody @Valid ProductReviewRequest request) {
        log.info("管理员审核商品，商品ID: {}, 状态: {}, 操作人: {}",
                productId, request.status(), StpUtil.getLoginId());

        Long adminId = Long.valueOf(StpUtil.getLoginId().toString());
        ProductVO product = productService.reviewProduct(productId, request, adminId);
        return ApiResponse.success(product);
    }

    /**
     * 强制下架商品
     */
    @PostMapping("/products/{productId}/force-offline")
    @SaCheckPermission(value = "admin:product:force-offline", type = "admin")
    @Operation(summary = "强制下架商品", description = "管理员强制下架已上架的商品")
    public ApiResponse<ProductVO> forceOfflineProduct(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long productId) {
        log.info("管理员强制下架商品，商品ID: {}, 操作人: {}", productId, StpUtil.getLoginId());

        Long adminId = Long.valueOf(StpUtil.getLoginId().toString());
        ProductVO product = productService.forceOfflineProduct(productId, adminId);
        return ApiResponse.success(product);
    }

    /**
     * 获取商品详情（管理员视角）
     */
    @GetMapping("/products/{productId}")
    @SaCheckPermission(value = "admin:product:detail", type = "admin")
    @Operation(summary = "获取商品详情", description = "管理员获取商品详细信息")
    public ApiResponse<ProductVO> getProductDetail(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long productId) {
        log.info("管理员查询商品详情，商品ID: {}, 操作人: {}", productId, StpUtil.getLoginId());

        ProductVO product = productService.getProductById(productId);
        return ApiResponse.success(product);
    }

    // ==================== 数据统计接口 ====================

    /**
     * 获取后台首页统计数据
     */
    @GetMapping("/dashboard/statistics")
    @SaCheckPermission(value = "admin:dashboard:statistics", type = "admin")
    @Operation(summary = "获取统计数据", description = "获取后台首页的统计数据")
    public ApiResponse<DashboardStatisticsVO> getDashboardStatistics() {
        log.info("管理员查询统计数据，操作人: {}", StpUtil.getLoginId());

        // 这里需要实现统计服务
        // 暂时返回空数据
        DashboardStatisticsVO statistics = new DashboardStatisticsVO();
        return ApiResponse.success(statistics);
    }
}
