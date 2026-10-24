package com.vocabularyai.payment.dto.payos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Payment Success Email Data
 * 
 * Contains all necessary information to render the payment success email
 * template
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEmailData {

    /**
     * Customer's full name
     */
    private String customerName;

    /**
     * Customer's email address (recipient)
     */
    private String email;

    /**
     * Payment order code
     */
    private String orderCode;

    /**
     * Payment date
     */
    private LocalDateTime paidDate;

    /**
     * Package name (e.g., "Pro Plan")
     */
    private String packageName;

    /**
     * Package duration in days
     */
    private Integer duration;

    /**
     * Subscription start date
     */
    private LocalDateTime subscriptionStartDate;

    /**
     * Subscription end date
     */
    private LocalDateTime subscriptionEndDate;

    /**
     * Payment amount in VND
     */
    private BigDecimal amount;

    /**
     * Transaction code from PayOS
     */
    private String transactionCode;

    /**
     * Frontend URL for CTA button
     */
    private String frontendUrl;
}
