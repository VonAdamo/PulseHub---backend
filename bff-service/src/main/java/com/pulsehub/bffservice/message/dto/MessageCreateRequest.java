package com.pulsehub.bffservice.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageCreateRequest(
        @Size(max = 100)
        String channel,

        @NotBlank
        @Size(max = 2000)
        String content
) {
}
