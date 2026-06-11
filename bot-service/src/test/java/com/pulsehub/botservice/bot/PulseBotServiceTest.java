package com.pulsehub.botservice.bot;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PulseBotServiceTest {

    @Test
    void handleCreatesBotResponseForHelpCommand() {
        RecordingMessageServiceClient messageServiceClient = new RecordingMessageServiceClient(0L);
        PulseBotService pulseBotService = new PulseBotService(messageServiceClient, new NoopUserServiceClient());

        pulseBotService.handle(event("adam", "Kan jag fa /help?", "general"));

        assertThat(messageServiceClient.lastRequest).isEqualTo(new MessageCreateRequest(
                PulseBotService.BOT_SENDER_ID,
                PulseBotService.BOT_USERNAME,
                "general",
                PulseBotService.HELP_RESPONSE
        ));
    }

    @Test
    void handleCreatesBotResponseForAboutCommand() {
        RecordingMessageServiceClient messageServiceClient = new RecordingMessageServiceClient(0L);
        PulseBotService pulseBotService = new PulseBotService(messageServiceClient, new NoopUserServiceClient());

        pulseBotService.handle(event("adam", "Kan du beratta om /about?", "general"));

        assertThat(messageServiceClient.lastRequest).isEqualTo(new MessageCreateRequest(
                PulseBotService.BOT_SENDER_ID,
                PulseBotService.BOT_USERNAME,
                "general",
                PulseBotService.ABOUT_RESPONSE
        ));
    }

    @Test
    void handleCreatesMeResponseWithUserDetailsAndMessageCount() {
        UUID senderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        RecordingMessageServiceClient messageServiceClient = new RecordingMessageServiceClient(12L);
        StubUserServiceClient userServiceClient = new StubUserServiceClient(new UserResponse(
                senderId,
                "adam",
                "Adam",
                Instant.parse("2026-06-01T10:00:00Z"),
                Instant.parse("2026-06-01T10:00:00Z")
        ));
        PulseBotService pulseBotService = new PulseBotService(messageServiceClient, userServiceClient);

        pulseBotService.handle(new MessagePublishedEvent(
                UUID.randomUUID(),
                "message-published",
                UUID.randomUUID(),
                senderId.toString(),
                "adam",
                "general",
                "/me",
                Instant.now()
        ));

        assertThat(messageServiceClient.lastRequest.content())
                .contains("adam (Adam)")
                .contains("Created: 2026-06-01 / 10:00")
                .contains("12");
    }

    @Test
    void handleIgnoresPulseBotEventsToAvoidLoop() {
        RecordingMessageServiceClient messageServiceClient = new RecordingMessageServiceClient(0L);
        PulseBotService pulseBotService = new PulseBotService(messageServiceClient, new NoopUserServiceClient());

        pulseBotService.handle(event("PulseBot", "hej bot", "general"));

        assertThat(messageServiceClient.lastRequest).isNull();
    }

    @Test
    void handleIgnoresMessagesWithoutTrigger() {
        RecordingMessageServiceClient messageServiceClient = new RecordingMessageServiceClient(0L);
        PulseBotService pulseBotService = new PulseBotService(messageServiceClient, new NoopUserServiceClient());

        pulseBotService.handle(event("adam", "vanligt meddelande", "general"));

        assertThat(messageServiceClient.lastRequest).isNull();
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

    private static final class RecordingMessageServiceClient extends MessageServiceClient {
        private final long sentMessagesCount;
        private MessageCreateRequest lastRequest;

        private RecordingMessageServiceClient(long sentMessagesCount) {
            super(null);
            this.sentMessagesCount = sentMessagesCount;
        }

        @Override
        public void createMessage(MessageCreateRequest request) {
            lastRequest = request;
        }

        @Override
        public long getSentMessagesCount(UUID senderId) {
            return sentMessagesCount;
        }
    }

    private static final class StubUserServiceClient extends UserServiceClient {
        private final UserResponse userResponse;

        private StubUserServiceClient(UserResponse userResponse) {
            super(null);
            this.userResponse = userResponse;
        }

        @Override
        public UserResponse getUser(UUID userId) {
            return userResponse;
        }
    }

    private static final class NoopUserServiceClient extends UserServiceClient {
        private NoopUserServiceClient() {
            super(null);
        }

        @Override
        public UserResponse getUser(UUID userId) {
            throw new UnsupportedOperationException("Not expected in this test");
        }
    }
}
