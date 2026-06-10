package com.pulsehub.botservice.bot;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
}
