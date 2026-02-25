package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
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
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建商品分类（需要管理员权限）
     */
    @PostMapping
    @SaCheckRole("admin")
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
     */
    @PutMapping
    @SaCheckRole("admin")
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
     */
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        log.info("收到删除分类请求，分类ID: {}", id);
        
        categoryService.deleteCategory(id);
        
        log.info("分类删除成功，分类ID: {}", id);
        return ApiResponse.success();
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    public ApiResponse<CategoryVO> getCategoryById(@PathVariable Long id) {
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
     */
    @GetMapping
    @SaCheckLogin
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
     * 获取分类树形结构
     */
    @GetMapping("/tree")
    @SaCheckLogin
    public ApiResponse<List<CategoryTreeVO>> getCategoryTree() {
        log.info("收到获取分类树形结构请求");
        
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        
        log.info("获取分类树形结构成功，根节点数量: {}", tree.size());
        return ApiResponse.success(tree);
    }

    /**
     * 获取指定分类的子分类
     */
    @GetMapping("/{parentId}/children")
    @SaCheckLogin
    public ApiResponse<List<CategoryVO>> getChildrenByParentId(@PathVariable Long parentId) {
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
     */
    @GetMapping("/check-name")
    @SaCheckLogin
    public ApiResponse<Boolean> checkCategoryName(
            @RequestParam(required = false) Long parentId,
            @RequestParam String name,
            @RequestParam(required = false) Long excludeId) {
        
        log.info("收到分类名称检查请求，父分类ID: {}, 名称: {}, 排除ID: {}", parentId, name, excludeId);
        
        boolean exists = categoryService.existsByNameInSameLevel(parentId, name, excludeId);
        boolean available = !exists;
        
        log.info("分类名称检查完成，父分类ID: {}, 名称: {}, 可用: {}", parentId, name, available);
        return ApiResponse.success(available);
    }

    /**
     * 将Category实体转换为CategoryVO
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