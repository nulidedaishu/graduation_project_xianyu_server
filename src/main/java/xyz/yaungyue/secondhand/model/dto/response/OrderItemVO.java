package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项VO
 *
 * @author yaungyue
 * @date 2026-02-26
 */
@Data
@Schema(description = "订单项信息")
public class OrderItemVO {

    /**
     * 订单项ID
     */
    @Schema(description = "订单项ID", example = "1")
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
     * 成交价格
     */
    @Schema(description = "成交价格", example = "8999.00")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @Schema(description = "购买数量", example = "1")
    private Integer quantity;

    /**
     * 卖家ID
     */
    @Schema(description = "卖家ID", example = "1")
    private Long sellerId;

    /**
     * 卖家昵称
     */
    @Schema(description = "卖家昵称", example = "张三")
    private String sellerName;
}
