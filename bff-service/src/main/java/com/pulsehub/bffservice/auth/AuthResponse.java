package com.pulsehub.bffservice.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        UUID userId,
        String username,
        String token
) {
}
