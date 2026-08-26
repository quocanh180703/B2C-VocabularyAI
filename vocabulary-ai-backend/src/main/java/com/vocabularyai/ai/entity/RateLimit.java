package com.vocabularyai.ai.entity;

import com.vocabularyai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RateLimit Entity
 * 
 * Maps to 'rate_limits' table in MySQL
 * Tracks API usage and token consumption per user within time periods
 */
@Entity
@Table(name = "rate_limits", indexes = {
    @Index(name = "idx_rate_limits_user_id", columnList = "user_id"),
    @Index(name = "idx_rate_limits_user_period", columnList = "user_id, period_start", unique = false),
    @Index(name = "idx_rate_limits_period", columnList = "period_start, period_end")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "createdAt", "updatedAt"})
@ToString(exclude = {"user"})
public class RateLimit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "tokens_used", nullable = false)
    @Builder.Default
    private Long tokensUsed = 0L;
    
    @Column(name = "request_count", nullable = false)
    @Builder.Default
    private Integer requestCount = 0;
    
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if rate limit is exceeded
     * 
     * @param dailyTokenLimit Maximum tokens allowed per day
     * @param dailyRequestLimit Maximum requests allowed per day
     * @return true if limit exceeded, false otherwise
     */
    public boolean isLimitExceeded(long dailyTokenLimit, int dailyRequestLimit) {
        return tokensUsed >= dailyTokenLimit || requestCount >= dailyRequestLimit;
    }
    
    /**
     * Check if period is still valid
     */
    public boolean isPeriodValid() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(periodStart) && now.isBefore(periodEnd);
    }
    
    /**
     * Get remaining tokens for the period
     */
    public long getRemainingTokens(long dailyTokenLimit) {
        return Math.max(0, dailyTokenLimit - tokensUsed);
    }
    
    /**
     * Get remaining requests for the period
     */
    public int getRemainingRequests(int dailyRequestLimit) {
        return Math.max(0, dailyRequestLimit - requestCount);
    }
}
