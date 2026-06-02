package com.pulsehub.authservice.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    ProblemDetail handleUsernameAlreadyExists(UsernameAlreadyExistsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Username already exists");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("Invalid credentials");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }
}
