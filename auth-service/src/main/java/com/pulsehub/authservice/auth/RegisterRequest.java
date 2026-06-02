package com.pulsehub.authservice.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Size(max = 100)
        String displayName,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}
