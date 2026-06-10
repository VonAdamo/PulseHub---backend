package com.pulsehub.bffservice.user;

import java.util.UUID;

public record CreateUserRequest(
        UUID id,
        String username,
        String displayName
) {
}
