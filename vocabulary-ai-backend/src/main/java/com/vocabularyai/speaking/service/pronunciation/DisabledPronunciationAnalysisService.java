package com.vocabularyai.speaking.service.pronunciation;

import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * No-op Pronunciation Analysis service.
 * Used when pronunciation analysis is disabled.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "speaking.pronunciation.provider", havingValue = "disabled", matchIfMissing = true)
public class DisabledPronunciationAnalysisService implements PronunciationAnalysisService {

    @Override
    public PronunciationAnalysisResult analyzePronunciation(String audioUrl, String transcript, String topic, String level, Long userId) {
        log.debug("Pronunciation analysis is disabled");
        return buildDisabledResult();
    }

    @Override
    public boolean isProviderAvailable() {
        return false;
    }

    @Override
    public String getProviderType() {
        return "disabled";
    }

    @Override
    public boolean isHealthy() {
        return true; // Disabled service is always "healthy"
    }

    private PronunciationAnalysisResult buildDisabledResult() {
        return PronunciationAnalysisResult.builder()
            .pronunciationScore(0.0)
            .feedback("Pronunciation analysis service is disabled")
            .stressFeedback(null)
            .intonationFeedback(null)
            .analysisConfidence(0.0)
            .provider("disabled")
            .build();
    }
}
