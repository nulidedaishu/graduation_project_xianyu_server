package xyz.yaungyue.secondhand.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import xyz.yaungyue.secondhand.interceptor.WebSocketAuthInterceptor;

/**
 * WebSocket 配置类
 * 配置 STOMP 消息代理和端点
 *
 * @author yaung
 * @date 2026-03-27
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * 配置消息代理
     * /topic - 广播消息前缀
     * /queue - 点对点消息前缀
     * /user - 用户特定消息前缀
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单的内存消息代理
        // /topic 用于广播，/queue 用于点对点
        registry.enableSimpleBroker("/topic", "/queue");

        // 设置应用前缀，客户端发送消息时需要加上此前缀
        // 例如: /app/chat/send
        registry.setApplicationDestinationPrefixes("/app");

        // 设置用户目标前缀，用于点对点消息
        // 例如: /user/queue/messages
        registry.setUserDestinationPrefix("/user");

        log.info("WebSocket 消息代理配置完成");
    }

    /**
     * 注册 STOMP 端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册端点 /ws/chat，客户端通过此端点建立 WebSocket 连接
        registry.addEndpoint("/ws/chat")
                // 允许跨域，生产环境应配置具体域名
                .setAllowedOriginPatterns("*")
                // 添加自定义拦截器进行身份验证
                .addInterceptors(webSocketAuthInterceptor);

        log.info("WebSocket STOMP 端点 /ws/chat 注册完成");
    }

    /**
     * 配置客户端入站通道
     * 添加自定义拦截器进行身份验证
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
