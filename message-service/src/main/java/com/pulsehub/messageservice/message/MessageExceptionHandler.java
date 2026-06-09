package com.pulsehub.messageservice.message;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MessageExceptionHandler {

    @ExceptionHandler(MessageNotFoundException.class)
    ProblemDetail handleMessageNotFound(MessageNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Message not found");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(MessageEventPublishException.class)
    ProblemDetail handleMessageEventPublish(MessageEventPublishException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problemDetail.setTitle("Message event could not be published");
        problemDetail.setDetail(exception.getMessage());
        return problemDetail;
    }
}
