package com.pulsehub.messageservice.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateMessageRequest(
        @NotNull
        UUID senderId,

        @NotBlank
        @Size(max = 50)
        String username,

        @Size(max = 100)
        String channel,

        @NotBlank
        @Size(max = 2000)
        String content
) {
}
