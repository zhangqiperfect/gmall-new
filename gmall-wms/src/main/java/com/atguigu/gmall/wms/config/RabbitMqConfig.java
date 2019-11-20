package com.atguigu.gmall.wms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZQ
 * @create 2019-11-18 23:27
 */
@Configuration
public class RabbitMqConfig {
    @Bean
    public Exchange exchange() {
        TopicExchange topicExchange = new TopicExchange("WMS-EXCHANGE", true, false);
        return topicExchange;
    }

    @Bean
    public Queue queue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "WMS-EXCHANGE");
        arguments.put("x-dead-letter-routing-key", "wms.ttl");
        arguments.put("x-message-ttl", 90000); // 仅仅用于测试，实际根据需求，通常30分钟或者15分钟
        return new Queue("WMS-TTL-QUEUE", true, false, false, arguments);
    }

    @Bean
    public Binding binding() {
        return new Binding("WMS-TTL-QUEUE", Binding.DestinationType.QUEUE, "WMS-EXCHANGE", "wms.unlock", null);
    }

    @Bean
    public Queue deadQueue() {
        return new Queue("WMS-DEAD-QUEUE", true, false, false);
    }

    @Bean
    public Binding deadBinding() {
        return new Binding("WMS-DEAD-QUEUE", Binding.DestinationType.QUEUE, "WMS-EXCHANGE", "wms.ttl", null);
    }
}
