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
 * 订单超时关闭定时任务
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
     * 每5分钟扫描一次超时订单
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void closeTimeoutOrders() {
        log.info("开始扫描超时订单...");

        // 查询待付款且已过期的订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.PENDING_PAYMENT)
                .lt(Order::getExpireTime, LocalDateTime.now());

        List<Order> timeoutOrders = orderService.list(wrapper);

        if (timeoutOrders.isEmpty()) {
            log.info("没有超时订单需要处理");
            return;
        }

        log.info("发现 {} 个超时订单需要关闭", timeoutOrders.size());

        // 关闭每个超时订单
        for (Order order : timeoutOrders) {
            try {
                orderService.closeOrder(order.getId(), "订单超时未支付，系统自动关闭");
                log.info("已关闭超时订单，orderId={}", order.getId());
            } catch (Exception e) {
                log.error("关闭超时订单失败，orderId={}", order.getId(), e);
            }
        }

        log.info("超时订单扫描完成，共处理 {} 个订单", timeoutOrders.size());
    }
}
