package com.vocabularyai.payment.dto.payos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO to create a payment link with PayOS
 * 
 * Required fields:
 * - orderCode: Unique order code from VocabularyAI (ORD_*_*)
 * - amount: Amount in VND
 * - description: Description of the payment
 * - returnUrl: URL to redirect user after successful payment
 * - cancelUrl: URL to redirect user after failed/cancelled payment
 * - expiredAt: Order expiration time (must be > now)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSCreatePaymentLinkRequest {
    
    /**
     * Unique order code - UK constraint in payment_orders table
     * Format: ORD_{timestamp}_{randomId}
     */
    @NotBlank(message = "Order code is required")
    @Size(min = 5, max = 50, message = "Order code must be between 5 and 50 characters")
    private String orderCode;
    
    /**
     * Payment amount in VND
     * Must match package price (database is source of truth)
     */
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    /**
     * Payment description for display
     * Format: "Premium Monthly - 99,000 VND"
     */
    @NotBlank(message = "Description is required")
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    /**
     * Merchant-provided return URL for successful payment
     * PayOS will redirect user here with transaction info
     */
    @NotBlank(message = "Return URL is required")
    @Size(max = 500, message = "Return URL must not exceed 500 characters")
    private String returnUrl;
    
    /**
     * Merchant-provided cancel URL for failed/cancelled payment
     * PayOS will redirect user here on failure
     */
    @NotBlank(message = "Cancel URL is required")
    @Size(max = 500, message = "Cancel URL must not exceed 500 characters")
    private String cancelUrl;
    
    /**
     * Order expiration time
     * Order must be paid before this time
     * Typically 15-30 minutes from creation
     */
    @NotNull(message = "Expiration time is required")
    @FutureOrPresent(message = "Expiration time must be in the future")
    private LocalDateTime expiredAt;
    
    /**
     * Optional buyer information for receipt
     */
    @Size(max = 100, message = "Buyer name must not exceed 100 characters")
    private String buyerName;
    
    /**
     * Optional buyer email for receipt
     */
    @Email(message = "Buyer email must be valid")
    @Size(max = 100, message = "Buyer email must not exceed 100 characters")
    private String buyerEmail;
    
    /**
     * Optional buyer phone for receipt
     */
    @Size(max = 20, message = "Buyer phone must not exceed 20 characters")
    private String buyerPhone;
}
