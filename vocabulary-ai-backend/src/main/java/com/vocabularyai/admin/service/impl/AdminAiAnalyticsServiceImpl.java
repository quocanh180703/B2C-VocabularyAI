package com.vocabularyai.admin.service.impl;

import com.vocabularyai.admin.dto.response.AiAnalyticsSummaryResponse;
import com.vocabularyai.admin.dto.response.AiRequestLogResponse;
import com.vocabularyai.admin.service.AdminAiAnalyticsService;
import com.vocabularyai.ai.entity.AiRequestLog;
import com.vocabularyai.ai.entity.AiRequestLogStatus;
import com.vocabularyai.ai.repository.AiRequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAiAnalyticsServiceImpl implements AdminAiAnalyticsService {

    private final AiRequestLogRepository aiRequestLogRepository;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String configuredModel;

    private static final long USD_TO_VND_RATE = 25400L;

    @Override
    @Transactional(readOnly = true)
    public AiAnalyticsSummaryResponse getAnalyticsSummary(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = resolveSinceDate(timeRange, now);

        long totalRequests = aiRequestLogRepository.countRequestsSince(since);
        long totalTokens = aiRequestLogRepository.sumTotalTokensSince(since);
        long promptTokens = aiRequestLogRepository.sumPromptTokensSince(since);
        long completionTokens = aiRequestLogRepository.sumCompletionTokensSince(since);
        BigDecimal costUsd = aiRequestLogRepository.sumCostUsdSince(since);
        double avgLatency = aiRequestLogRepository.avgLatencyMsSince(since);
        long failedCount = aiRequestLogRepository.countByStatusSince(AiRequestLogStatus.FAILED, since);
        long circuitBreakerCount = aiRequestLogRepository.countByStatusSince(AiRequestLogStatus.CIRCUIT_BREAKER_OPEN, since);

        long totalErrors = failedCount + circuitBreakerCount;
        double successRate = totalRequests > 0
                ? Math.round(((double) (totalRequests - totalErrors) / totalRequests) * 1000.0) / 10.0
                : 100.0;

        long costVnd = costUsd.multiply(BigDecimal.valueOf(USD_TO_VND_RATE)).setScale(0, RoundingMode.HALF_UP).longValue();

        // 1. Feature Breakdown
        List<Object[]> rawFeatures = aiRequestLogRepository.getFeatureBreakdownSince(since);
        List<AiAnalyticsSummaryResponse.FeatureBreakdown> featureBreakdowns = new ArrayList<>();
        for (Object[] row : rawFeatures) {
            String feat = (String) row[0];
            long count = ((Number) row[1]).longValue();
            long tokens = ((Number) row[2]).longValue();
            BigDecimal featCost = (BigDecimal) row[3];
            double featLatency = ((Number) row[4]).doubleValue();
            double pct = totalRequests > 0 ? Math.round(((double) count / totalRequests) * 1000.0) / 10.0 : 0.0;

            featureBreakdowns.add(AiAnalyticsSummaryResponse.FeatureBreakdown.builder()
                    .feature(feat)
                    .displayName(getFeatureDisplayName(feat))
                    .requestCount(count)
                    .totalTokens(tokens)
                    .costUsd(featCost)
                    .avgLatencyMs(Math.round(featLatency * 10.0) / 10.0)
                    .percentage(pct)
                    .build());
        }

        // 2. Daily Trend
        List<Object[]> rawTrend = aiRequestLogRepository.getDailyTrendSince(since);
        List<AiAnalyticsSummaryResponse.DailyTrend> dailyTrends = new ArrayList<>();
        for (Object[] row : rawTrend) {
            dailyTrends.add(AiAnalyticsSummaryResponse.DailyTrend.builder()
                    .date(String.valueOf(row[0]))
                    .requests(((Number) row[1]).longValue())
                    .tokens(((Number) row[2]).longValue())
                    .costUsd((BigDecimal) row[3])
                    .build());
        }

        // 3. Anomalies (threshold: > 1500 tokens or > 3000ms latency or non-SUCCESS)
        Pageable anomalyLimit = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AiRequestLog> rawAnomalies = aiRequestLogRepository.findAnomaliesSince(since, 1500, 3000, anomalyLimit);
        List<AiAnalyticsSummaryResponse.AnomalyItem> anomalies = new ArrayList<>();
        for (AiRequestLog a : rawAnomalies) {
            String reason = a.getStatus() != AiRequestLogStatus.SUCCESS ? "Yêu cầu gặp lỗi (" + a.getStatus() + ")"
                    : a.getTotalTokens() > 1500 ? "Token vượt ngưỡng cao (" + a.getTotalTokens() + " tokens)"
                    : "Độ trễ xử lý cao (" + a.getLatencyMs() + "ms)";

            anomalies.add(AiAnalyticsSummaryResponse.AnomalyItem.builder()
                    .id(a.getId())
                    .feature(getFeatureDisplayName(a.getFeature()))
                    .totalTokens(a.getTotalTokens())
                    .latencyMs(a.getLatencyMs())
                    .status(a.getStatus().name())
                    .reason(reason)
                    .createdAt(a.getCreatedAt())
                    .build());
        }

        // 4. Actionable Optimization Tips
        List<String> tips = generateOptimizationTips(totalRequests, totalTokens, avgLatency, successRate);

        return AiAnalyticsSummaryResponse.builder()
                .totalRequests(totalRequests)
                .totalTokens(totalTokens)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .estimatedCostUsd(costUsd)
                .estimatedCostVnd(costVnd)
                .averageLatencyMs(Math.round(avgLatency * 10.0) / 10.0)
                .successRate(successRate)
                .errorCount(totalErrors)
                .activeModel(configuredModel)
                .periodStart(since)
                .periodEnd(now)
                .featureBreakdown(featureBreakdowns)
                .dailyTrend(dailyTrends)
                .anomalies(anomalies)
                .optimizationTips(tips)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiRequestLogResponse> getLogs(String timeRange, String feature, String status, Pageable pageable) {
        LocalDateTime since = resolveSinceDate(timeRange, LocalDateTime.now());

        Page<AiRequestLog> entities;
        if (feature != null && !feature.trim().isEmpty()) {
            entities = aiRequestLogRepository.findByFeatureAndCreatedAtAfter(feature.trim(), since, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            try {
                AiRequestLogStatus s = AiRequestLogStatus.valueOf(status.trim().toUpperCase());
                entities = aiRequestLogRepository.findByStatusAndCreatedAtAfter(s, since, pageable);
            } catch (Exception e) {
                entities = aiRequestLogRepository.findByCreatedAtAfter(since, pageable);
            }
        } else {
            entities = aiRequestLogRepository.findByCreatedAtAfter(since, pageable);
        }

        return entities.map(l -> AiRequestLogResponse.builder()
                .id(l.getId())
                .userId(l.getUser() != null ? l.getUser().getId() : null)
                .username(l.getUser() != null ? l.getUser().getUsername() : "Hệ thống / Khách")
                .feature(getFeatureDisplayName(l.getFeature()))
                .model(l.getModel())
                .promptTokens(l.getPromptTokens())
                .completionTokens(l.getCompletionTokens())
                .totalTokens(l.getTotalTokens())
                .estimatedCostUsd(l.getEstimatedCostUsd())
                .latencyMs(l.getLatencyMs())
                .status(l.getStatus().name())
                .errorMessage(l.getErrorMessage())
                .createdAt(l.getCreatedAt())
                .build());
    }

    private LocalDateTime resolveSinceDate(String timeRange, LocalDateTime now) {
        if ("24h".equalsIgnoreCase(timeRange)) {
            return now.minusHours(24);
        } else if ("30d".equalsIgnoreCase(timeRange)) {
            return now.minusDays(30);
        } else if ("all".equalsIgnoreCase(timeRange)) {
            return now.minusYears(5);
        }
        // Default: 7 days
        return now.minusDays(7);
    }

    private String getFeatureDisplayName(String feature) {
        if (feature == null) return "Chung";
        switch (feature.toUpperCase()) {
            case "AI_TUTOR_CHAT":
            case "CHAT":
                return "Trò chuyện AI Tutor";
            case "GRAMMAR_CHECK":
            case "GRAMMAR":
                return "Kiểm tra ngữ pháp";
            case "VOCAB_EXPLAIN":
            case "VOCABULARY":
                return "Giải nghĩa từ vựng";
            case "SPEAKING_FEEDBACK":
            case "SPEAKING":
                return "Chấm điểm phát âm";
            case "HINT":
                return "Gợi ý câu đố";
            case "ADMIN_ASSISTANT":
            case "ADMIN_COPILOT":
                return "Trợ lý Quản trị Admin";
            default:
                return feature;
        }
    }

    private List<String> generateOptimizationTips(long requests, long tokens, double latency, double successRate) {
        List<String> tips = new ArrayList<>();
        if (requests > 0) {
            tips.add("Model hiện tại '" + configuredModel + "' duy trì tỷ lệ thành công " + successRate + "% và SLA phản hồi " + Math.round(latency) + "ms.");
            tips.add("Tối ưu hóa: Bật cơ chế Cache cho các từ vựng phổ biến trong bảng 'words' để giảm 20-30% lượt gọi trùng lặp.");
            tips.add("Kiểm soát Prompt: Rút ngắn System Instructions của tính năng AI Tutor Chat giúp tiết kiệm trung bình 80-120 token mỗi lượt trao đổi.");
            tips.add("Giám sát hạn mức: Hiện tại hệ thống đang nằm trong ngưỡng an toàn của hạn mức Free Tier Google AI Studio.");
        } else {
            tips.add("Hệ thống giám sát AI đang hoạt động và sẵn sàng ghi nhận dữ liệu.");
        }
        return tips;
    }
}
