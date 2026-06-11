package com.pulsehub.bffservice.message;

import com.pulsehub.bffservice.message.dto.MessageResponse;
import com.pulsehub.bffservice.message.dto.MessageServiceCreateRequest;
import com.pulsehub.bffservice.message.dto.MessageCountResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;
import java.util.List;

@Component
public class MessageServiceClient {

    private final RestClient restClient;

    public MessageServiceClient(@Qualifier("messageRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public MessageResponse createMessage(MessageServiceCreateRequest request) {
        return restClient.post()
                .uri("/messages")
                .body(request)
                .retrieve()
                .body(MessageResponse.class);
    }

    public List<MessageResponse> getMessages(String channel) {
        return restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/messages");

                    if (channel != null && !channel.isBlank()) {
                        builder.queryParam("channel", channel);
                    }

                    return builder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public MessageResponse getMessageById(String id) {
        return restClient.get()
                .uri("/messages/{id}", id)
                .retrieve()
                .body(MessageResponse.class);
    }

    public long getSentMessagesCount(UUID senderId) {
        MessageCountResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/messages/count")
                        .queryParam("senderId", senderId)
                        .build())
                .retrieve()
                .body(MessageCountResponse.class);

        return response.sentMessagesCount();
    }
}
