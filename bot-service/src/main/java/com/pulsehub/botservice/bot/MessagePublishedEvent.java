package com.pulsehub.botservice.bot;

import java.time.Instant;
import java.util.UUID;

public record MessagePublishedEvent(
        UUID eventId,
        String type,
        UUID messageId,
        String senderId,
        String username,
        String channel,
        String content,
        Instant createdAt
) {
}
