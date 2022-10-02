package com.example.islandreservation.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException() {
        super(HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    public InvalidRequestException(Throwable cause) {
        super(HttpStatus.BAD_REQUEST.getReasonPhrase(), cause);
    }

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
