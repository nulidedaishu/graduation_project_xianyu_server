package xyz.yaungyue.secondhand.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏创建请求
 */
@Data
@Schema(description = "收藏创建请求")
public class FavoriteCreateRequest {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1", required = true)
    private Long productId;
}
