package com.pulsehub.messageservice.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;

    public MessageEventPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${pulsehub.rabbitmq.messages.exchange}") String exchange,
            @Value("${pulsehub.rabbitmq.messages.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishMessagePublished(Message message) {
        try {
            String eventJson = objectMapper.writeValueAsString(MessagePublishedEvent.from(message));
            rabbitTemplate.convertAndSend(exchange, routingKey, eventJson);
        } catch (JsonProcessingException | AmqpException exception) {
            throw new MessageEventPublishException("Could not publish message-published event", exception);
        }
    }
}
