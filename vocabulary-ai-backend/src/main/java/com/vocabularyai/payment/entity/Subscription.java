package com.vocabularyai.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Subscription Entity - Represents user's active subscription entitlement
 * 
 * Lifecycle:
 * 1. ACTIVE: Subscription is currently valid (start_at <= now <= end_at)
 * 2. EXPIRED: Subscription has ended (now > end_at)
 * 3. CANCELLED: User cancelled subscription (manual cancellation)
 * 4. SUSPENDED: Subscription suspended by system
 */
@Entity
@Table(
    name = "subscriptions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_subscription_user_id", columnNames = "user_id")
    },
    indexes = {
        @Index(name = "idx_subscription_user_id", columnList = "user_id"),
        @Index(name = "idx_subscription_status", columnList = "status"),
        @Index(name = "idx_subscription_end_at", columnList = "end_at"),
        @Index(name = "idx_subscription_package_id", columnList = "package_id")
    }
)
@Getter
@Setter
@ToString
@Builder
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User ID who owns the subscription
     * FK to users table (users.id)
     * UK constraint ensures 1 active subscription per user
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * Package ID being subscribed to
     * FK to packages table (packages.id)
     * Denormalized from payment for quick lookup
     */
    @Column(name = "package_id", nullable = false)
    private Long packageId;
    
    /**
     * Payment ID that initiated this subscription
     * FK to payments table (payments.id)
     * Links this subscription to the payment transaction
     */
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
    
    /**
     * Subscription status
     * ACTIVE: Currently valid (start_at <= now <= end_at)
     * EXPIRED: Subscription period has ended
     * CANCELLED: User cancelled
     * SUSPENDED: System suspended
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACTIVE, EXPIRED, CANCELLED, SUSPENDED
    
    /**
     * Subscription start date/time
     * When the subscription becomes effective
     */
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    
    /**
     * Subscription end date/time
     * When the subscription expires
     * Set as: startAt + package.durationDays
     */
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;
    
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
    
    public Subscription() {
    }

    public Subscription(Long id, Long userId, Long packageId, Long paymentId, String status, LocalDateTime startAt, LocalDateTime endAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.packageId = packageId;
        this.paymentId = paymentId;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
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
    
    /**
     * Check if subscription is currently active
     * Returns true if now is between startAt and endAt
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "ACTIVE".equals(this.status) && 
               !now.isBefore(this.startAt) && 
               !now.isAfter(this.endAt);
    }
}
