package com.pulsehub.bffservice.client;

import com.pulsehub.bffservice.auth.RegistrationProfileCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class BffExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<String> handleRestClientResponse(RestClientResponseException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(exception.getResponseBodyAsString());
    }

    @ExceptionHandler(RegistrationProfileCreationException.class)
    ProblemDetail handleRegistrationProfileCreation(RegistrationProfileCreationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        problemDetail.setTitle("Registration profile creation failed");
        problemDetail.setDetail("Auth user was created, but user profile could not be created. This can leave a partially created account.");
        problemDetail.setProperty("userId", exception.getUserId());
        problemDetail.setProperty("username", exception.getUsername());
        problemDetail.setProperty("downstreamService", "user-service");
        problemDetail.setProperty("downstreamStatus", exception.getDownstreamStatus());
        problemDetail.setProperty("downstreamResponse", exception.getDownstreamResponse());
        return problemDetail;
    }
}
