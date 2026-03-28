package xyz.yaungyue.secondhand.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import xyz.yaungyue.secondhand.model.dto.request.ChatMessageRequest;
import xyz.yaungyue.secondhand.model.dto.response.ChatMessageVO;
import xyz.yaungyue.secondhand.model.entity.ChatMessage;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.ChatMessageService;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.service.ProductService;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket 聊天控制器
 * 处理 STOMP 消息的发送和接收
 *
 * @author yaung
 * @date 2026-03-27
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final ProductService productService;

    /**
     * 接收客户端发送的聊天消息
     * 客户端发送消息到: /app/chat/send
     *
     * @param request   消息请求
     * @param principal 当前用户身份
     */
    @MessageMapping("/chat/send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        log.debug("收到聊天消息: senderId={}, receiverId={}, content={}",
                senderId, request.getReceiverId(), request.getContent());

        try {
            // 不能给自己发送消息
            if (senderId.equals(request.getReceiverId())) {
                throw new RuntimeException("不能给自己发送消息");
            }

            // 检查接收者是否存在
            User receiver = userService.getById(request.getReceiverId());
            if (receiver == null) {
                throw new RuntimeException("接收者不存在");
            }

            // 生成会话标识 (较小的ID在前)
            String sessionKey = generateSessionKey(senderId, request.getReceiverId());

            // 创建消息
            ChatMessage message = new ChatMessage();
            message.setSenderId(senderId);
            message.setReceiverId(request.getReceiverId());
            message.setProductId(request.getProductId());
            message.setContent(request.getContent());
            message.setMsgType(request.getMsgType() != null ? request.getMsgType() : 0);
            message.setIsRead(0);
            message.setSessionKey(sessionKey);

            boolean success = chatMessageService.save(message);
            if (!success) {
                throw new RuntimeException("保存消息失败");
            }

            // 构建响应 VO
            ChatMessageVO response = convertToVO(message);

            // 发送给接收者 (/user/queue/messages)
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(request.getReceiverId()),
                    "/queue/messages",
                    response
            );

            // 发送回发送者，确认消息已发送
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(senderId),
                    "/queue/messages",
                    response
            );

            log.debug("聊天消息发送成功: messageId={}", message.getId());
        } catch (Exception e) {
            log.error("发送聊天消息失败: {}", e.getMessage(), e);

            // 发送错误消息给发送者
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(senderId),
                    "/queue/errors",
                    "发送消息失败: " + e.getMessage()
            );
        }
    }

    /**
     * 处理订阅请求
     * 当客户端订阅 /user/queue/messages 时触发
     */
    @SubscribeMapping("/user/queue/messages")
    public void handleSubscription(Principal principal) {
        log.info("用户订阅消息队列: userId={}", principal.getName());
    }

    /**
     * 心跳检测（可选）
     * 客户端可以定期发送心跳到 /app/chat/ping
     */
    @MessageMapping("/chat/ping")
    public void handlePing(Principal principal) {
        log.debug("收到心跳: userId={}", principal.getName());

        // 回复 pong
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/pong",
                LocalDateTime.now().toString()
        );
    }

    /**
     * 生成会话标识
     */
    private String generateSessionKey(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    /**
     * 转换为VO
     */
    private ChatMessageVO convertToVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setProductId(message.getProductId());
        vo.setContent(message.getContent());
        vo.setMsgType(message.getMsgType());
        vo.setIsRead(message.getIsRead());
        vo.setSessionKey(message.getSessionKey());
        vo.setCreateTime(message.getCreateTime());

        // 消息类型描述
        String msgTypeDesc = message.getMsgType() != null && message.getMsgType() == 1 ? "图片" : "文字";
        vo.setMsgTypeDesc(msgTypeDesc);

        // 查询发送者信息
        User sender = userService.getById(message.getSenderId());
        if (sender != null) {
            vo.setSenderNickname(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        // 查询接收者信息
        User receiver = userService.getById(message.getReceiverId());
        if (receiver != null) {
            vo.setReceiverNickname(receiver.getNickname());
            vo.setReceiverAvatar(receiver.getAvatar());
        }

        // 查询商品信息
        if (message.getProductId() != null) {
            var product = productService.getById(message.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                vo.setProductImage(product.getMainImage());
            }
        }

        return vo;
    }
}
