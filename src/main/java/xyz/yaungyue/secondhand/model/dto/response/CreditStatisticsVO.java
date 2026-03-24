package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 信用积分统计视图对象
 */
@Data
@Schema(description = "信用积分统计信息")
public class CreditStatisticsVO {

    @Schema(description = "当前信用积分", example = "100")
    private Integer currentCredit;

    @Schema(description = "总积分收入", example = "200")
    private Integer totalIncome;

    @Schema(description = "总积分支出", example = "100")
    private Integer totalExpense;

    @Schema(description = "交易完成奖励次数", example = "15")
    private Integer transactionRewardCount;

    @Schema(description = "好评奖励次数", example = "10")
    private Integer goodEvaluateCount;

    @Schema(description = "信用等级", example = "优秀")
    private String creditLevel;

    @Schema(description = "信用等级描述", example = "您的信用非常好，享受优先推荐权益")
    private String creditLevelDesc;
}
