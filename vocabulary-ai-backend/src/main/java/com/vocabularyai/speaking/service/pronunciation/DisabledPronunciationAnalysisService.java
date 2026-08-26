package com.vocabularyai.speaking.service.pronunciation;

import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * DisabledPronunciationAnalysisService
 * 
 * Placeholder implementation khi pronunciation analysis bị disable
 * 
 * Returns fixed pronunciation score (80/100) mà không thực hiện analysis thực tế
 * 
 * ConditionalOnProperty: pronunciation.provider=disabled
 */
@Service
@ConditionalOnProperty(
    name = "speaking.pronunciation.provider",
    havingValue = "disabled"
)
@Slf4j
public class DisabledPronunciationAnalysisService implements PronunciationAnalysisService {
    
    @Override
    public PronunciationAnalysisResult analyzePronunciation(
            String audioFilePath,
            String transcript,
            String topic,
            String level,
            Long userId) {
        
        log.info("Pronunciation analysis is disabled - returning placeholder result");
        
        // Return placeholder result
        return PronunciationAnalysisResult.builder()
                .pronunciationScore(null)  // null để placeholder
                .feedback("Pronunciation analysis is not enabled in this deployment")
                .stressFeedback("")
                .intonationFeedback("")
                .wordLevelIssues(null)
                .suggestions(null)
                .analysisConfidence(0.0)
                .provider("disabled")
                .build();
    }
    
    @Override
    public boolean isProviderAvailable() {
        return false;
    }
    
    @Override
    public String getProviderName() {
        return "disabled";
    }
}
