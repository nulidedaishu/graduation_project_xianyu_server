package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 总未读消息数VO
 *
 * @author yaung
 * @date 2026-03-22
 */
@Data
@Builder
@Schema(description = "总未读消息统计")
public class TotalUnreadCountVO {

    @Schema(description = "总未读数（聊天+通知）", example = "10")
    private Integer total;

    @Schema(description = "聊天未读数", example = "5")
    private Integer chat;

    @Schema(description = "通知未读数", example = "5")
    private Integer notice;
}
