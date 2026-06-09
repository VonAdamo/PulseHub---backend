package com.pulsehub.messageservice.message;

public class MessageEventPublishException extends RuntimeException {

    public MessageEventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
