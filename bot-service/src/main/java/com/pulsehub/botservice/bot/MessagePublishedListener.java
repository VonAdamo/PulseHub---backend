package com.pulsehub.botservice.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessagePublishedListener {

    private static final Logger log = LoggerFactory.getLogger(MessagePublishedListener.class);

    private final ObjectMapper objectMapper;
    private final PulseBotService pulseBotService;

    public MessagePublishedListener(ObjectMapper objectMapper, PulseBotService pulseBotService) {
        this.objectMapper = objectMapper;
        this.pulseBotService = pulseBotService;
    }

    @RabbitListener(queues = "${pulsehub.rabbitmq.queue}")
    public void onMessagePublished(String eventJson) {
        MessagePublishedEvent event = readEvent(eventJson);
        log.info("Received message-published event: eventId={}, messageId={}, username={}, channel={}",
                event.eventId(), event.messageId(), event.username(), event.channel());

        pulseBotService.handle(event);
    }

    private MessagePublishedEvent readEvent(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, MessagePublishedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not parse message-published event", exception);
        }
    }
}
