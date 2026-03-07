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
import xyz.yaungyue.secondhand.model.dto.request.ProductUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.request.StockCheckRequest;
import xyz.yaungyue.secondhand.model.dto.request.StockLockRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.ProductDetailVO;
import xyz.yaungyue.secondhand.model.dto.response.ProductListVO;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import xyz.yaungyue.secondhand.model.dto.response.StockCheckResult;
import xyz.yaungyue.secondhand.model.dto.response.StockLockResult;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.service.impl.ProductServiceImpl;
import xyz.yaungyue.secondhand.util.SaTokenUtil;


/**
 * 商品管理控制器
 * <p>
 * 安全架构说明：
 * 1. Spring Security 负责认证（Authentication）- 验证用户是否登录
 * - 在 SecurityConfig 中配置，公共接口无需登录，其他接口需要认证
 * <p>
 * 2. Sa-Token 负责授权（Authorization）- 验证用户是否有权限执行操作
 * - 使用 @SaCheckLogin 确保已登录
 * - 使用 @SaCheckRole 检查角色
 * - 使用 @SaCheckPermission 检查权限
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品的发布、查询、审核等接口")
public class ProductController {

    private final ProductService productService;

    /**
     * 发布商品
     * 需要登录，普通用户即可发布
     *
     * @param request 商品创建请求
     * @return 发布的商品信息
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
     * 获取最新商品（分页）
     * 公开接口，无需登录
     *
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表
     */
    @GetMapping
    @Operation(summary = "获取最新商品", description = "获取最新上架的商品列表，支持分页，每页固定 20 条")
    public ApiResponse<IPage<ProductListVO>> getLatestProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size) {
        Long currentUserId = null;
        try {
            currentUserId = SaTokenUtil.getCurrentUserId();
        } catch (Exception e) {
            // 未登录时忽略异常
        }
        IPage<ProductListVO> products = productService.getLatestProducts(page, size, currentUserId);
        return ApiResponse.success(products);
    }

    /**
     * 获取推荐商品
     * 公开接口，无需登录
     *
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "推荐商品", description = "随机获取已上架的推荐商品，每页固定 20 条")
    public ApiResponse<IPage<ProductListVO>> getRecommendedProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size) {
        Long currentUserId = null;
        try {
            currentUserId = SaTokenUtil.getCurrentUserId();
        } catch (Exception e) {
            // 未登录时忽略异常
        }
        IPage<ProductListVO> products = productService.getRecommendedProducts(page, size, currentUserId);
        return ApiResponse.success(products);
    }

    /**
     * 搜索商品
     * 公开接口，无需登录
     *
     * @param request 商品查询请求
     * @return 商品列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据关键词、分类等条件搜索商品")
    public ApiResponse<IPage<ProductListVO>> searchProducts(ProductQueryRequest request) {
        Long currentUserId = null;
        try {
            currentUserId = SaTokenUtil.getCurrentUserId();
        } catch (Exception e) {
            // 未登录时忽略异常
        }
        IPage<ProductListVO> products = productService.searchProducts(request, currentUserId);
        return ApiResponse.success(products);
    }

    /**
     * 获取商品详情
     * 公开接口，无需登录
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据商品 ID 获取详细信息")
    public ApiResponse<ProductDetailVO> getProductById(
            @Parameter(description = "商品 ID", example = "1") @PathVariable Long id) {
        ProductDetailVO product = productService.getProductDetailById(id);
        return ApiResponse.success(product);
    }

    /**
     * 根据分类获取商品
     * 公开接口，无需登录
     *
     * @param categoryId 分类 ID
     * @param page       页码
     * @param size       每页数量
     * @return 商品列表
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类查询商品", description = "获取指定分类下的商品列表，每页固定 20 条")
    public ApiResponse<IPage<ProductListVO>> getProductsByCategory(
            @Parameter(description = "分类 ID", example = "1") @PathVariable Long categoryId,
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size) {
        Long currentUserId = null;
        try {
            currentUserId = SaTokenUtil.getCurrentUserId();
        } catch (Exception e) {
            // 未登录时忽略异常
        }
        IPage<ProductListVO> products = productService.getProductsByCategory(categoryId, page, size, currentUserId);
        return ApiResponse.success(products);
    }

    /**
     * 获取我发布的商品
     * 需要登录
     *
     * @param page   页码
     * @param size   每页数量
     * @param status 商品状态（可选）
     * @return 商品列表
     */
    @GetMapping("/my")
    @SaCheckLogin(type = "user")
    @Operation(summary = "我的商品", description = "获取当前登录用户发布的商品列表，每页固定 20 条")
    public ApiResponse<IPage<ProductListVO>> getMyProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size,
            @Parameter(description = "商品状态 (0-待审核，1-已上架，2-审核驳回，3-已下架，4-已售出，5-已删除)", example = "1")
            @RequestParam(name = "status", required = false) Integer status) {
        Long userId = SaTokenUtil.getCurrentUserId();
        IPage<ProductListVO> products = productService.getProductsByUser(userId, page, size, status);
        return ApiResponse.success(products);
    }

    /**
     * 下架商品
     * 需要登录，且是商品所有者
     *
     * @param id 商品 ID
     * @return 下架后的商品信息
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
     *
     * @param id 商品 ID
     * @return 重新上架后的商品信息
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
     *
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表
     */
    @GetMapping("/pending")
    @SaCheckRole(value = "admin", type = "admin")
    @Operation(summary = "待审核商品", description = "管理员获取待审核的商品列表，每页固定 20 条")
    public ApiResponse<IPage<ProductVO>> getPendingProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size) {
        IPage<ProductVO> products = productService.getPendingProducts(page, size);
        return ApiResponse.success(products);
    }

    /**
     * 审核商品（管理员）
     * 需要管理员角色
     *
     * @param id      商品 ID
     * @param request 审核请求
     * @return 审核后的商品信息
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

    /**
     * 删除商品
     * 需要登录，且是商品所有者
     * 支持软删除，将商品状态设置为已删除
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "删除商品", description = "删除自己发布的商品（仅支持待审核、审核驳回、已下架、已售出状态的商品）")
    public ApiResponse<Void> deleteProduct(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long id) {
        Long userId = SaTokenUtil.getCurrentUserId();
        productService.deleteProduct(id, userId);
        return ApiResponse.success();
    }

    /**
     * 修改商品
     * 需要登录，且是商品所有者
     * 仅支持待审核、审核驳回、已下架状态的商品修改
     *
     * @param id      商品 ID
     * @param request 商品更新请求
     * @return 修改后的商品信息
     */
    @PutMapping("/{id}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "修改商品", description = "修改自己发布的商品信息（仅支持待审核、审核驳回、已下架状态的商品）")
    public ApiResponse<ProductVO> updateProduct(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long id,
            @RequestBody @Valid ProductUpdateRequest request) {
        Long userId = SaTokenUtil.getCurrentUserId();
        log.info("用户修改商品，商品ID: {}, 用户ID: {}", id, userId);
        request.setId(id);
        ProductVO product = productService.updateProduct(request, userId);
        return ApiResponse.success(product);
    }

    /**
     * 检查库存是否充足
     * 需要登录
     *
     * @param id      商品 ID
     * @param request 库存检查请求
     * @return 库存检查结果
     */
    @PostMapping("/{id}/check-stock")
    @SaCheckLogin(type = "user")
    @Operation(summary = "检查库存", description = "校验指定商品的库存是否满足购买需求")
    public ApiResponse<StockCheckResult> checkStock(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long id,
            @RequestBody @Valid StockCheckRequest request) {
        log.info("检查库存请求，商品ID: {}, 数量: {}", id, request.getQuantity());

        // 验证路径参数和请求体中的商品ID一致
        if (!id.equals(request.getProductId())) {
            throw new BusinessException(400, "商品ID不一致");
        }

        // 1. 查询商品信息
        Product product = productService.getById(id);
        if (product == null) {
            log.warn("检查库存失败，商品不存在，productId={}", id);
            StockCheckResult result = new StockCheckResult();
            result.setProductId(id);
            result.setProductName("未知商品");
            result.setStock(0);
            result.setLockedStock(0);
            result.setAvailableStock(0);
            result.setRequestedQuantity(request.getQuantity());
            result.setSufficient(false);
            return ApiResponse.error(404, "商品不存在");
        }

        // 2. 计算可用库存
        int lockedStock = product.getLockedStock() != null ? product.getLockedStock() : 0;
        int availableStock = product.getStock() - lockedStock;

        // 3. 构建返回结果
        StockCheckResult result = new StockCheckResult();
        result.setProductId(product.getId());
        result.setProductName(product.getTitle());
        result.setStock(product.getStock());
        result.setLockedStock(lockedStock);
        result.setAvailableStock(availableStock);
        result.setRequestedQuantity(request.getQuantity());
        result.setSufficient(availableStock >= request.getQuantity());

        log.info("检查库存结果，商品ID: {}, 可用库存: {}, 请求数量: {}, 是否充足: {}",
                product.getId(), availableStock, request.getQuantity(), result.getSufficient());

        return ApiResponse.success(result);
    }

    /**
     * 锁定库存（下单预占）
     * 需要登录
     *
     * @param id      商品 ID
     * @param request 库存锁定请求
     * @return 库存锁定结果
     */
    @PostMapping("/{id}/lock-stock")
    @SaCheckLogin(type = "user")
    @Operation(summary = "锁定库存", description = "下单时预占商品库存，防止超卖")
    public ApiResponse<StockLockResult> lockStock(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long id,
            @RequestBody @Valid StockLockRequest request) {
        log.info("锁定库存请求，商品ID: {}, 数量: {}", id, request.getQuantity());

        // 验证路径参数和请求体中的商品ID一致
        if (!id.equals(request.getProductId())) {
            throw new BusinessException(400, "商品ID不一致");
        }

        // 1. 查询商品信息
        Product product = productService.getById(id);
        if (product == null) {
            log.warn("锁定库存失败，商品不存在，productId={}", id);
            StockLockResult result = new StockLockResult();
            result.setProductId(id);
            result.setProductName("未知商品");
            result.setSuccess(false);
            result.setMessage("商品不存在");
            return ApiResponse.error(404, "商品不存在");
        }

        // 2. 调用服务层锁定库存
        boolean locked = productService.lockStock(id, request.getQuantity());

        // 3. 构建返回结果
        StockLockResult result = new StockLockResult();
        result.setProductId(product.getId());
        result.setProductName(product.getTitle());
        result.setSuccess(locked);
        result.setMessage(locked ? "库存锁定成功" : "库存不足或操作失败");

        if (locked) {
            log.info("锁定库存成功，商品ID: {}, 数量: {}", id, request.getQuantity());
            return ApiResponse.success(result);
        } else {
            log.warn("锁定库存失败，商品ID: {}, 数量: {}", id, request.getQuantity());
            return ApiResponse.error(400, "库存锁定失败");
        }
    }
}
