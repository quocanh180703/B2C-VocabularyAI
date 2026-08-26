package com.vocabularyai.common.exception;

/**
 * Exception thrown when external service call fails
 * HTTP Status: 503 Service Unavailable
 */
public class ExternalServiceException extends RuntimeException {
    
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
