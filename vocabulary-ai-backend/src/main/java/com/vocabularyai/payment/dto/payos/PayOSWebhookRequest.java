package com.vocabularyai.payment.dto.payos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Webhook request DTO from PayOS
 * 
 * PayOS sends webhook payload when payment status changes:
 * - When user completes payment (PAID)
 * - When user cancels payment (CANCELLED)
 * - On timeout (EXPIRED)
 * 
 * Webhook format:
 * {
 *   "id": "123456789",
 *   "code": "PAYMENT.RECEIVED" | "PAYMENT_CANCELLED" | "PAYMENT_EXPIRED",
 *   "desc": "Payment received successfully",
 *   "data": {
 *     "id": "string",
 *     "orderCode": "ORD_1694000001_12345",
 *     "amount": 99000,
 *     "amountPaid": 99000,
 *     "amountRemaining": 0,
 *     "transactionDateTime": "2023-01-01T12:00:00Z",
 *     "expireAt": "2023-01-01T12:15:00Z",
 *     "expiredAt": "2023-01-01T12:15:00Z",
 *     "status": "PAID" | "CANCELLED" | "PENDING" | "EXPIRED",
 *     "referenceCode": "string",
 *     "transactionCode": "string",
 *     "reason": "string",
 *     "accountNumber": "string",
 *     "accountName": "string",
 *     "counterPartyCode": "111222333",
 *     "counterPartyName": "TRAN VAN A",
 *     "transferType": "BANK_ACCOUNT" | "CARD" | "WALLET",
 *     "description": "Premium Monthly",
 *     "cancellationReason": "USER_CANCELLED",
 *     "cancellationDescription": "Người dùng hủy"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class PayOSWebhookRequest {
    
    /**
     * Webhook ID - unique per webhook event
     */
    @JsonProperty("id")
    private String webhookId;
    
    /**
     * Webhook code
     * "PAYMENT.RECEIVED" = payment successful
     * "PAYMENT_CANCELLED" = payment cancelled
     * "PAYMENT_EXPIRED" = order expired
     */
    private String code;
    
    /**
     * Description
     */
    private String desc;
    
    /**
     * Webhook payload data
     */
    private PayOSWebhookData data;
    
    /**
     * Webhook signature directly in payload body
     */
    private String signature;
    
    /**
     * Nested DTO for webhook data
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class PayOSWebhookData {
        
        /**
         * Payment ID from PayOS
         */
        private String id;
        
        /**
         * Order code from VocabularyAI
         * IMPORTANT: Use this to look up PaymentOrder
         */
        private String orderCode;
        
        /**
         * Payment amount
         */
        private Long amount;
        
        /**
         * Amount paid
         */
        private Long amountPaid;
        
        /**
         * Amount remaining
         */
        private Long amountRemaining;
        
        /**
         * Transaction creation time
         */
        private String transactionDateTime;
        
        /**
         * Expiration time
         */
        private String expireAt;
        
        /**
         * Expired at (another format)
         */
        private String expiredAt;
        
        /**
         * Payment status
         * "PAID" = successful payment
         * "CANCELLED" = user cancelled
         * "PENDING" = still processing
         * "EXPIRED" = order expired
         */
        private String status;
        
        /**
         * Reference code
         */
        private String referenceCode;

        /**
         * Reference in official PayOS webhook
         */
        @JsonProperty("reference")
        private String reference;
        
        /**
         * Transaction code from PayOS
         * IMPORTANT: Save this in Payment.transactionCode
         * Used to prevent duplicate payment processing (idempotency)
         */
        private String transactionCode;

        public String getTransactionCode() {
            if (transactionCode != null && !transactionCode.isBlank()) {
                return transactionCode;
            }
            if (reference != null && !reference.isBlank()) {
                return reference;
            }
            if (referenceCode != null && !referenceCode.isBlank()) {
                return referenceCode;
            }
            return orderCode != null ? orderCode : null;
        }
        
        /**
         * Reason for status change
         */
        private String reason;
        
        /**
         * Account number used for transfer
         */
        private String accountNumber;
        
        /**
         * Account name on transfer
         */
        private String accountName;
        
        /**
         * Counter party code (payer's bank code)
         */
        private String counterPartyCode;
        
        /**
         * Counter party name (payer's name)
         */
        private String counterPartyName;
        
        /**
         * Transfer method
         * "BANK_ACCOUNT", "CARD", "WALLET"
         */
        private String transferType;
        
        /**
         * Payment description
         */
        private String description;
        
        /**
         * Cancellation reason (if cancelled)
         */
        private String cancellationReason;
        
        /**
         * Cancellation description (if cancelled)
         */
        private String cancellationDescription;
    }
    
    /**
     * Check if this is a successful payment webhook
     */
    public boolean isPaymentReceived() {
        return ("00".equals(this.code) || "PAYMENT.RECEIVED".equalsIgnoreCase(this.code)) && 
               this.data != null && 
               ("PAID".equalsIgnoreCase(this.data.status) || "00".equals(this.data.status));
    }
    
    /**
     * Check if this is a cancelled payment webhook
     */
    public boolean isPaymentCancelled() {
        return ("PAYMENT_CANCELLED".equalsIgnoreCase(this.code) || "PAYMENT.CANCELLED".equalsIgnoreCase(this.code)) && 
               this.data != null && 
               ("CANCELLED".equalsIgnoreCase(this.data.status));
    }
    
    /**
     * Check if this is an expired payment webhook
     */
    public boolean isPaymentExpired() {
        return ("PAYMENT_EXPIRED".equalsIgnoreCase(this.code) || "PAYMENT.EXPIRED".equalsIgnoreCase(this.code)) && 
               this.data != null && 
               ("EXPIRED".equalsIgnoreCase(this.data.status));
    }
}
