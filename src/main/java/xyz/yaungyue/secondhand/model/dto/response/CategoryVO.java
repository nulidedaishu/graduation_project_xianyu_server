package xyz.yaungyue.secondhand.model.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类响应VO
 */
public record CategoryVO(
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
        LocalDateTime createTime
) implements Serializable {
}