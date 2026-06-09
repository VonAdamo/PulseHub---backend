package com.pulsehub.bffservice.message.dto;

public record MessageServiceCreateRequest(
        String senderId,
        String username,
        String channel,
        String content
) {
}
