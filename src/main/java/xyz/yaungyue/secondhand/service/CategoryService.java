package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.yaungyue.secondhand.model.dto.response.CategoryTreeVO;

import java.util.List;

/**
* @author yaung
* @description 针对表【bus_category(商品分类表)】的数据库操作Service
* @createDate 2026-02-12 17:21:41
*/
public interface CategoryService extends IService<Category> {

    /**
     * 创建商品分类
     * @param parentId 父分类ID（可为null）
     * @param name 分类名称
     * @param icon 图标URL
     * @param sort 排序值
     * @return 创建的分类
     */
    Category createCategory(Long parentId, String name, String icon, Integer sort);

    /**
     * 更新商品分类
     * @param id 分类ID
     * @param parentId 父分类ID（可为null）
     * @param name 分类名称
     * @param icon 图标URL
     * @param sort 排序值
     * @return 更新后的分类
     */
    Category updateCategory(Long id, Long parentId, String name, String icon, Integer sort);

    /**
     * 删除商品分类
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean deleteCategory(Long id);

    /**
     * 获取分类树形结构
     * @return 分类树列表
     */
    List<CategoryTreeVO> getCategoryTree();

    /**
     * 获取指定分类的子分类列表
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> getChildrenByParentId(Long parentId);

    /**
     * 检查分类名称在同级下是否已存在
     * @param parentId 父分类ID
     * @param name 分类名称
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByNameInSameLevel(Long parentId, String name, Long excludeId);

    /**
     * 检查分类是否有子分类
     * @param id 分类ID
     * @return 是否有子分类
     */
    boolean hasChildren(Long id);

    /**
     * 检查分类是否被商品使用
     * @param id 分类ID
     * @return 是否被使用
     */
    boolean isUsedByProduct(Long id);
}