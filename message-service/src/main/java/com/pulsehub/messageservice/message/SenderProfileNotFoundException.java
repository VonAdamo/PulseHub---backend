package com.pulsehub.messageservice.message;

import java.util.UUID;

public class SenderProfileNotFoundException extends RuntimeException {

    public SenderProfileNotFoundException(UUID senderId) {
        super("Sender profile not found in user-service: " + senderId);
    }
}
