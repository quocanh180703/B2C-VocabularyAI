package com.vocabularyai.common.exception;

/**
 * Exception thrown when a requested resource is not found
 * HTTP Status: 404 Not Found
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ResourceNotFoundException of(String resourceName, String fieldName, Object fieldValue) {
        return new ResourceNotFoundException(
            String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue)
        );
    }
}
