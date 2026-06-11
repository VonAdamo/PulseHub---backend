package com.pulsehub.botservice.bot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class MessageServiceClient {

    private final RestClient messageRestClient;

    public MessageServiceClient(RestClient messageRestClient) {
        this.messageRestClient = messageRestClient;
    }

    public void createMessage(MessageCreateRequest request) {
        messageRestClient.post()
                .uri("/messages")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public long getSentMessagesCount(UUID senderId) {
        MessageCountResponse response = messageRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/messages/count")
                        .queryParam("senderId", senderId)
                        .build())
                .retrieve()
                .body(MessageCountResponse.class);

        return response.sentMessagesCount();
    }
}
