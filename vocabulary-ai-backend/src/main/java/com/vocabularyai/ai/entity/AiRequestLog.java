package com.vocabularyai.ai.entity;

import com.vocabularyai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for tracking AI calls, token consumption, latency, and costs
 * Table: ai_request_logs
 */
@Entity
@Table(name = "ai_request_logs", indexes = {
    @Index(name = "idx_ai_request_logs_created_at", columnList = "created_at"),
    @Index(name = "idx_ai_request_logs_feature", columnList = "feature"),
    @Index(name = "idx_ai_request_logs_status", columnList = "status"),
    @Index(name = "idx_ai_request_logs_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "feature", nullable = false, length = 50)
    private String feature;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private Integer promptTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private Integer completionTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private Integer totalTokens = 0;

    @Column(name = "estimated_cost_usd", nullable = false, precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal estimatedCostUsd = BigDecimal.ZERO;

    @Column(name = "latency_ms", nullable = false)
    @Builder.Default
    private Long latencyMs = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AiRequestLogStatus status = AiRequestLogStatus.SUCCESS;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
