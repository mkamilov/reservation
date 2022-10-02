package com.example.islandreservation.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    public ResourceNotFoundException(Throwable cause) {
        super(HttpStatus.NOT_FOUND.getReasonPhrase(), cause);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
