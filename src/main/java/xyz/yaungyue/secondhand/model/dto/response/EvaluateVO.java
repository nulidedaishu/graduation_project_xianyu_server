package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价视图对象
 */
@Data
@Schema(description = "评价信息")
public class EvaluateVO {

    @Schema(description = "评价ID", example = "1")
    private Long id;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "评价人ID", example = "1")
    private Long fromUserId;

    @Schema(description = "评价人昵称", example = "张三")
    private String fromUserNickname;

    @Schema(description = "评价人头像", example = "https://example.com/avatar.jpg")
    private String fromUserAvatar;

    @Schema(description = "被评价人ID", example = "2")
    private Long toUserId;

    @Schema(description = "被评价人昵称", example = "李四")
    private String toUserNickname;

    @Schema(description = "评分(1-5)", example = "5")
    private Integer score;

    @Schema(description = "评价内容", example = "商品质量很好，卖家服务态度不错")
    private String content;

    @Schema(description = "类型(1-买家评卖家, 2-卖家评买家)", example = "1")
    private Integer type;

    @Schema(description = "评价时间")
    private LocalDateTime createTime;
}
