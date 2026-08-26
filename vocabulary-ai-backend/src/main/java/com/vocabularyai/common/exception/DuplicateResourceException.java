package com.vocabularyai.common.exception;

/**
 * Exception thrown when trying to create duplicate resource
 * HTTP Status: 409 Conflict
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static DuplicateResourceException of(String fieldName, Object fieldValue) {
        return new DuplicateResourceException(
            String.format("%s already exists: %s", fieldName, fieldValue)
        );
    }
}
