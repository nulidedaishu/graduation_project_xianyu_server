package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.service.MessagePushService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * SSE消息推送控制器
 *
 * @author yaung
 * @date 2026-03-22
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "消息推送", description = "SSE实时消息推送相关接口")
public class MessagePushController {

    private final MessagePushService messagePushService;

    /**
     * 建立SSE连接
     * 支持Last-Event-ID重连机制
     * EventSource不支持自定义header，通过cookie进行身份验证
     *
     * @param lastEventId 上次接收到的最后事件ID（用于重连时恢复消息）
     * @return SseEmitter
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "建立SSE连接", description = "建立SSE实时消息推送连接，支持自动重连")
    public SseEmitter subscribe(
            @Parameter(description = "上次接收的最后事件ID，用于断线重连", example = "100")
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            @Parameter(description = "备选：从URL参数获取token（当cookie不可用时）")
            @RequestParam(name = "token", required = false) String tokenParam,
            HttpServletRequest request) {

        Long userId;
        try {
            // 优先从cookie读取token
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 如果cookie登录失败，尝试从token参数登录
            if (tokenParam != null && !tokenParam.isEmpty()) {
                try {
                    // 设置token到当前上下文
                    StpUtil.setTokenValue(tokenParam);
                    // 验证token并获取登录ID
                    userId = StpUtil.getLoginIdAsLong();
                } catch (Exception ex) {
                    log.warn("SSE连接token认证失败: {}", ex.getMessage());
                    return createErrorEmitter("未登录");
                }
            } else {
                log.warn("SSE连接认证失败: {}", e.getMessage());
                return createErrorEmitter("未登录");
            }
        }

        log.info("用户 {} 建立SSE连接, Last-Event-ID={}", userId, lastEventId);

        // 如果提供了Last-Event-ID，可以在这里处理离线期间的消息恢复
        if (lastEventId != null && !lastEventId.isEmpty()) {
            try {
                long lastSeq = Long.parseLong(lastEventId);
                log.debug("用户 {} 重连，需要恢复序列号 {} 之后的消息", userId, lastSeq);
            } catch (NumberFormatException e) {
                log.warn("无效的Last-Event-ID: {}", lastEventId);
            }
        }

        return messagePushService.subscribe(userId);
    }

    /**
     * 创建错误Emitter
     */
    private SseEmitter createErrorEmitter(String message) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(message));
            emitter.complete();
        } catch (IOException ioException) {
            // ignore
        }
        return emitter;
    }

    /**
     * 断开SSE连接
     */
    @PostMapping("/disconnect")
    @SaCheckPermission(value = "user:message:disconnect", type = "user")
    @Operation(summary = "断开SSE连接", description = "手动断开SSE连接")
    public ApiResponse<Void> disconnect() {
        Long userId = StpUtil.getLoginIdAsLong();
        messagePushService.disconnect(userId);
        return ApiResponse.success();
    }

    /**
     * 获取连接状态
     */
    @GetMapping("/connection-status")
    @SaCheckPermission(value = "user:message:status", type = "user")
    @Operation(summary = "获取连接状态", description = "检查当前用户的SSE连接状态")
    public ApiResponse<ConnectionStatusVO> getConnectionStatus() {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean online = messagePushService.isOnline(userId);

        ConnectionStatusVO vo = new ConnectionStatusVO();
        vo.setUserId(userId);
        vo.setConnected(online);

        return ApiResponse.success(vo);
    }

    /**
     * 获取全局连接统计（管理员接口）
     */
    @GetMapping("/admin/connection-stats")
    @SaCheckPermission(value = "admin:connection:stats", type = "admin")
    @Operation(summary = "连接统计", description = "获取SSE连接统计信息（管理员）")
    public ApiResponse<ConnectionStatsVO> getConnectionStats() {
        ConnectionStatsVO vo = new ConnectionStatsVO();
        vo.setActiveConnections(messagePushService.getConnectionCount());
        return ApiResponse.success(vo);
    }

    /**
     * 连接状态VO
     */
    public static class ConnectionStatusVO {
        private Long userId;
        private boolean connected;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }
    }

    /**
     * 连接统计VO
     */
    public static class ConnectionStatsVO {
        private int activeConnections;

        public int getActiveConnections() {
            return activeConnections;
        }

        public void setActiveConnections(int activeConnections) {
            this.activeConnections = activeConnections;
        }
    }
}
