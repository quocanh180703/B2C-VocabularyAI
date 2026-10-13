package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment List Item Response DTO
 * 
 * Used for paginated list of payments (admin system-wide view)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Payment information for list view")
public class PaymentListResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "Payment ID", example = "1")
    private Long paymentId;
    
    @Schema(description = "Transaction code from payment gateway", example = "TXN_123456789")
    private String transactionCode;
    
    @Schema(description = "Order code (internal)", example = "ORD_1234567890123_12345")
    private String orderCode;
    
    @Schema(description = "User ID who made payment", example = "42")
    private Long userId;
    
    @Schema(description = "User email", example = "user@example.com")
    private String userEmail;
    
    @Schema(description = "User full name", example = "Nguyễn Văn A")
    private String userFullName;
    
    @Schema(description = "Package name purchased", example = "Premium 3 Months")
    private String packageName;
    
    @Schema(description = "Payment amount in VND", example = "499000")
    private BigDecimal amount;
    
    @Schema(description = "Payment status (SUCCESS, PENDING, CANCELLED, FAILED)", example = "SUCCESS")
    private String status;
    
    @Schema(description = "Payment method (ATM, CREDIT, EWALLET, etc.)", example = "ATM")
    private String paymentMethod;
    
    @Schema(description = "Payment date", example = "2024-01-15T10:30:00")
    private LocalDateTime paidAt;
    
    @Schema(description = "Payment creation date", example = "2024-01-15T10:25:00")
    private LocalDateTime createdAt;
}
