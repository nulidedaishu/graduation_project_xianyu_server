package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知会话视图对象
 *
 * @author yaung
 * @date 2026-03-22
 */
@Data
@Schema(description = "系统通知会话信息")
public class NoticeSessionVO {

    @Schema(description = "会话ID（固定为'system'）", example = "system")
    private String sessionId;

    @Schema(description = "会话标题", example = "系统通知")
    private String title;

    @Schema(description = "会话图标", example = "/icons/system-notification.png")
    private String icon;

    @Schema(description = "最后一条通知标题", example = "商品审核通过")
    private String lastNoticeTitle;

    @Schema(description = "最后一条通知时间")
    private LocalDateTime lastNoticeTime;

    @Schema(description = "未读通知数", example = "5")
    private Integer unreadCount;
}
