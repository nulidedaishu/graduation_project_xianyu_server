package xyz.yaungyue.secondhand.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.yaungyue.secondhand.model.dto.response.MessagePushEvent;
import xyz.yaungyue.secondhand.service.MessagePushService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SSE消息推送服务实现类
 *
 * @author yaung
 * @date 2026-03-22
 */
@Slf4j
@Service
public class MessagePushServiceImpl implements MessagePushService {

    /**
     * 用户SSE连接池：userId -> List<SseEmitter>
     */
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    /**
     * 用户连接计数器（用于限制同一用户的并发连接数）
     */
    private final Map<Long, Integer> userConnectionCount = new ConcurrentHashMap<>();

    /**
     * 全局序列号生成器
     */
    private final AtomicLong sequenceGenerator = new AtomicLong(0);

    /**
     * 心跳定时任务执行器
     */
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);

    /**
     * 连接超时时间：30分钟
     */
    private static final long CONNECTION_TIMEOUT = 30 * 60 * 1000L;

    /**
     * 同一用户最大并发连接数
     */
    private static final int MAX_CONNECTIONS_PER_USER = 3;

    /**
     * 心跳间隔：30秒
     */
    private static final long HEARTBEAT_INTERVAL = 30 * 1000L;

    public MessagePushServiceImpl() {
        // 启动全局心跳任务
        startGlobalHeartbeat();
    }

    @Override
    public SseEmitter subscribe(Long userId) {
        // 检查并发连接数限制
        int currentCount = userConnectionCount.getOrDefault(userId, 0);
        if (currentCount >= MAX_CONNECTIONS_PER_USER) {
            log.warn("用户 {} 的SSE连接数已达到上限 {}", userId, MAX_CONNECTIONS_PER_USER);
            // 移除最旧的连接
            removeOldestConnection(userId);
        }

        // 创建SseEmitter，设置超时时间
        SseEmitter emitter = new SseEmitter(CONNECTION_TIMEOUT);

        // 更新连接计数
        userConnectionCount.put(userId, userConnectionCount.getOrDefault(userId, 0) + 1);

        // 存储连接到连接池
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 配置连接回调
        emitter.onCompletion(() -> {
            log.debug("SSE连接完成: userId={}", userId);
            removeConnection(userId, emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE连接超时: userId={}", userId);
            removeConnection(userId, emitter);
        });

        emitter.onError((e) -> {
            log.error("SSE连接错误: userId={}, error={}", userId, e.getMessage());
            removeConnection(userId, emitter);
        });

        // 发送连接确认消息
        try {
            MessagePushEvent connectEvent = MessagePushEvent.systemEvent("connected", userId);
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(connectEvent.getSequence()))
                    .name("connect")
                    .data(connectEvent));
            log.info("用户 {} SSE连接建立成功", userId);
        } catch (IOException e) {
            log.error("发送连接确认消息失败: userId={}", userId, e);
            removeConnection(userId, emitter);
        }

        return emitter;
    }

    @Override
    public void disconnect(Long userId) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null && !emitters.isEmpty()) {
            // 断开该用户的所有连接
            for (SseEmitter emitter : new ArrayList<>(emitters)) {
                removeConnection(userId, emitter);
            }
            log.info("用户 {} 的所有SSE连接已手动断开", userId);
        }
    }

    @Override
    @Async("ssePushExecutor")
    public void pushToUser(Long userId, MessagePushEvent event) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("用户 {} 不在线，消息推送失败", userId);
            return;
        }

        for (SseEmitter emitter : new ArrayList<>(emitters)) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(event.getSequence()))
                        .name(event.getEventType())
                        .data(event));
                log.debug("消息推送成功: userId={}, eventType={}, sequence={}",
                        userId, event.getEventType(), event.getSequence());
            } catch (IOException e) {
                log.error("消息推送失败: userId={}, eventType={}, error={}",
                        userId, event.getEventType(), e.getMessage());
                removeConnection(userId, emitter);
            }
        }
    }

    @Override
    @Async("ssePushExecutor")
    public void broadcast(MessagePushEvent event) {
        userEmitters.forEach((userId, emitters) -> {
            for (SseEmitter emitter : new ArrayList<>(emitters)) {
                try {
                    emitter.send(SseEmitter.event()
                            .id(String.valueOf(event.getSequence()))
                            .name(event.getEventType())
                            .data(event));
                } catch (IOException e) {
                    log.error("广播消息失败: userId={}, error={}", userId, e.getMessage());
                    removeConnection(userId, emitter);
                }
            }
        });
    }

    @Override
    public int getConnectionCount() {
        return userEmitters.values().stream().mapToInt(List::size).sum();
    }

    @Override
    public boolean isOnline(Long userId) {
        return userEmitters.containsKey(userId);
    }

    /**
     * 生成下一个序列号
     */
    public Long nextSequence() {
        return sequenceGenerator.incrementAndGet();
    }

    /**
     * 移除连接
     */
    private void removeConnection(Long userId, SseEmitter emitter) {
        // 从连接池移除
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }

        // 更新连接计数
        Integer count = userConnectionCount.get(userId);
        if (count != null) {
            if (count <= 1) {
                userConnectionCount.remove(userId);
            } else {
                userConnectionCount.put(userId, count - 1);
            }
        }

        // 关闭emitter，完全静默处理所有异常
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // 连接可能已关闭，忽略所有异常
        }
    }

    /**
     * 移除最旧的连接（当超过并发限制时）
     */
    private void removeOldestConnection(Long userId) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null && !emitters.isEmpty()) {
            // 移除列表中的第一个（最旧的）连接
            SseEmitter oldestEmitter = emitters.get(0);
            removeConnection(userId, oldestEmitter);
            log.info("用户 {} 最旧的SSE连接已被移除", userId);
        }
    }

    /**
     * 启动全局心跳任务
     */
    private void startGlobalHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                MessagePushEvent pingEvent = MessagePushEvent.pingEvent();
                // 遍历所有用户的连接列表
                for (Map.Entry<Long, List<SseEmitter>> entry : userEmitters.entrySet()) {
                    Long userId = entry.getKey();
                    List<SseEmitter> emitters = entry.getValue();
                    // 创建副本避免并发修改问题
                    for (SseEmitter emitter : new ArrayList<>(emitters)) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("ping")
                                    .data(pingEvent));
                        } catch (Exception e) {
                            // 捕获所有异常，包括IOException和AsyncRequestNotUsableException
                            log.debug("心跳发送失败，移除连接: userId={}, error={}", userId, e.getMessage());
                            removeConnection(userId, emitter);
                        }
                    }
                }
                log.debug("全局心跳发送完成，当前连接数: {}", getConnectionCount());
            } catch (Exception e) {
                log.error("全局心跳任务异常", e);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);

        log.info("SSE全局心跳任务已启动，心跳间隔: {}ms", HEARTBEAT_INTERVAL);
    }
}
