package com.vocabularyai.ai.service.tracking;

import com.vocabularyai.ai.entity.AiRequestLogStatus;

public interface AiUsageTrackingService {

    /**
     * Asynchronously records an AI request log with token counts, latency, and estimated cost
     */
    void recordLog(
            Long userId,
            String feature,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            AiRequestLogStatus status,
            String errorMessage
    );
}
