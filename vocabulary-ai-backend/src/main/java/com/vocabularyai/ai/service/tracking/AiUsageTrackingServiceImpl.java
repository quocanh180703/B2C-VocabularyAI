package com.vocabularyai.ai.service.tracking;

import com.vocabularyai.ai.entity.AiRequestLog;
import com.vocabularyai.ai.entity.AiRequestLogStatus;
import com.vocabularyai.ai.repository.AiRequestLogRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiUsageTrackingServiceImpl implements AiUsageTrackingService {

    private final AiRequestLogRepository aiRequestLogRepository;
    private final UserRepository userRepository;

    @Value("${ai.pricing.prompt-rate-per-million:0.075}")
    private double promptRatePerMillion;

    @Value("${ai.pricing.completion-rate-per-million:0.30}")
    private double completionRatePerMillion;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLog(
            Long userId,
            String feature,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            AiRequestLogStatus status,
            String errorMessage
    ) {
        try {
            int totalTokens = promptTokens + completionTokens;

            // Calculate estimated USD cost
            double promptCost = (promptTokens / 1_000_000.0) * promptRatePerMillion;
            double completionCost = (completionTokens / 1_000_000.0) * completionRatePerMillion;
            BigDecimal totalCost = BigDecimal.valueOf(promptCost + completionCost).setScale(6, RoundingMode.HALF_UP);

            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }

            AiRequestLog logEntry = AiRequestLog.builder()
                    .user(user)
                    .feature(feature != null ? feature : "GENERAL")
                    .model(model != null ? model : "gemini-2.5-flash")
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .estimatedCostUsd(totalCost)
                    .latencyMs(latencyMs)
                    .status(status != null ? status : AiRequestLogStatus.SUCCESS)
                    .errorMessage(errorMessage != null && errorMessage.length() > 500 ? errorMessage.substring(0, 497) + "..." : errorMessage)
                    .createdAt(LocalDateTime.now())
                    .build();

            aiRequestLogRepository.save(logEntry);
            log.debug("[AI USAGE] Recorded log: feature={}, tokens={}, costUsd={}, latency={}ms",
                    feature, totalTokens, totalCost, latencyMs);

        } catch (Exception e) {
            log.warn("[AI USAGE] Failed to record AI usage log: {}", e.getMessage());
        }
    }
}
