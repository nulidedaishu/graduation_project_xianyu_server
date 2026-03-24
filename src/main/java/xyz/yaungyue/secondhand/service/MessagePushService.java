package xyz.yaungyue.secondhand.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.yaungyue.secondhand.model.dto.response.MessagePushEvent;

/**
 * SSE消息推送服务接口
 *
 * @author yaung
 * @date 2026-03-22
 */
public interface MessagePushService {

    /**
     * 订阅SSE连接
     *
     * @param userId 用户ID
     * @return SseEmitter实例
     */
    SseEmitter subscribe(Long userId);

    /**
     * 断开SSE连接
     *
     * @param userId 用户ID
     */
    void disconnect(Long userId);

    /**
     * 向指定用户推送消息
     *
     * @param userId 目标用户ID
     * @param event  消息事件
     */
    void pushToUser(Long userId, MessagePushEvent event);

    /**
     * 向所有在线用户广播消息
     *
     * @param event 消息事件
     */
    void broadcast(MessagePushEvent event);

    /**
     * 获取用户连接数（用于监控）
     *
     * @return 当前连接数
     */
    int getConnectionCount();

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isOnline(Long userId);

    /**
     * 生成下一个序列号
     *
     * @return 递增的序列号
     */
    Long nextSequence();
}
