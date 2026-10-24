package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private String feature;
    private String model;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private BigDecimal estimatedCostUsd;
    private long latencyMs;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
