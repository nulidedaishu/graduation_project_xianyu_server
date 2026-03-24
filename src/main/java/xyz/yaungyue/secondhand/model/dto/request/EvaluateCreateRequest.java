package xyz.yaungyue.secondhand.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价创建请求
 */
@Data
@Schema(description = "评价创建请求")
public class EvaluateCreateRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", example = "1", required = true)
    private Long orderId;

    @NotNull(message = "被评价人ID不能为空")
    @Schema(description = "被评价人ID", example = "2", required = true)
    private Long toUserId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    @Schema(description = "评分(1-5)", example = "5", required = true)
    private Integer score;

    @NotBlank(message = "评价内容不能为空")
    @Schema(description = "评价内容", example = "商品质量很好，卖家服务态度不错", required = true)
    private String content;

    @NotNull(message = "评价类型不能为空")
    @Schema(description = "类型(1-买家评卖家, 2-卖家评买家)", example = "1", required = true)
    private Integer type;
}
