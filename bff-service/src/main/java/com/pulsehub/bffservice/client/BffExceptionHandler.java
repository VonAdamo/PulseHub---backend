package com.pulsehub.bffservice.client;

import org.springframework.http.MediaType;
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
}
