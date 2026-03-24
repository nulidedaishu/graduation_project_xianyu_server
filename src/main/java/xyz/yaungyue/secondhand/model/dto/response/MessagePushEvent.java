package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息推送事件
 * 用于SSE实时推送的消息格式
 *
 * @author yaung
 * @date 2026-03-22
 */
@Data
@Builder
@Schema(description = "消息推送事件")
public class MessagePushEvent {

    @Schema(description = "事件类型: chat-聊天消息, notice-系统通知, order-订单消息, system-系统事件", example = "chat")
    private String eventType;

    @Schema(description = "事件数据，根据eventType不同，数据类型不同")
    private Object data;

    @Schema(description = "服务端时间戳，用于排序和去重", example = "1711094400000")
    private Long serverTime;

    @Schema(description = "递增序列号，用于去重和保证顺序", example = "1001")
    private Long sequence;

    @Schema(description = "推送目标用户ID", example = "1")
    private Long targetUserId;

    /**
     * 创建聊天消息事件
     */
    public static MessagePushEvent chatEvent(Object chatMessage, Long sequence, Long targetUserId) {
        return MessagePushEvent.builder()
                .eventType("chat")
                .data(chatMessage)
                .serverTime(System.currentTimeMillis())
                .sequence(sequence)
                .targetUserId(targetUserId)
                .build();
    }

    /**
     * 创建通知消息事件
     */
    public static MessagePushEvent noticeEvent(Object notice, Long sequence, Long targetUserId) {
        return MessagePushEvent.builder()
                .eventType("notice")
                .data(notice)
                .serverTime(System.currentTimeMillis())
                .sequence(sequence)
                .targetUserId(targetUserId)
                .build();
    }

    /**
     * 创建订单消息事件
     */
    public static MessagePushEvent orderEvent(Object orderData, Long sequence, Long targetUserId) {
        return MessagePushEvent.builder()
                .eventType("order")
                .data(orderData)
                .serverTime(System.currentTimeMillis())
                .sequence(sequence)
                .targetUserId(targetUserId)
                .build();
    }

    /**
     * 创建系统消息事件（如心跳、连接确认等）
     */
    public static MessagePushEvent systemEvent(String message, Long targetUserId) {
        return MessagePushEvent.builder()
                .eventType("system")
                .data(message)
                .serverTime(System.currentTimeMillis())
                .sequence(0L)
                .targetUserId(targetUserId)
                .build();
    }

    /**
     * 创建心跳事件
     */
    public static MessagePushEvent pingEvent() {
        return MessagePushEvent.builder()
                .eventType("ping")
                .data("pong")
                .serverTime(System.currentTimeMillis())
                .sequence(0L)
                .targetUserId(null)
                .build();
    }
}
