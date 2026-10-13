package com.vocabularyai.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Package Entity - Represents subscription packages available for purchase
 * 
 * V1 Packages:
 * - PREMIUM_MONTHLY: 99,000 VND/month
 * - PREMIUM_YEARLY: 899,000 VND/year
 */
@Entity
@Table(
    name = "packages",
    uniqueConstraints = @UniqueConstraint(name = "uk_package_code", columnNames = "code"),
    indexes = {
        @Index(name = "idx_package_status", columnList = "status"),
        @Index(name = "idx_package_code", columnList = "code")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Package code - Unique identifier (e.g., PREMIUM_MONTHLY, PREMIUM_YEARLY)
     * UK constraint ensures uniqueness
     */
    @Column(name = "code", nullable = false, length = 50)
    private String code;
    
    /**
     * Package name (e.g., "Premium Monthly", "Premium Yearly")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * Package price in VND (Vietnamese Dong)
     * PREMIUM_MONTHLY: 99,000
     * PREMIUM_YEARLY: 899,000
     */
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
    
    /**
     * Subscription duration in days
     * PREMIUM_MONTHLY: 30 (approximately 1 month)
     * PREMIUM_YEARLY: 365
     */
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;
    
    /**
     * Package status
     * ACTIVE: Available for purchase
     * INACTIVE: Not available
     * ARCHIVED: Discontinued (kept for historical records)
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACTIVE, INACTIVE, ARCHIVED
    
    /**
     * Description of the package (optional)
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
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
