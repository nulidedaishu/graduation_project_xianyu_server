package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告视图对象
 */
@Data
@Schema(description = "通知公告信息")
public class NoticeVO {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "接收用户ID", example = "1")
    private Long userId;

    @Schema(description = "标题", example = "商品审核通知")
    private String title;

    @Schema(description = "通知内容", example = "您的商品已通过审核并上架")
    private String content;

    @Schema(description = "类型(1-审核通知, 2-订单通知, 3-系统公告)", example = "1")
    private Integer type;

    @Schema(description = "类型描述", example = "审核通知")
    private String typeDesc;

    @Schema(description = "是否已读(0-未读, 1-已读)", example = "0")
    private Integer isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
