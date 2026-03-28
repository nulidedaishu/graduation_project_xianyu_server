package xyz.yaungyue.secondhand.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.constant.OrderStatus;
import xyz.yaungyue.secondhand.model.entity.Order;
import xyz.yaungyue.secondhand.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时关闭定时任务（兜底方案）
 * <p>
 * 主要超时处理由 RabbitMQ 延时队列完成，此定时任务作为兜底方案，
 * 防止 RabbitMQ 故障或消息丢失导致订单无法关闭。
 *
 * @author yaung
 * @date 2026-02-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final OrderService orderService;

    /**
     * 每30分钟扫描一次超时订单（兜底方案）
     * 正常情况下，RabbitMQ 死信队列会实时处理超时订单
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void closeTimeoutOrders() {
        log.info("【兜底】开始扫描超时订单...");

        // 查询待付款且已过期的订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.PENDING_PAYMENT)
                .lt(Order::getExpireTime, LocalDateTime.now());

        List<Order> timeoutOrders = orderService.list(wrapper);

        if (timeoutOrders.isEmpty()) {
            log.info("【兜底】没有超时订单需要处理");
            return;
        }

        log.info("【兜底】发现 {} 个超时订单需要关闭", timeoutOrders.size());

        // 关闭每个超时订单
        for (Order order : timeoutOrders) {
            try {
                orderService.closeOrder(order.getId(), "订单超时未支付，系统自动关闭（兜底）");
                log.info("【兜底】已关闭超时订单，orderId={}", order.getId());
            } catch (Exception e) {
                log.error("【兜底】关闭超时订单失败，orderId={}", order.getId(), e);
            }
        }

        log.info("【兜底】超时订单扫描完成，共处理 {} 个订单", timeoutOrders.size());
    }
}
