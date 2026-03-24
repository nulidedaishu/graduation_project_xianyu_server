package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 未读消息数量视图对象
 */
@Data
@Schema(description = "未读消息数量统计")
public class ChatUnreadCountVO {

    @Schema(description = "总未读消息数量", example = "10")
    private Integer totalUnreadCount;

    @Schema(description = "会话数量", example = "3")
    private Integer sessionCount;
}
