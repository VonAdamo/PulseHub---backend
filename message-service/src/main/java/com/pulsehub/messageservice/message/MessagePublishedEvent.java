package com.pulsehub.messageservice.message;

import java.time.Instant;
import java.util.UUID;

public record MessagePublishedEvent(
        UUID eventId,
        String type,
        UUID messageId,
        UUID senderId,
        String username,
        String channel,
        String content,
        Instant createdAt
) {

    static MessagePublishedEvent from(Message message) {
        return new MessagePublishedEvent(
                UUID.randomUUID(),
                "message-published",
                message.getId(),
                message.getSenderId(),
                message.getUsername(),
                message.getChannel(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
