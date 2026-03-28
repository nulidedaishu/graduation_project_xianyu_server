package xyz.yaungyue.secondhand.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.config.RabbitMQDelayConfig;
import xyz.yaungyue.secondhand.constant.OrderStatus;
import xyz.yaungyue.secondhand.model.entity.Order;
import xyz.yaungyue.secondhand.service.OrderService;

/**
 * 订单超时消费者（监听死信队列）
 *
 * @author yaung
 * @date 2026-03-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderService orderService;

    /**
     * 监听死信队列，处理超时订单
     * 消息在延时队列中停留30分钟后，自动进入死信队列
     *
     * @param orderIdStr 订单ID（字符串）
     */
    @RabbitListener(queues = RabbitMQDelayConfig.ORDER_DEAD_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderTimeout(String orderIdStr) {
        Long orderId = Long.valueOf(orderIdStr);
        log.info("收到订单超时消息，orderId={}", orderId);

        try {
            // 1. 查询订单最新状态
            Order order = orderService.getById(orderId);

            if (order == null) {
                log.warn("订单不存在，orderId={}", orderId);
                return;
            }

            // 2. 幂等性检查：只有待付款状态才关闭订单
            // 如果订单已支付，说明支付回调已处理，直接忽略此消息
            if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT)) {
                log.info("订单状态已变更，无需关闭，orderId={}, status={}",
                        orderId, order.getStatus());
                return;
            }

            // 3. 关闭超时订单
            orderService.closeOrder(orderId, "订单超时未支付，系统自动关闭");
            log.info("超时订单已关闭，orderId={}", orderId);

        } catch (Exception e) {
            log.error("处理订单超时消息失败，orderId={}", orderId, e);
            // 抛出异常，消息会重新入队（根据RabbitMQ重试配置）
            throw e;
        }
    }
}
