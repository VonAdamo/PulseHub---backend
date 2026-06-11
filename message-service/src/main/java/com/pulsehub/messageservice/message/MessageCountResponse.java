package com.pulsehub.messageservice.message;

public record MessageCountResponse(
        long sentMessagesCount
) {
    static MessageCountResponse from(long sentMessagesCount) {
        return new MessageCountResponse(sentMessagesCount);
    }
}
