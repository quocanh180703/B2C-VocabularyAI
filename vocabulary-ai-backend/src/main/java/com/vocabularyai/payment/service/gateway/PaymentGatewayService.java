package com.vocabularyai.payment.service.gateway;

import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkRequest;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;

/**
 * Interface for Payment Gateway (abstract layer for PayOS)
 * 
 * This interface provides abstraction for payment gateway operations.
 * Current implementation: PayOS
 * Future implementations: Stripe, VietComBank, etc.
 * 
 * Responsibilities:
 * - Payment link generation
 * - Webhook signature verification
 * - Payment status retrieval
 * - Payment cancellation
 * 
 * NOT responsible for:
 * - Business logic (subscription creation, payment recording)
 * - Database operations
 * - User authentication/authorization
 */
public interface PaymentGatewayService {
    
    /**
     * Create a payment link with the payment gateway
     * 
     * Performs:
     * - Request validation
     * - Payment link generation via API
     * - Retry logic on transient failures
     * - Error handling
     * 
     * @param request Payment link creation request (orderCode, amount, description, URLs, expiration)
     * @return Payment link response (checkoutUrl, transactionCode, qrCode, etc.)
     * @throws com.vocabularyai.payment.exception.PaymentGatewayException if API call fails
     * @throws com.vocabularyai.payment.exception.PaymentTimeoutException if API times out
     */
    PayOSCreatePaymentLinkResponse createPaymentLink(PayOSCreatePaymentLinkRequest request);
    
    /**
     * Verify webhook signature from payment gateway
     * 
     * Performs HMAC-SHA256 signature verification to ensure:
     * - Webhook is from trusted source (PayOS)
     * - Payload was not tampered with
     * - Replay attack prevention through signature
     * 
     * Implementation:
     * 1. Reconstruct message: "orderCode=xxx&amount=yyy&reference=zzz"
     * 2. Calculate HMAC-SHA256(message, apiKey)
     * 3. Compare with received signature
     * 
     * @param orderCode Order code from webhook
     * @param amount Payment amount from webhook
     * @param transactionCode Transaction code from PayOS
     * @param signature Signature from webhook header
     * @return true if signature is valid, false otherwise
     */
    boolean verifyWebhookSignature(String orderCode, Long amount, String transactionCode, String signature);
    
    /**
     * Get payment status from payment gateway
     * 
     * Queries the gateway for current payment status
     * (not typically needed in webhook-based flow, but useful for recovery)
     * 
     * @param transactionCode Transaction code from PayOS
     * @return Payment status (PAID, PENDING, CANCELLED, EXPIRED, etc.)
     * @throws com.vocabularyai.payment.exception.PaymentGatewayException if query fails
     * @throws com.vocabularyai.payment.exception.PaymentTimeoutException if query times out
     */
    String getPaymentStatus(String transactionCode);
    
    /**
     * Cancel a payment order
     * 
     * Initiates cancellation with the payment gateway
     * (not typically needed in normal flow, but useful for admin operations)
     * 
     * @param orderCode Order code to cancel
     * @return true if cancellation was successful, false otherwise
     * @throws com.vocabularyai.payment.exception.PaymentGatewayException if cancellation fails
     */
    boolean cancelPayment(String orderCode);
    
    /**
     * Validate webhook request from payment gateway
     * 
     * Performs basic structure validation:
     * - Checks required fields are present
     * - Validates data types
     * - Checks for anomalies
     * 
     * @param webhook Webhook request to validate
     * @return true if webhook is structurally valid, false otherwise
     */
    boolean validateWebhook(PayOSWebhookRequest webhook);
    
    /**
     * Get gateway name for logging and identification
     * 
     * @return Gateway name (e.g., "PayOS", "Stripe")
     */
    String getGatewayName();
    
    /**
     * Check if gateway is available/healthy
     * 
     * Useful for:
     * - Health checks
     * - Monitoring
     * - Fallback logic
     * 
     * @return true if gateway is available, false otherwise
     */
    boolean isHealthy();
}
