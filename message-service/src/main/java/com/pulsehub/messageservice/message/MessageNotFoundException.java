package com.pulsehub.messageservice.message;

import java.util.UUID;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(UUID id) {
        super("Message not found: " + id);
    }
}
