package com.vocabularyai.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity - Represents actual payment transaction from PayOS
 * 
 * Lifecycle:
 * 1. PENDING: Payment is processing
 * 2. SUCCESS: Payment successful, confirmed by PayOS
 * 3. FAILED: Payment failed
 * 4. CANCELLED: Payment cancelled
 */
@Entity
@Table(
    name = "payments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_order_id", columnNames = "payment_order_id"),
        @UniqueConstraint(name = "uk_transaction_code", columnNames = "transaction_code")
    },
    indexes = {
        @Index(name = "idx_payment_order_id", columnList = "payment_order_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_transaction_code", columnList = "transaction_code"),
        @Index(name = "idx_payment_paid_at", columnList = "paid_at")
    }
)
@Getter
@Setter
@ToString
@Builder
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Payment Order ID
     * FK to payment_orders table (payment_orders.id)
     * UK constraint ensures 1:1 relationship (one payment per order)
     */
    @Column(name = "payment_order_id", nullable = false)
    private Long paymentOrderId;
    
    /**
     * Transaction code returned by PayOS
     * Unique identifier from payment gateway
     * Used to reconcile and prevent duplicate processing
     * UK constraint ensures uniqueness
     */
    @Column(name = "transaction_code", nullable = false, length = 100)
    private String transactionCode;
    
    /**
     * Payment amount in VND
     * Should match payment_order.amount for consistency
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    /**
     * Payment status
     * PENDING: Processing
     * SUCCESS: Successful
     * FAILED: Failed
     * CANCELLED: Cancelled
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, SUCCESS, FAILED, CANCELLED
    
    /**
     * Payment method used (e.g., CREDIT_CARD, BANK_TRANSFER, WALLET)
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    /**
     * Payment confirmation time
     * When the payment was confirmed by PayOS
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public Payment() {
    }

    public Payment(Long id, Long paymentOrderId, String transactionCode, BigDecimal amount, String status, String paymentMethod, LocalDateTime paidAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.paymentOrderId = paymentOrderId;
        this.transactionCode = transactionCode;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * JPA lifecycle callback - set createdAt before persist
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    /**
     * JPA lifecycle callback - set updatedAt before update
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
