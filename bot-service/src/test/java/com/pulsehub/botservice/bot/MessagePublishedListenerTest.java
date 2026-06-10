package com.pulsehub.botservice.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessagePublishedListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final PulseBotService pulseBotService = mock(PulseBotService.class);
    private final MessagePublishedListener listener = new MessagePublishedListener(objectMapper, pulseBotService);

    @Test
    void onMessagePublishedParsesEventAndPassesItToPulseBotService() throws Exception {
        MessagePublishedEvent event = new MessagePublishedEvent(
                UUID.randomUUID(),
                "message-published",
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "adam",
                "general",
                "hej bot",
                Instant.parse("2026-06-10T10:15:30Z")
        );

        listener.onMessagePublished(objectMapper.writeValueAsString(event));

        verify(pulseBotService).handle(event);
    }
}
