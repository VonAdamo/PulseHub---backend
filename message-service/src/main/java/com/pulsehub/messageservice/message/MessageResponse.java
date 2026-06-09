package com.pulsehub.messageservice.message;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID senderId,
        String username,
        String channel,
        String content,
        Instant createdAt,
        Instant updatedAt
) {

    static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getUsername(),
                message.getChannel(),
                message.getContent(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
