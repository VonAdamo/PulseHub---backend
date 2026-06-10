package com.pulsehub.botservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    TopicExchange messageEventsExchange(
            @Value("${pulsehub.rabbitmq.exchange}") String exchangeName
    ) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue messagePublishedQueue(
            @Value("${pulsehub.rabbitmq.queue}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding messagePublishedBinding(
            Queue messagePublishedQueue,
            TopicExchange messageEventsExchange,
            @Value("${pulsehub.rabbitmq.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(messagePublishedQueue)
                .to(messageEventsExchange)
                .with(routingKey);
    }
}
