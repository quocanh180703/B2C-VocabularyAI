package com.vocabularyai.payment.exception;

/**
 * Base exception for payment module
 * All payment-related exceptions extend this
 */
public class PaymentException extends RuntimeException {
    
    private String errorCode;
    
    public PaymentException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PAYMENT_ERROR";
    }
    
    public PaymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PaymentException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
