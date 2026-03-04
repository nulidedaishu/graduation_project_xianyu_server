package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存锁定结果
 *
 * @author yaung
 * @date 2026-03-04
 */
@Data
@Schema(description = "库存锁定结果")
public class StockLockResult {

    /**
     * 商品 ID
     */
    @Schema(description = "商品 ID", example = "1")
    private Long productId;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    private String productName;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 失败消息
     */
    @Schema(description = "失败消息", example = "库存不足")
    private String message;
}
