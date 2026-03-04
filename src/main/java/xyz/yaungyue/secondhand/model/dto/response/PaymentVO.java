package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付信息VO
 *
 * @author yaungyue
 * @date 2026-02-26
 */
@Data
@Schema(description = "支付信息")
public class PaymentVO {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "202502260001")
    private String orderSn;

    /**
     * 支付金额
     */
    @Schema(description = "支付金额", example = "8999.00")
    private BigDecimal amount;

    /**
     * 支付表单HTML（支付宝）
     */
    @Schema(description = "支付表单HTML（用于支付宝PC支付）")
    private String payFormHtml;

    /**
     * 支付链接（用于跳转）
     */
    @Schema(description = "支付链接")
    private String payUrl;

    /**
     * 支付截止时间
     */
    @Schema(description = "支付截止时间")
    private LocalDateTime expireTime;
}
