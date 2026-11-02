package com.poc.hub.core.error;

/** Maps to HTTP 409 (for example a business rule that conflicts with existing data). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
