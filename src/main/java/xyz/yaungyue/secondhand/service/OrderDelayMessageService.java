package xyz.yaungyue.secondhand.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import xyz.yaungyue.secondhand.config.RabbitMQDelayConfig;

/**
 * 订单延时消息服务
 *
 * @author yaung
 * @date 2026-03-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDelayMessageService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送订单延时消息（订单超时关闭）
     *
     * @param orderId 订单ID
     */
    public void sendOrderDelayMessage(Long orderId) {
        try {
            // 设置消息过期时间（TTL）
            MessagePostProcessor messagePostProcessor = message -> {
                message.getMessageProperties().setExpiration(String.valueOf(RabbitMQDelayConfig.ORDER_TIMEOUT_MS));
                return message;
            };

            rabbitTemplate.convertAndSend(
                    RabbitMQDelayConfig.ORDER_DELAY_EXCHANGE,
                    RabbitMQDelayConfig.ORDER_DELAY_ROUTING_KEY,
                    orderId.toString(),
                    messagePostProcessor
            );

            log.info("订单延时消息已发送，orderId={}, 延时{}分钟", orderId, RabbitMQDelayConfig.ORDER_TIMEOUT_MS / 60000);
        } catch (AmqpException e) {
            log.error("发送订单延时消息失败，orderId={}", orderId, e);
            // 抛出异常，让上层决定如何处理（可以降级到定时任务）
            throw e;
        }
    }

    /**
     * 取消订单延时消息
     * 说明：RabbitMQ 不支持直接删除指定消息
     * 实际方案：支付成功后，消息到达死信队列时会被忽略
     *
     * @param orderId 订单ID
     */
    public void cancelOrderDelayMessage(Long orderId) {
        // RabbitMQ 无法直接删除已发送的消息
        // 解决方案：在死信队列消费者中检查订单状态，已支付则忽略
        log.info("订单已支付，orderId={}，超时消息将在死信队列消费时被忽略", orderId);
    }
}
