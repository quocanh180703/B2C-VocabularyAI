package com.vocabularyai.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * UserDailyUsage Entity
 * Tracks daily quota usage per user and per feature.
 */
@Entity
@Table(
    name = "user_daily_usage",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_feature_date", columnNames = {"user_id", "feature_type", "usage_date"})
    },
    indexes = {
        @Index(name = "idx_user_daily_usage_lookup", columnList = "user_id, usage_date")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", nullable = false, length = 20)
    private FeatureType featureType;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private int usageCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
