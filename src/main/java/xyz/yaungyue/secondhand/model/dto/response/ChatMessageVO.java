package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息视图对象
 */
@Data
@Schema(description = "聊天消息信息")
public class ChatMessageVO {

    @Schema(description = "消息ID", example = "1")
    private Long id;

    @Schema(description = "发送者ID", example = "1")
    private Long senderId;

    @Schema(description = "发送者昵称", example = "张三")
    private String senderNickname;

    @Schema(description = "发送者头像", example = "https://example.com/avatar.jpg")
    private String senderAvatar;

    @Schema(description = "接收者ID", example = "2")
    private Long receiverId;

    @Schema(description = "接收者昵称", example = "李四")
    private String receiverNickname;

    @Schema(description = "接收者头像", example = "https://example.com/avatar2.jpg")
    private String receiverAvatar;

    @Schema(description = "关联商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品标题", example = "iPhone 14 Pro")
    private String productTitle;

    @Schema(description = "商品主图", example = "https://example.com/product.jpg")
    private String productImage;

    @Schema(description = "消息内容", example = "您好，这个商品还在吗？")
    private String content;

    @Schema(description = "消息类型(0-文字, 1-图片)", example = "0")
    private Integer msgType;

    @Schema(description = "消息类型描述", example = "文字")
    private String msgTypeDesc;

    @Schema(description = "是否已读(0-未读, 1-已读)", example = "0")
    private Integer isRead;

    @Schema(description = "会话标识", example = "1_2")
    private String sessionKey;

    @Schema(description = "发送时间")
    private LocalDateTime createTime;
}
