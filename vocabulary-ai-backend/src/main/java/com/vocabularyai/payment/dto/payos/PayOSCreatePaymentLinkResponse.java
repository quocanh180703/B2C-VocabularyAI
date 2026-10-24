package com.vocabularyai.payment.dto.payos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from PayOS when creating a payment link
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayOSCreatePaymentLinkResponse {
    
    /**
     * Response code
     * "00" = success
     * "01-99" = various error codes
     */
    private String code;
    
    /**
     * Response description
     * "success" or error message
     */
    private String desc;
    
    /**
     * Payment link data
     */
    private PayOSPaymentLinkData data;
    
    /**
     * Signature from PayOS
     */
    private String signature;
    
    /**
     * Nested DTO for payment link data
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayOSPaymentLinkData {
        
        private String bin;
        private String currency;
        private String paymentLinkId;
        private String status;
        private Long orderCode;
        
        /**
         * Virtual account number for bank transfer
         */
        private String accountNumber;
        
        /**
         * Account name on bank transfer
         */
        private String accountName;
        
        /**
         * Acquirer ID
         */
        private String acqId;
        
        /**
         * Payment amount in VND
         */
        private Long amount;
        
        /**
         * Amount already paid
         */
        private Long amountPaid;
        
        /**
         * Remaining amount to pay
         */
        private Long amountRemaining;
        
        /**
         * Transaction creation time in ISO 8601 format
         */
        private String transactionDateTime;
        
        /**
         * Payment expiration time in ISO 8601 format
         */
        private String expireAt;
        
        /**
         * Reference code for tracking
         */
        private String referenceCode;
        
        /**
         * Transaction code from PayOS - used for webhook verification and reconciliation
         * IMPORTANT: Save this in Payment.transactionCode
         */
        private String transactionCode;
        
        /**
         * QR code image URL for scanning
         */
        private String qrCode;
        
        /**
         * Checkout URL - redirect user here to complete payment
         * This is the payment link user needs to access
         * Save in PaymentOrder.paymentUrl
         */
        private String checkoutUrl;
        
        /**
         * EMI amount (installment)
         */
        private Long emiAmount;
        
        /**
         * EMI description
         */
        private String emiDescription;
        
        /**
         * EMI iteration
         */
        private Long emiIteration;
    }
    
    /**
     * Check if response is successful
     */
    public boolean isSuccess() {
        return "00".equals(this.code) && this.data != null;
    }
}
