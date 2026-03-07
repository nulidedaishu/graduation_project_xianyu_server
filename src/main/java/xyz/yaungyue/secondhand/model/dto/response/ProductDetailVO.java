package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情 VO（用于详情展示）
 */
@Data
@Schema(description = "商品详情信息")
public class ProductDetailVO {

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
     * 商品描述
     */
    @Schema(description = "商品描述", example = "全新未拆封，支持验货")
    private String description;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格", example = "8999.00")
    private BigDecimal price;

    /**
     * 商品分类名称
     */
    @Schema(description = "商品分类名称", example = "手机数码")
    private String categoryName;

    /**
     * 商品库存
     */
    @Schema(description = "商品库存", example = "10")
    private Integer stock;

    /**
     * 发布者昵称
     */
    @Schema(description = "发布者昵称", example = "张三")
    private String userNickname;

    /**
     * 所在城市
     */
    @Schema(description = "所在城市", example = "北京市")
    private String province;

    /**
     * 商品图片 URL 列表（包含主图和其他图片）
     */
    @Schema(description = "商品图片 URL 列表")
    private List<String> imageUrls;

    /**
     * 商品状态
     */
    @Schema(description = "商品状态", example = "1")
    private Integer status;

    /**
     * 发布者id
     */
    @Schema(description = "发布者id", example = "1")
    private Long userId;
}
