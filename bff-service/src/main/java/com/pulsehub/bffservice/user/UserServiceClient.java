package com.pulsehub.bffservice.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class UserServiceClient {

    private final RestClient userRestClient;

    public UserServiceClient(@Qualifier("userRestClient") RestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    public List<UserResponse> getUsers() {
        return userRestClient.get()
                .uri("/users")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public UserResponse getUser(UUID id) {
        return userRestClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .body(UserResponse.class);
    }
}
