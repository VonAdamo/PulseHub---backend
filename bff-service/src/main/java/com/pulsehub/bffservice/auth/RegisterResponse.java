package com.pulsehub.bffservice.auth;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String username,
        String displayName
) {
}
