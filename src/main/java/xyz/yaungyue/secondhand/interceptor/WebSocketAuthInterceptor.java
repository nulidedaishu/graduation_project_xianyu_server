package xyz.yaungyue.secondhand.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 认证拦截器
 * 处理 WebSocket 连接时的身份验证
 *
 * @author yaung
 * @date 2026-03-27
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor, ChannelInterceptor {

    private static final String USER_ID_KEY = "userId";

    /**
     * WebSocket 握手前拦截
     * 从 URL 参数中获取 token 并进行验证
     */
    @Override
    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                   org.springframework.http.server.ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // 获取 token 参数
        String query = request.getURI().getQuery();
        String token = null;

        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    token = param.substring(6);
                    break;
                }
            }
        }

        if (token == null || token.isEmpty()) {
            log.warn("WebSocket 连接失败: 缺少 token 参数");
            return false;
        }

        try {
            // 使用 Sa-Token 验证 token
            StpUtil.setTokenValue(token);
            long userId = StpUtil.getLoginIdAsLong();

            // 将 userId 存入 attributes，后续使用
            attributes.put(USER_ID_KEY, userId);

            log.info("WebSocket 握手成功: userId={}", userId);
            return true;
        } catch (Exception e) {
            log.warn("WebSocket 连接认证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                               org.springframework.http.server.ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 握手后的处理，不需要额外操作
    }

    /**
     * STOMP 消息通道拦截
     * 在消息发送到控制器前进行身份验证
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // 处理 CONNECT 命令
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从 session attributes 获取 userId
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null && sessionAttributes.containsKey(USER_ID_KEY)) {
                Long userId = (Long) sessionAttributes.get(USER_ID_KEY);

                // 设置用户身份，用于点对点消息
                accessor.setUser(() -> String.valueOf(userId));

                log.info("STOMP CONNECT 成功: userId={}", userId);
            } else {
                log.warn("STOMP CONNECT 失败: 无法获取 userId");
                return null; // 拒绝连接
            }
        }

        return message;
    }
}
