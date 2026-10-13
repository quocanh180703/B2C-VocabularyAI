package com.vocabularyai.payment.exception;

/**
 * Thrown when payment gateway API returns error
 * Examples: Invalid request, API errors, gateway failures
 */
public class PaymentGatewayException extends PaymentException {
    
    public PaymentGatewayException(String message) {
        super("GATEWAY_ERROR", message);
    }
    
    public PaymentGatewayException(String message, Throwable cause) {
        super("GATEWAY_ERROR", message, cause);
    }
    
    public PaymentGatewayException(String gatewayResponse, String message, Throwable cause) {
        super("GATEWAY_ERROR", message + " (Gateway response: " + gatewayResponse + ")", cause);
    }
}
