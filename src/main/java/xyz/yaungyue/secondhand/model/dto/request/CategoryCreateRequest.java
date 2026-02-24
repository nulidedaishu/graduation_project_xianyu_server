package xyz.yaungyue.secondhand.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 创建商品分类请求DTO
 */
public record CategoryCreateRequest(
        /**
         * 父分类ID（可为空，表示顶级分类）
         */
        Long parentId,

        /**
         * 分类名称
         */
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称长度不能超过50个字符")
        String name,

        /**
         * 图标URL
         */
        @Size(max = 255, message = "图标URL长度不能超过255个字符")
        String icon,

        /**
         * 排序值
         */
        @NotNull(message = "排序值不能为空")
        Integer sort
) implements Serializable {
}