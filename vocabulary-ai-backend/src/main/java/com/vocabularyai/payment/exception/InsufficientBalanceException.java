package com.vocabularyai.payment.exception;

/**
 * Thrown when user has insufficient balance or funds for payment
 * Note: In V1, this is reserved for future payment methods (wallet, account balance)
 * Current implementation: All payments go through PayOS
 */
public class InsufficientBalanceException extends PaymentException {
    
    public InsufficientBalanceException(String message) {
        super("INSUFFICIENT_BALANCE", message);
    }
    
    public InsufficientBalanceException(String message, Throwable cause) {
        super("INSUFFICIENT_BALANCE", message, cause);
    }
}
