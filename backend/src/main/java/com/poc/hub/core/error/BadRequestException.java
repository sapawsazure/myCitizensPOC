package com.poc.hub.core.error;

/** Maps to HTTP 400 for input problems that bean validation cannot express. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
