package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信用积分流水视图对象
 */
@Data
@Schema(description = "信用积分流水信息")
public class CreditLogVO {

    @Schema(description = "积分记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "关联订单ID", example = "1")
    private Long orderId;

    @Schema(description = "变动分值", example = "10")
    private Integer changeValue;

    @Schema(description = "变动分值显示（带符号）", example = "+10")
    private String changeValueDisplay;

    @Schema(description = "变动原因", example = "交易完成")
    private String reason;

    @Schema(description = "订单编号", example = "20250320123456")
    private String orderSn;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
