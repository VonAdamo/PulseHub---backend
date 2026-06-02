package com.pulsehub.authservice.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        UUID userId,
        String username,
        String token
) {

    static AuthResponse registered(AuthUser user) {
        return new AuthResponse(user.getUserId(), user.getUsername(), null);
    }

    static AuthResponse authenticated(AuthUser user, String token) {
        return new AuthResponse(user.getUserId(), user.getUsername(), token);
    }
}
