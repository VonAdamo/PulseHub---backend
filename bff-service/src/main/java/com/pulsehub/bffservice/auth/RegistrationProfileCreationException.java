package com.pulsehub.bffservice.auth;

import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

public class RegistrationProfileCreationException extends RuntimeException {

    private final UUID userId;
    private final String username;
    private final Integer downstreamStatus;
    private final String downstreamResponse;

    public RegistrationProfileCreationException(UUID userId, String username, Throwable cause) {
        super("Auth user was created, but user profile could not be created");
        this.userId = userId;
        this.username = username;
        if (cause instanceof RestClientResponseException responseException) {
            this.downstreamStatus = responseException.getStatusCode().value();
            this.downstreamResponse = responseException.getResponseBodyAsString();
        } else {
            this.downstreamStatus = null;
            this.downstreamResponse = cause.getMessage();
        }
        initCause(cause);
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Integer getDownstreamStatus() {
        return downstreamStatus;
    }

    public String getDownstreamResponse() {
        return downstreamResponse;
    }
}
