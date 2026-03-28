package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.model.dto.request.CategoryCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.CategoryUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.CategoryTreeVO;
import xyz.yaungyue.secondhand.model.dto.response.CategoryVO;
import xyz.yaungyue.secondhand.model.entity.Category;
import xyz.yaungyue.secondhand.service.CategoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "商品分类管理", description = "商品分类的增删改查接口")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建商品分类（需要管理员权限）
     * @param request 分类创建请求
     * @return 创建的分类信息
     */
    @PostMapping
    @SaCheckRole("admin")
    @Operation(summary = "创建分类", description = "创建新的商品分类，需要管理员权限")
    public ApiResponse<CategoryVO> createCategory(@RequestBody @Valid CategoryCreateRequest request) {
        log.info("收到创建分类请求，分类名称: {}", request.name());
        
        Category category = categoryService.createCategory(
                request.parentId(),
                request.name(),
                request.icon(),
                request.sort()
        );
        
        CategoryVO response = convertToVO(category);
        log.info("分类创建成功，分类ID: {}", category.getId());
        return ApiResponse.success(response);
    }

    /**
     * 更新商品分类（需要管理员权限）
     * @param request 分类更新请求
     * @return 更新后的分类信息
     */
    @PutMapping
    @SaCheckRole("admin")
    @Operation(summary = "更新分类", description = "更新商品分类信息，需要管理员权限")
    public ApiResponse<CategoryVO> updateCategory(@RequestBody @Valid CategoryUpdateRequest request) {
        log.info("收到更新分类请求，分类ID: {}", request.id());
        
        Category category = categoryService.updateCategory(
                request.id(),
                request.parentId(),
                request.name(),
                request.icon(),
                request.sort()
        );
        
        CategoryVO response = convertToVO(category);
        log.info("分类更新成功，分类ID: {}", category.getId());
        return ApiResponse.success(response);
    }

    /**
     * 删除商品分类（需要管理员权限）
     * @param id 分类 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    @Operation(summary = "删除分类", description = "删除商品分类，需要管理员权限")
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id) {
        log.info("收到删除分类请求，分类ID: {}", id);
        
        categoryService.deleteCategory(id);
        
        log.info("分类删除成功，分类ID: {}", id);
        return ApiResponse.success();
    }

    /**
     * 获取分类详情
     * @param id 分类 ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "获取分类详情", description = "根据分类ID获取分类详细信息")
    public ApiResponse<CategoryVO> getCategoryById(
            @Parameter(description = "分类ID", example = "1") @PathVariable Long id) {
        log.info("收到获取分类详情请求，分类ID: {}", id);
        
        Category category = categoryService.getById(id);
        if (category == null) {
            log.warn("分类不存在，分类ID: {}", id);
            return ApiResponse.error(404, "分类不存在");
        }
        
        CategoryVO response = convertToVO(category);
        log.info("获取分类详情成功，分类ID: {}", id);
        return ApiResponse.success(response);
    }

    /**
     * 获取所有分类（平铺结构）
     * @return 分类列表
     */
    @GetMapping
    @SaCheckLogin
    @Operation(summary = "获取所有分类", description = "获取所有商品分类列表（平铺结构）")
    public ApiResponse<List<CategoryVO>> getAllCategories() {
        log.info("收到获取所有分类请求");
        
        List<Category> categories = categoryService.list();
        List<CategoryVO> response = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
                
        log.info("获取所有分类成功，分类数量: {}", response.size());
        return ApiResponse.success(response);
    }

    /**
     * 获取分类树形结构（公开接口，无需登录）
     * @return 分类树形结构
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取商品分类的树形结构（公开接口，无需登录）")
    public ApiResponse<List<CategoryTreeVO>> getCategoryTree() {
        log.info("收到获取分类树形结构请求");
        
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        
        log.info("获取分类树形结构成功，根节点数量: {}", tree.size());
        return ApiResponse.success(tree);
    }

    /**
     * 获取指定分类的子分类
     * @param parentId 父分类 ID
     * @return 子分类列表
     */
    @GetMapping("/{parentId}/children")
    @SaCheckLogin
    @Operation(summary = "获取子分类", description = "获取指定父分类下的子分类列表")
    public ApiResponse<List<CategoryVO>> getChildrenByParentId(
            @Parameter(description = "父分类ID", example = "0") @PathVariable Long parentId) {
        log.info("收到获取子分类请求，父分类ID: {}", parentId);
        
        List<Category> children = categoryService.getChildrenByParentId(parentId);
        List<CategoryVO> response = children.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
                
        log.info("获取子分类成功，父分类ID: {}, 子分类数量: {}", parentId, response.size());
        return ApiResponse.success(response);
    }

    /**
     * 检查分类名称是否可用（同级下唯一性检查）
     * @param parentId 父分类 ID
     * @param name 分类名称
     * @param excludeId 排除的分类 ID
     * @return true-可用，false-不可用
     */
    @GetMapping("/check-name")
    @SaCheckLogin
    @Operation(summary = "检查分类名称", description = "检查分类名称在同级别下是否可用")
    public ApiResponse<Boolean> checkCategoryName(
            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "分类名称") @RequestParam String name,
            @Parameter(description = "排除的分类ID") @RequestParam(required = false) Long excludeId) {
        
        log.info("收到分类名称检查请求，父分类ID: {}, 名称: {}, 排除ID: {}", parentId, name, excludeId);
        
        boolean exists = categoryService.existsByNameInSameLevel(parentId, name, excludeId);
        boolean available = !exists;
        
        log.info("分类名称检查完成，父分类ID: {}, 名称: {}, 可用: {}", parentId, name, available);
        return ApiResponse.success(available);
    }

    /**
     * 将 Category 实体转换为 CategoryVO
     * @param category 分类实体
     * @return 分类 VO
     */
    private CategoryVO convertToVO(Category category) {
        return new CategoryVO(
                category.getId(),
                category.getParentId(),
                category.getName(),
                category.getIcon(),
                category.getSort(),
                category.getCreateTime() != null ? category.getCreateTime() : LocalDateTime.now()
        );
    }
}