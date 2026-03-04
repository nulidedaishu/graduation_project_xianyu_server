package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车VO
 *
 * @author yaungyue
 * @date 2026-02-26
 */
@Data
@Schema(description = "购物车信息")
public class CartVO {

    /**
     * 购物车ID
     */
    @Schema(description = "购物车ID", example = "1")
    private Long id;

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", example = "1")
    private Long productId;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    private String productName;

    /**
     * 商品图片
     */
    @Schema(description = "商品图片", example = "https://example.com/image.jpg")
    private String productImage;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格", example = "8999.00")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @Schema(description = "购买数量", example = "1")
    private Integer quantity;

    /**
     * 库存
     */
    @Schema(description = "库存", example = "5")
    private Integer stock;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
