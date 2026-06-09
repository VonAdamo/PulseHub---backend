package com.pulsehub.bffservice.message.dto;

import java.time.Instant;

public record MessageResponse(
        String id,
        String senderId,
        String username,
        String channel,
        String content,
        Instant createdAt,
        Instant updatedAt
) {

}
