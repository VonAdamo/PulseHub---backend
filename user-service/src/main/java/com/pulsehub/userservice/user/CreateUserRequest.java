package com.pulsehub.userservice.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserRequest(
        UUID id,

        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Size(max = 100)
        String displayName
) {

    public CreateUserRequest(String username, String displayName) {
        this(null, username, displayName);
    }
}
