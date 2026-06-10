package com.pulsehub.botservice.bot;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PulseBotServiceTest {

    private final MessageServiceClient messageServiceClient = mock(MessageServiceClient.class);
    private final PulseBotService pulseBotService = new PulseBotService(messageServiceClient);

    @Test
    void handleCreatesBotResponseForHelpCommand() {
        MessagePublishedEvent event = event("adam", "Kan jag få /help?", "general");

        pulseBotService.handle(event);

        ArgumentCaptor<MessageCreateRequest> requestCaptor = ArgumentCaptor.forClass(MessageCreateRequest.class);
        verify(messageServiceClient).createMessage(requestCaptor.capture());

        MessageCreateRequest request = requestCaptor.getValue();
        assertThat(request.senderId()).isEqualTo(PulseBotService.BOT_SENDER_ID);
        assertThat(request.username()).isEqualTo(PulseBotService.BOT_USERNAME);
        assertThat(request.channel()).isEqualTo("general");
        assertThat(request.content()).isEqualTo(PulseBotService.BOT_RESPONSE);
    }

    @Test
    void handleCreatesBotResponseForHejBot() {
        pulseBotService.handle(event("adam", "hej bot", "general"));

        verify(messageServiceClient).createMessage(new MessageCreateRequest(
                PulseBotService.BOT_SENDER_ID,
                PulseBotService.BOT_USERNAME,
                "general",
                PulseBotService.BOT_RESPONSE
        ));
    }

    @Test
    void handleIgnoresPulseBotEventsToAvoidLoop() {
        pulseBotService.handle(event("PulseBot", "hej bot", "general"));

        verify(messageServiceClient, never()).createMessage(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void handleIgnoresMessagesWithoutTrigger() {
        pulseBotService.handle(event("adam", "vanligt meddelande", "general"));

        verify(messageServiceClient, never()).createMessage(org.mockito.ArgumentMatchers.any());
    }

    private MessagePublishedEvent event(String username, String content, String channel) {
        return new MessagePublishedEvent(
                UUID.randomUUID(),
                "message-published",
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                username,
                channel,
                content,
                Instant.now()
        );
    }
}
