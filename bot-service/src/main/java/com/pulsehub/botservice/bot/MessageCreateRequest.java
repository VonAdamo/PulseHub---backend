package com.pulsehub.botservice.bot;

public record MessageCreateRequest(
        String senderId,
        String username,
        String channel,
        String content
) {
}
