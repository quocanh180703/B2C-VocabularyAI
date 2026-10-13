package com.vocabularyai.payment.exception;

/**
 * Thrown when payment processing fails during transaction handling
 * Examples: Database transaction failure, subscription creation failure
 */
public class PaymentProcessingException extends PaymentException {
    
    public PaymentProcessingException(String message) {
        super("PROCESSING_ERROR", message);
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super("PROCESSING_ERROR", message, cause);
    }
}
