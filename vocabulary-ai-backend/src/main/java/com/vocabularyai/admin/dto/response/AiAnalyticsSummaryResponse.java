package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalyticsSummaryResponse {

    private long totalRequests;
    private long totalTokens;
    private long promptTokens;
    private long completionTokens;
    private BigDecimal estimatedCostUsd;
    private long estimatedCostVnd;
    private double averageLatencyMs;
    private double successRate;
    private long errorCount;
    private String activeModel;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    private List<FeatureBreakdown> featureBreakdown;
    private List<DailyTrend> dailyTrend;
    private List<AnomalyItem> anomalies;
    private List<String> optimizationTips;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureBreakdown {
        private String feature;
        private String displayName;
        private long requestCount;
        private long totalTokens;
        private BigDecimal costUsd;
        private double avgLatencyMs;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend {
        private String date;
        private long requests;
        private long tokens;
        private BigDecimal costUsd;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyItem {
        private Long id;
        private String feature;
        private int totalTokens;
        private long latencyMs;
        private String status;
        private String reason;
        private LocalDateTime createdAt;
    }
}
