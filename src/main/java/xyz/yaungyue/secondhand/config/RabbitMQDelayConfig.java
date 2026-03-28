package xyz.yaungyue.secondhand.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 延时队列配置（死信队列实现）
 *
 * @author yaung
 * @date 2026-03-27
 */
@Configuration
public class RabbitMQDelayConfig {

    // ==================== 订单超时关闭配置 ====================

    // 订单延时队列（消息在此等待过期）
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    // 死信队列（过期消息进入此队列，消费者监听此队列处理超时订单）
    public static final String ORDER_DEAD_QUEUE = "order.dead.queue";
    public static final String ORDER_DEAD_EXCHANGE = "order.dead.exchange";
    public static final String ORDER_DEAD_ROUTING_KEY = "order.dead";

    // 订单超时时间：30分钟（毫秒）
    public static final long ORDER_TIMEOUT_MS = 30 * 60 * 1000;

    /**
     * 延时交换机
     */
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(ORDER_DELAY_EXCHANGE);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange orderDeadExchange() {
        return new DirectExchange(ORDER_DEAD_EXCHANGE);
    }

    /**
     * 延时队列（设置死信参数）
     * 消息过期后，会自动发送到死信交换机
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 消息过期后，发送到死信交换机
        args.put("x-dead-letter-exchange", ORDER_DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", ORDER_DEAD_ROUTING_KEY);
        // 可选：设置队列默认TTL（这里不设置，由消息单独指定，更灵活）
        return QueueBuilder.durable(ORDER_DELAY_QUEUE).withArguments(args).build();
    }

    /**
     * 死信队列（消费者监听此队列处理超时订单）
     */
    @Bean
    public Queue orderDeadQueue() {
        return QueueBuilder.durable(ORDER_DEAD_QUEUE).build();
    }

    /**
     * 绑定：延时队列 -> 延时交换机
     */
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderDelayExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }

    /**
     * 绑定：死信队列 -> 死信交换机
     */
    @Bean
    public Binding orderDeadBinding() {
        return BindingBuilder.bind(orderDeadQueue())
                .to(orderDeadExchange())
                .with(ORDER_DEAD_ROUTING_KEY);
    }
}
