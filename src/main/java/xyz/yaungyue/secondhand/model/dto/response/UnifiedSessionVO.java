package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一会话视图对象
 * 将用户聊天会话和系统通知会话统一的数据结构
 *
 * @author yaung
 * @date 2026-03-22
 */
@Data
@Builder
@Schema(description = "统一会话信息")
public class UnifiedSessionVO {

    @Schema(description = "会话类型: user-用户聊天, system-系统通知", example = "user")
    private String sessionType;

    @Schema(description = "会话ID (用户会话用sessionKey, 系统会话固定为'system')", example = "1_2")
    private String sessionId;

    // ===== 用户会话字段 =====
    @Schema(description = "对方用户ID（用户聊天时用）", example = "2")
    private Long otherUserId;

    @Schema(description = "对方用户昵称（用户聊天时用）", example = "李四")
    private String otherUserName;

    @Schema(description = "对方用户头像（用户聊天时用）", example = "https://example.com/avatar.jpg")
    private String otherUserAvatar;

    @Schema(description = "关联商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品标题", example = "iPhone 14 Pro")
    private String productTitle;

    @Schema(description = "商品图片", example = "https://example.com/product.jpg")
    private String productImage;

    // ===== 系统会话字段 =====
    @Schema(description = "系统标题（系统会话时用）", example = "系统通知")
    private String systemTitle;

    @Schema(description = "系统图标（系统会话时用）", example = "/icons/system.png")
    private String systemIcon;

    // ===== 通用字段 =====
    @Schema(description = "最后一条消息内容预览", example = "您好，这个商品还在吗？")
    private String lastMessage;

    @Schema(description = "最后一条消息类型: 0-文字, 1-图片, 2-通知", example = "0")
    private Integer lastMsgType;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数", example = "3")
    private Integer unreadCount;

    @Schema(description = "是否置顶（系统通知始终置顶）", example = "true")
    private Boolean isPinned;
}
