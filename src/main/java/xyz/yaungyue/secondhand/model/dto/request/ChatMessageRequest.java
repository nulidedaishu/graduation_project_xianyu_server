package xyz.yaungyue.secondhand.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聊天消息发送请求
 */
@Data
@Schema(description = "聊天消息发送请求")
public class ChatMessageRequest {

    @NotNull(message = "接收者ID不能为空")
    @Schema(description = "接收者ID", example = "2", required = true)
    private Long receiverId;

    @Schema(description = "关联商品ID", example = "1")
    private Long productId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容", example = "您好，这个商品还在吗？", required = true)
    private String content;

    @Schema(description = "消息类型(0-文字, 1-图片)", example = "0")
    private Integer msgType;
}
