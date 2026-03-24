package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏视图对象
 */
@Data
@Schema(description = "收藏信息")
public class FavoriteVO {

    @Schema(description = "收藏ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品标题", example = "iPhone 14 Pro")
    private String productTitle;

    @Schema(description = "商品主图", example = "https://example.com/image.jpg")
    private String productImage;

    @Schema(description = "商品价格", example = "5999.00")
    private String productPrice;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}
