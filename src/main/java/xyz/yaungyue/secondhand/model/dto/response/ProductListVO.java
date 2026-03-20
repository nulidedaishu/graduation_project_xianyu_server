package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表信息 VO（用于列表展示）
 */
@Data
@Schema(description = "商品列表信息")
public class ProductListVO {

    /**
     * 商品 ID
     */
    @Schema(description = "商品 ID", example = "1")
    private Long id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    private String name;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格", example = "8999.00")
    private BigDecimal price;

    /**
     * 发布者昵称
     */
    @Schema(description = "发布者昵称", example = "张三")
    private String userNickname;

    /**
     * 主图 URL
     */
    @Schema(description = "主图 URL", example = "https://example.com/image.jpg")
    private String mainImageUrl;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;
}
