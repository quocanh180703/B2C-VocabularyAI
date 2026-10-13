package com.vocabularyai.payment.exception;

/**
 * Thrown when attempting to process duplicate payment
 * Idempotency protection: same transactionCode received multiple times
 */
public class DuplicatePaymentException extends PaymentException {
    
    public DuplicatePaymentException(String message) {
        super("DUPLICATE_PAYMENT", message);
    }
    
    public DuplicatePaymentException(String message, Throwable cause) {
        super("DUPLICATE_PAYMENT", message, cause);
    }
}
