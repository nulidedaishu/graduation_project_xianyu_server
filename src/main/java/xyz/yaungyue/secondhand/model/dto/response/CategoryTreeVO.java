package xyz.yaungyue.secondhand.model.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类树形结构响应VO
 */
public record CategoryTreeVO(
        /**
         * 分类ID
         */
        Long id,

        /**
         * 父分类ID
         */
        Long parentId,

        /**
         * 分类名称
         */
        String name,

        /**
         * 图标URL
         */
        String icon,

        /**
         * 排序值
         */
        Integer sort,

        /**
         * 创建时间
         */
        LocalDateTime createTime,

        /**
         * 子分类列表
         */
        List<CategoryTreeVO> children
) implements Serializable {
}