package com.vocabularyai.common.exception;

/**
 * Exception thrown when request is invalid or contains bad data
 * HTTP Status: 400 Bad Request
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
