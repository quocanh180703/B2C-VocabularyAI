package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.AiAnalyticsSummaryResponse;
import com.vocabularyai.admin.dto.response.AiRequestLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminAiAnalyticsService {

    /**
     * Get aggregated summary for AI Usage & Cost Optimizer
     * 
     * @param timeRange '24h', '7d', '30d', or 'all'
     * @return Aggregated analytics with metrics, feature distribution, daily trend and tips
     */
    AiAnalyticsSummaryResponse getAnalyticsSummary(String timeRange);

    /**
     * Get paginated logs with optional filters
     */
    Page<AiRequestLogResponse> getLogs(String timeRange, String feature, String status, Pageable pageable);
}
