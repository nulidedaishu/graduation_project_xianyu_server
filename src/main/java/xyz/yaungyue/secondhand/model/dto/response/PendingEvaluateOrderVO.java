package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待评价订单视图对象
 */
@Data
@Schema(description = "待评价订单信息")
public class PendingEvaluateOrderVO {

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单编号", example = "20250320123456")
    private String orderSn;

    @Schema(description = "订单状态", example = "3")
    private Integer status;

    @Schema(description = "订单状态描述", example = "待评价")
    private String statusDesc;

    @Schema(description = "对方用户ID", example = "2")
    private Long otherUserId;

    @Schema(description = "对方用户昵称", example = "李四")
    private String otherUserNickname;

    @Schema(description = "对方用户头像", example = "https://example.com/avatar.jpg")
    private String otherUserAvatar;

    @Schema(description = "评价类型(1-买家评卖家, 2-卖家评买家)", example = "1")
    private Integer evaluateType;

    @Schema(description = "订单商品列表")
    private List<OrderItemVO> items;

    @Schema(description = "订单创建时间")
    private LocalDateTime createTime;

    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;
}
