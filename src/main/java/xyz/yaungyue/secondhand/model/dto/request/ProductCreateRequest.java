package xyz.yaungyue.secondhand.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品创建请求DTO
 *
 * @author yaungyue
 * @date 2026-02-12
 */
@Data
@Schema(description = "商品创建请求")
public class ProductCreateRequest {

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    private String name;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述", example = "全新未拆封，支持验货")
    private String description;

    /**
     * 商品价格
     */
    @NotNull(message = "商品价格不能为空")
    @Positive(message = "商品价格必须大于0")
    @Schema(description = "商品价格", example = "8999.00")
    private BigDecimal price;

    /**
     * 商品分类ID
     */
    @NotNull(message = "商品分类ID不能为空")
    @Schema(description = "商品分类ID", example = "1")
    private Long categoryId;

    /**
     * 商品图片URL列表，多个URL用逗号分隔
     */
    @Schema(description = "商品图片URL列表", example = "https://example.com/image1.jpg,https://example.com/image2.jpg")
    private String imageUrls;

    /**
     * 商品详情
     */
    @Schema(description = "商品详情", example = "详细的产品介绍...")
    private String detail;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式", example = "13800138000")
    private String contactInfo;
}