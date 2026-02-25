package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.exception.ErrorCode;
import xyz.yaungyue.secondhand.mapper.CategoryMapper;
import xyz.yaungyue.secondhand.mapper.ProductMapper;
import xyz.yaungyue.secondhand.model.dto.response.CategoryTreeVO;
import xyz.yaungyue.secondhand.model.entity.Category;
import xyz.yaungyue.secondhand.service.CategoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author yaung
* @description 针对表【bus_category(商品分类表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category createCategory(Long parentId, String name, String icon, Integer sort) {
        log.info("开始创建商品分类，父分类ID: {}, 名称: {}", parentId, name);
        
        // 参数校验
        if (parentId != null) {
            Category parentCategory = getById(parentId);
            if (parentCategory == null) {
                log.warn("父分类不存在，父分类ID: {}", parentId);
                throw new BusinessException(ErrorCode.CATEGORY_PARENT_NOT_FOUND);
            }
        }
        
        // 检查同级分类名称是否重复
        if (existsByNameInSameLevel(parentId, name, null)) {
            log.warn("同级分类名称已存在，父分类ID: {}, 名称: {}", parentId, name);
            throw new BusinessException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        
        // 创建分类
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setIcon(icon);
        category.setSort(sort);
        category.setCreateTime(LocalDateTime.now());
        
        boolean saved = save(category);
        if (!saved) {
            log.error("保存分类失败，名称: {}", name);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "创建分类失败");
        }
        
        log.info("商品分类创建成功，分类ID: {}, 名称: {}", category.getId(), name);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category updateCategory(Long id, Long parentId, String name, String icon, Integer sort) {
        log.info("开始更新商品分类，分类ID: {}, 名称: {}", id, name);
        
        // 检查分类是否存在
        Category category = getById(id);
        if (category == null) {
            log.warn("分类不存在，分类ID: {}", id);
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        
        // 检查父分类是否存在
        if (parentId != null && !parentId.equals(category.getParentId())) {
            Category parentCategory = getById(parentId);
            if (parentCategory == null) {
                log.warn("父分类不存在，父分类ID: {}", parentId);
                throw new BusinessException(ErrorCode.CATEGORY_PARENT_NOT_FOUND);
            }
            
            // 检查不能将分类设置为自己或自己的子分类的子分类
            if (isDescendantOf(id, parentId)) {
                log.warn("不能将分类设置为自己的子分类，分类ID: {}, 父分类ID: {}", id, parentId);
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不能将分类设置为自己的子分类");
            }
        }
        
        // 检查同级分类名称是否重复
        if (existsByNameInSameLevel(parentId, name, id)) {
            log.warn("同级分类名称已存在，父分类ID: {}, 名称: {}, 排除ID: {}", parentId, name, id);
            throw new BusinessException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        
        // 更新分类
        category.setParentId(parentId);
        category.setName(name);
        category.setIcon(icon);
        category.setSort(sort);
        
        boolean updated = updateById(category);
        if (!updated) {
            log.error("更新分类失败，分类ID: {}", id);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "更新分类失败");
        }
        
        log.info("商品分类更新成功，分类ID: {}, 名称: {}", id, name);
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        log.info("开始删除商品分类，分类ID: {}", id);
        
        // 检查分类是否存在
        Category category = getById(id);
        if (category == null) {
            log.warn("分类不存在，分类ID: {}", id);
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        
        // 检查是否有子分类
        if (hasChildren(id)) {
            log.warn("该分类下存在子分类，无法删除，分类ID: {}", id);
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }
        
        // 检查是否被商品使用
        if (isUsedByProduct(id)) {
            log.warn("该分类正在被商品使用，无法删除，分类ID: {}", id);
            throw new BusinessException(ErrorCode.CATEGORY_USED_BY_PRODUCT);
        }
        
        // 执行删除
        boolean removed = removeById(id);
        if (!removed) {
            log.error("删除分类失败，分类ID: {}", id);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "删除分类失败");
        }
        
        log.info("商品分类删除成功，分类ID: {}, 名称: {}", id, category.getName());
        return true;
    }

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        log.info("获取商品分类树形结构");
        
        // 获取所有分类
        List<Category> allCategories = list();
        
        // 构建树形结构
        List<CategoryTreeVO> tree = buildCategoryTree(allCategories, null);
        
        log.info("商品分类树形结构构建完成，根节点数量: {}", tree.size());
        return tree;
    }

    @Override
    public List<Category> getChildrenByParentId(Long parentId) {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId)
               .orderByAsc("sort");
        return list(wrapper);
    }

    @Override
    public boolean existsByNameInSameLevel(Long parentId, String name, Long excludeId) {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId == null ? "NULL" : parentId)
               .eq("name", name);
        
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        
        return count(wrapper) > 0;
    }

    @Override
    public boolean hasChildren(Long id) {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        return count(wrapper) > 0;
    }

    @Override
    public boolean isUsedByProduct(Long id) {
        return productMapper.selectCountByCategoryId(id) > 0;
    }

    /**
     * 构建分类树形结构
     * @param categories 所有分类列表
     * @param parentId 父分类ID
     * @return 树形结构列表
     */
    private List<CategoryTreeVO> buildCategoryTree(List<Category> categories, Long parentId) {
        return categories.stream()
                .filter(category -> {
                    if (parentId == null) {
                        return category.getParentId() == null;
                    } else {
                        return parentId.equals(category.getParentId());
                    }
                })
                .sorted((c1, c2) -> {
                    int sort1 = c1.getSort() != null ? c1.getSort() : 0;
                    int sort2 = c2.getSort() != null ? c2.getSort() : 0;
                    return Integer.compare(sort1, sort2);
                })
                .map(category -> {
                    List<CategoryTreeVO> children = buildCategoryTree(categories, category.getId());
                    return new CategoryTreeVO(
                            category.getId(),
                            category.getParentId(),
                            category.getName(),
                            category.getIcon(),
                            category.getSort(),
                            category.getCreateTime() != null ? category.getCreateTime() : LocalDateTime.now(),
                            children
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查目标分类是否是指定分类的后代
     * @param categoryId 目标分类ID
     * @param parentId 父分类ID
     * @return 是否为后代
     */
    private boolean isDescendantOf(Long categoryId, Long parentId) {
        if (categoryId.equals(parentId)) {
            return true;
        }
        
        Category parent = getById(parentId);
        while (parent != null && parent.getParentId() != null) {
            if (parent.getParentId().equals(categoryId)) {
                return true;
            }
            parent = getById(parent.getParentId());
        }
        
        return false;
    }
}