package com.vocabularyai.payment.dto.payos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for PayOS webhook signature verification
 * 
 * PayOS signs webhook payloads using HMAC-SHA256 with a secret key
 * The signature is sent in the HTTP header: "x-payos-signature" or in request body
 * 
 * To verify:
 * 1. Extract the signature from webhook header
 * 2. Reconstruct the message: "orderCode=ORD_123&amount=99000&reference=TXN_ABC"
 * 3. Calculate HMAC-SHA256(message, apiKey)
 * 4. Compare with received signature
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSVerifySignatureRequest {
    
    /**
     * The webhook payload data as a reconstructed message
     * Format: "orderCode=ORD_123&amount=99000&reference=TXN_ABC"
     * Used to verify signature
     */
    @NotBlank(message = "Message cannot be blank")
    private String message;
    
    /**
     * The signature received from PayOS
     * HMAC-SHA256 hash
     */
    @NotBlank(message = "Signature cannot be blank")
    private String signature;
}
