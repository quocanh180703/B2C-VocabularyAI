package com.vocabularyai.payment.service;

import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkRequest;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.entity.PaymentOrder;
import com.vocabularyai.payment.entity.Subscription;

/**
 * Payment Service Interface - Business Logic Layer
 * 
 * Responsible for:
 * - Payment initiation (create PaymentOrder, call gateway)
 * - Payment confirmation (verify signature, create Payment, create Subscription)
 * - Payment status tracking
 * - Subscription management
 * - Business rule enforcement
 * 
 * NOT responsible for:
 * - HTTP handling (done by Controller)
 * - Gateway API calls (done by PaymentGatewayService)
 * - Database operations directly (done by Repositories)
 */
public interface PaymentService {
    
    /**
     * Initiate a payment for a specific package
     * 
     * Performs:
     * 1. Validate user and package
     * 2. Check for existing active subscriptions (prevent duplicate)
     * 3. Generate unique order code
     * 4. Create PaymentOrder in database (status: PENDING)
     * 5. Call PaymentGatewayService to create payment link (using backend-configured URLs)
     * 6. Update PaymentOrder with payment URL
     * 7. Return response with checkout URL
     * 
     * Note: Return and Cancel URLs are configured in backend (from PaymentProperties),
     * not provided by client, to prevent clients from redirecting users to malicious sites.
     * Package price is fetched from database, not accepted from client.
     * 
     * @param userId User ID who is making payment
     * @param packageId Package ID to purchase
     * @return Payment initiation response with checkout URL
     * @throws IllegalArgumentException if package not found
     * @throws IllegalStateException if user already has active subscription
     * @throws com.vocabularyai.payment.exception.PaymentGatewayException if payment link creation fails
     */
    PayOSCreatePaymentLinkResponse initiatePayment(
        Long userId,
        Long packageId
    );
    
    /**
     * Handle payment webhook callback from PayOS
     * 
     * Performs:
     * 1. Verify webhook signature (security check)
     * 2. Validate webhook structure
     * 3. Check webhook is for PAID status (skip if not)
     * 4. Look up PaymentOrder by orderCode
     * 5. Check for duplicate (transaction already processed - idempotency)
     * 6. Update PaymentOrder status to PAID
     * 7. Create Payment record with transaction details
     * 8. Create Subscription record for user
     * 9. Emit event for subscription activation (optional)
     * 
     * Idempotency:
     * - Same webhook received twice (same transactionCode)
     * - Second call returns success without re-processing
     * - Uses transactionCode as unique key
     * 
     * @param webhook PayOS webhook payload
     * @param signature Webhook signature from HTTP header
     * @return true if webhook processed successfully
     * @throws com.vocabularyai.payment.exception.InvalidSignatureException if signature verification fails
     * @throws com.vocabularyai.payment.exception.DuplicatePaymentException if payment already processed
     * @throws IllegalArgumentException if webhook data invalid or PaymentOrder not found
     */
    boolean handlePaymentWebhook(PayOSWebhookRequest webhook, String signature);
    
    /**
     * Get payment order details
     * 
     * @param orderId Payment order ID
     * @return PaymentOrder details
     * @throws IllegalArgumentException if order not found
     */
    PaymentOrder getPaymentOrder(Long orderId);
    
    /**
     * Get user's current subscription
     * 
     * @param userId User ID
     * @return Current active Subscription, or null if no active subscription
     */
    Subscription getUserSubscription(Long userId);
    
    /**
     * Check if user has active subscription
     * 
     * Checks:
     * - Subscription exists
     * - Status is ACTIVE
     * - End time is after now
     * 
     * @param userId User ID
     * @return true if user has valid active subscription
     */
    boolean hasActiveSubscription(Long userId);
    
    /**
     * Get user's subscription status
     * 
     * Returns information about:
     * - Is active or expired
     * - Package name
     * - Remaining days
     * - End date
     * 
     * @param userId User ID
     * @return Subscription status information, or null if no subscription
     */
    SubscriptionStatus getSubscriptionStatus(Long userId);
    
    /**
     * Cancel a subscription (admin operation)
     * 
     * @param subscriptionId Subscription ID to cancel
     * @return true if cancelled successfully
     * @throws IllegalArgumentException if subscription not found
     */
    boolean cancelSubscription(Long subscriptionId);
    
    /**
     * DTO for subscription status information
     */
    class SubscriptionStatus {
        public Long subscriptionId;
        public Long userId;
        public String packageName;
        public String status; // ACTIVE, EXPIRED, CANCELLED, SUSPENDED
        public Long remainingDays;
        public java.time.LocalDateTime endDate;
        
        public SubscriptionStatus(Long subscriptionId, Long userId, String packageName, 
                                 String status, Long remainingDays, java.time.LocalDateTime endDate) {
            this.subscriptionId = subscriptionId;
            this.userId = userId;
            this.packageName = packageName;
            this.status = status;
            this.remainingDays = remainingDays;
            this.endDate = endDate;
        }
    }
}
