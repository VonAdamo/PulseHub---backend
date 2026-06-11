package com.pulsehub.botservice.bot;

import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class PulseBotService {

    static final String BOT_SENDER_ID = "00000000-0000-0000-0000-000000000001";
    static final String BOT_USERNAME = "PulseBot";
    static final String HELP_RESPONSE = "Hej! Jag ar PulseBot. Testa att skriva /help, /about eller /me.";
    static final String ABOUT_RESPONSE = "PulseHub ar en chattplattform med BFF, JWT, gRPC och RabbitMQ. Skriv /help for kommandon.";

    private static final DateTimeFormatter CREATED_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd / HH:mm");

    private final MessageServiceClient messageServiceClient;
    private final UserServiceClient userServiceClient;

    public PulseBotService(MessageServiceClient messageServiceClient, UserServiceClient userServiceClient) {
        this.messageServiceClient = messageServiceClient;
        this.userServiceClient = userServiceClient;
    }

    public void handle(MessagePublishedEvent event) {
        if (!shouldRespond(event)) {
            return;
        }

        messageServiceClient.createMessage(new MessageCreateRequest(
                BOT_SENDER_ID,
                BOT_USERNAME,
                event.channel(),
                responseFor(event)
        ));
    }

    boolean shouldRespond(MessagePublishedEvent event) {
        if (event == null || BOT_USERNAME.equals(event.username()) || event.content() == null) {
            return false;
        }

        String content = event.content().toLowerCase(Locale.ROOT);
        return content.contains("/help") || content.contains("/about") || content.contains("/me") || content.contains("hej bot");
    }

    String responseFor(MessagePublishedEvent event) {
        String content = event.content().toLowerCase(Locale.ROOT);
        if (content.contains("/me")) {
            return buildMeResponse(event);
        }
        if (content.contains("/about")) {
            return ABOUT_RESPONSE;
        }
        return HELP_RESPONSE;
    }

    String buildMeResponse(MessagePublishedEvent event) {
        UUID senderId = UUID.fromString(event.senderId());
        UserResponse user = userServiceClient.getUser(senderId);
        long sentMessagesCount = countMessages(senderId);

        return """
                Du ar inloggad som %s (%s)
                Created: %s
                Skickade meddelanden: %d
                """
                .formatted(
                        user.username(),
                        user.displayName(),
                        CREATED_FORMATTER.format(user.createdAt().atZone(ZoneOffset.UTC)),
                        sentMessagesCount
                ).trim();
    }

    private long countMessages(UUID senderId) {
        return messageServiceClient.getSentMessagesCount(senderId);
    }
}
