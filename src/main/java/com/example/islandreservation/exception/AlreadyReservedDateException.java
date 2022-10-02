package com.example.islandreservation.exception;

import org.springframework.http.HttpStatus;

public class AlreadyReservedDateException extends RuntimeException {
    public AlreadyReservedDateException() {
        super(HttpStatus.CONFLICT.getReasonPhrase());
    }

    public AlreadyReservedDateException(Throwable cause) {
        super(HttpStatus.CONFLICT.getReasonPhrase(), cause);
    }

    public AlreadyReservedDateException(String message) {
        super(message);
    }

    public AlreadyReservedDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
