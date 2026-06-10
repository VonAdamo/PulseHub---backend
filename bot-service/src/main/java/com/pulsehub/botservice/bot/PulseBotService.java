package com.pulsehub.botservice.bot;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class PulseBotService {

    static final String BOT_SENDER_ID = "00000000-0000-0000-0000-000000000001";
    static final String BOT_USERNAME = "PulseBot";
    static final String BOT_RESPONSE = "Hej! Jag är PulseBot. Testa att skriva /help.";

    private final MessageServiceClient messageServiceClient;

    public PulseBotService(MessageServiceClient messageServiceClient) {
        this.messageServiceClient = messageServiceClient;
    }

    public void handle(MessagePublishedEvent event) {
        if (!shouldRespond(event)) {
            return;
        }

        messageServiceClient.createMessage(new MessageCreateRequest(
                BOT_SENDER_ID,
                BOT_USERNAME,
                event.channel(),
                BOT_RESPONSE
        ));
    }

    boolean shouldRespond(MessagePublishedEvent event) {
        if (event == null || BOT_USERNAME.equals(event.username()) || event.content() == null) {
            return false;
        }

        String content = event.content().toLowerCase(Locale.ROOT);
        return content.contains("/help") || content.contains("hej bot");
    }
}
