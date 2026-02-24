package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品响应VO
 *
 * @author yaungyue
 * @date 2026-02-12
 */
@Data
@Schema(description = "商品信息")
public class ProductVO {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", example = "1")
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
     * 商品分类ID
     */
    @Schema(description = "商品分类ID", example = "1")
    private Long categoryId;

    /**
     * 商品分类名称
     */
    @Schema(description = "商品分类名称", example = "手机数码")
    private String categoryName;

    /**
     * 商品图片URL列表
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

    /**
     * 商品状态：0-待审核，1-已上架，2-已下架，3-审核拒绝
     */
    @Schema(description = "商品状态：0-待审核，1-已上架，2-已下架，3-审核拒绝", example = "0")
    private Integer status;

    /**
     * 发布者ID
     */
    @Schema(description = "发布者ID", example = "1")
    private Long userId;

    /**
     * 发布者昵称
     */
    @Schema(description = "发布者昵称", example = "张三")
    private String userNickname;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-02-12T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-02-12T10:00:00")
    private LocalDateTime updateTime;
}