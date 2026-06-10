package com.pulsehub.messageservice.message;

import java.util.UUID;

public record UserProfile(
        UUID userId,
        String username,
        String displayName
) {
}
