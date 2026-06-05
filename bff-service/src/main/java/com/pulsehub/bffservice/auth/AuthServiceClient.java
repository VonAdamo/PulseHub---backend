package com.pulsehub.bffservice.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthServiceClient {

    private final RestClient authRestClient;

    public AuthServiceClient(@Qualifier("authRestClient") RestClient authRestClient) {
        this.authRestClient = authRestClient;
    }

    public AuthResponse register(RegisterRequest request) {
        return authRestClient.post()
                .uri("/auth/register")
                .body(request)
                .retrieve()
                .body(AuthResponse.class);
    }

    public AuthResponse login(LoginRequest request) {
        return authRestClient.post()
                .uri("/auth/login")
                .body(request)
                .retrieve()
                .body(AuthResponse.class);
    }
}
