package com.vocabularyai.payment.exception;

/**
 * Thrown when payment gateway API times out
 */
public class PaymentTimeoutException extends PaymentException {
    
    public PaymentTimeoutException(String message) {
        super("TIMEOUT", message);
    }
    
    public PaymentTimeoutException(String message, Throwable cause) {
        super("TIMEOUT", message, cause);
    }
}
