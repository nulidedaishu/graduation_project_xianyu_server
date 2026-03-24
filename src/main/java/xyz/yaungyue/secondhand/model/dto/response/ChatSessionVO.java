package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话视图对象
 */
@Data
@Schema(description = "聊天会话信息")
public class ChatSessionVO {

    @Schema(description = "会话标识", example = "1_2")
    private String sessionKey;

    @Schema(description = "对方用户ID", example = "2")
    private Long otherUserId;

    @Schema(description = "对方用户昵称", example = "李四")
    private String otherUserNickname;

    @Schema(description = "对方用户头像", example = "https://example.com/avatar2.jpg")
    private String otherUserAvatar;

    @Schema(description = "关联商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品标题", example = "iPhone 14 Pro")
    private String productTitle;

    @Schema(description = "商品主图", example = "https://example.com/product.jpg")
    private String productImage;

    @Schema(description = "最后一条消息内容", example = "您好，这个商品还在吗？")
    private String lastMessage;

    @Schema(description = "最后一条消息类型(0-文字, 1-图片)", example = "0")
    private Integer lastMsgType;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数量", example = "3")
    private Integer unreadCount;
}
