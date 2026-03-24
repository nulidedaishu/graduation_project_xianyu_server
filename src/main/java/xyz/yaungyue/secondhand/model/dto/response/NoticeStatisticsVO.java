package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通知统计视图对象
 */
@Data
@Schema(description = "通知统计信息")
public class NoticeStatisticsVO {

    @Schema(description = "未读通知数量", example = "5")
    private Integer unreadCount;

    @Schema(description = "总通知数量", example = "20")
    private Integer totalCount;

    @Schema(description = "审核通知未读数量", example = "2")
    private Integer auditUnreadCount;

    @Schema(description = "订单通知未读数量", example = "3")
    private Integer orderUnreadCount;

    @Schema(description = "系统公告未读数量", example = "0")
    private Integer systemUnreadCount;
}
