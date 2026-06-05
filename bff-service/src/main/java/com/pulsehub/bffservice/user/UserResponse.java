package com.pulsehub.bffservice.user;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String displayName,
        Instant createdAt,
        Instant updatedAt
) {
}
