package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存校验结果
 *
 * @author yaung
 * @date 2026-03-04
 */
@Data
@Schema(description = "库存校验结果")
public class StockCheckResult {

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
     * 总库存
     */
    @Schema(description = "总库存", example = "10")
    private Integer stock;

    /**
     * 锁定库存
     */
    @Schema(description = "锁定库存", example = "2")
    private Integer lockedStock;

    /**
     * 可用库存
     */
    @Schema(description = "可用库存", example = "8")
    private Integer availableStock;

    /**
     * 请求数量
     */
    @Schema(description = "请求数量", example = "1")
    private Integer requestedQuantity;

    /**
     * 是否充足
     */
    @Schema(description = "是否充足", example = "true")
    private Boolean sufficient;
}
