package com.pulsehub.messageservice.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessageEventPublisherTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final MessageEventPublisher publisher = new MessageEventPublisher(
            rabbitTemplate,
            objectMapper,
            "pulsehub.messages",
            "message.published"
    );

    @Test
    void publishesMessagePublishedEventAsJson() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-06-09T10:00:00Z");
        Message message = new Message(senderId, "milla", "general", "Hej");
        ReflectionTestUtils.setField(message, "id", messageId);
        ReflectionTestUtils.setField(message, "createdAt", createdAt);

        publisher.publishMessagePublished(message);

        var jsonCaptor = forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("pulsehub.messages"),
                org.mockito.ArgumentMatchers.eq("message.published"),
                jsonCaptor.capture()
        );

        JsonNode event = objectMapper.readTree(jsonCaptor.getValue());
        assertThat(event.get("eventId").asText()).isNotBlank();
        assertThat(event.get("type").asText()).isEqualTo("message-published");
        assertThat(event.get("messageId").asText()).isEqualTo(messageId.toString());
        assertThat(event.get("senderId").asText()).isEqualTo(senderId.toString());
        assertThat(event.get("username").asText()).isEqualTo("milla");
        assertThat(event.get("channel").asText()).isEqualTo("general");
        assertThat(event.get("content").asText()).isEqualTo("Hej");
        assertThat(event.get("createdAt").asText()).isEqualTo(createdAt.toString());
    }
}
