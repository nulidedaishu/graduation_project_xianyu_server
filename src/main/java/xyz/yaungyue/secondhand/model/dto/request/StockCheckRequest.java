package xyz.yaungyue.secondhand.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 库存校验请求
 *
 * @author yaung
 * @date 2026-03-04
 */
@Data
@Schema(description = "库存校验请求")
public class StockCheckRequest {

    /**
     * 商品 ID
     */
    @NotNull(message = "商品 ID 不能为空")
    @Schema(description = "商品 ID", example = "1")
    private Long productId;

    /**
     * 请求数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于 0")
    @Schema(description = "请求数量", example = "1")
    private Integer quantity;
}
