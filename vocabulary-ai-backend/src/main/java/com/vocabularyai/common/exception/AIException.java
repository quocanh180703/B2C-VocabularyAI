package com.vocabularyai.common.exception;

/**
 * Exception thrown when AI service fails
 * HTTP Status: 500 Internal Server Error
 */
public class AIException extends RuntimeException {
    
    public AIException(String message) {
        super(message);
    }
    
    public AIException(String message, Throwable cause) {
        super(message, cause);
    }
}
