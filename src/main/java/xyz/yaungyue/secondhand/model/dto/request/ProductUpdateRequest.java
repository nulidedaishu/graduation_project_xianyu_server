package xyz.yaungyue.secondhand.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品更新请求DTO
 *
 * @author yaungyue
 * @date 2026-02-12
 */
@Data
@Schema(description = "商品更新请求")
public class ProductUpdateRequest {
    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Long id;

    /**
     * 商品名称
     */
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
    @Positive(message = "商品价格必须大于0")
    @Schema(description = "商品价格", example = "8999.00")
    private BigDecimal price;

    /**
     * 商品分类ID
     */
    @Schema(description = "商品分类ID", example = "1")
    private Long categoryId;

    /**
     * 主图 URL
     */
    @Schema(description = "主图 URL", example = "https://example.com/main.jpg")
    private String mainImageUrl;

    /**
     * 其他图片 URL 数组
     */
    @Schema(description = "其他图片 URL 数组")
    private List<String> otherImageUrls;

    /**
     * 运费
     */
    @Schema(description = "运费", example = "0.00")
    private BigDecimal freight;

    /**
     * 区位置 ID（同城交易）
     */
    @Schema(description = "区位置 ID", example = "1")
    private Long districtId;
}
