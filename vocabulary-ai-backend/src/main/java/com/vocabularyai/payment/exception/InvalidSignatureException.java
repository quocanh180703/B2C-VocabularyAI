package com.vocabularyai.payment.exception;

/**
 * Thrown when webhook signature verification fails
 * Security issue: webhook is not from trusted PayOS
 */
public class InvalidSignatureException extends PaymentException {
    
    public InvalidSignatureException(String message) {
        super("INVALID_SIGNATURE", message);
    }
    
    public InvalidSignatureException(String message, Throwable cause) {
        super("INVALID_SIGNATURE", message, cause);
    }
}
