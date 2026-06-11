package com.pulsehub.botservice.bot;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestClient userRestClient;

    public UserServiceClient(@Qualifier("userRestClient") RestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    public UserResponse getUser(UUID userId) {
        return userRestClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .body(UserResponse.class);
    }
}
