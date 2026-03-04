package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单VO
 *
 * @author yaungyue
 * @date 2026-02-26
 */
@Data
@Schema(description = "订单信息")
public class OrderVO {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    private Long id;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "202502260001")
    private String orderSn;

    /**
     * 订单总额
     */
    @Schema(description = "订单总额", example = "8999.00")
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态(0-待付款, 1-待发货, 2-待收货, 3-待评价, 4-已完成, 5-已取消, 6-已关闭)", example = "0")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "待付款")
    private String statusDesc;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式(1-支付宝, 2-微信, 3-模拟支付)", example = "1")
    private Integer payType;

    /**
     * 支付时间
     */
    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    /**
     * 收货时间
     */
    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

    /**
     * 支付截止时间
     */
    @Schema(description = "支付截止时间")
    private LocalDateTime expireTime;

    /**
     * 关闭时间
     */
    @Schema(description = "关闭时间")
    private LocalDateTime closeTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "请尽快发货")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 订单项列表
     */
    @Schema(description = "订单项列表")
    private List<OrderItemVO> items;
}
