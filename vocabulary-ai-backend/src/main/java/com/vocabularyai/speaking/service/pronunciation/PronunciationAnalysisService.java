package com.vocabularyai.speaking.service.pronunciation;

import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult;

/**
 * Interface for Pronunciation Analysis services.
 * Analyzes and compares user pronunciation against reference.
 */
public interface PronunciationAnalysisService {

    /**
     * Analyze pronunciation based on audio URL and transcript
     *
     * @param audioUrl URL/path to audio file
     * @param transcript The user's transcribed speech
     * @param topic The speaking topic/reference text
     * @param level Speaking level (A1-C2)
     * @param userId User ID (for context)
     * @return PronunciationAnalysisResult with detailed feedback
     * @throws PronunciationAnalysisException if analysis fails
     */
    PronunciationAnalysisResult analyzePronunciation(String audioUrl, String transcript, String topic, String level, Long userId);

    /**
     * Check if pronunciation analysis provider is available
     *
     * @return true if provider is configured and not disabled
     */
    boolean isProviderAvailable();

    /**
     * Get the analysis provider type
     *
     * @return Provider name (gemini, google_cloud, azure, aws, disabled)
     */
    String getProviderType();

    /**
     * Check if pronunciation analysis service is healthy
     *
     * @return true if service is available, false otherwise
     */
    boolean isHealthy();
}
