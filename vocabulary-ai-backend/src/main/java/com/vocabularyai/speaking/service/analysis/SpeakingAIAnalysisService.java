package com.vocabularyai.speaking.service.analysis;

import com.vocabularyai.speaking.service.analysis.dto.SpeakingAnalysisResult;

/**
 * SpeakingAIAnalysisService
 * 
 * Dịch vụ phân tích AI cho bài nói
 * 
 * Orchestrates the flow:
 * 1. Accept transcript from SpeechToTextService
 * 2. Call buildSpeakingAnalysisPrompt() and send to GeminiAIClientService
 * 3. Parse AI scores response (grammar, vocabulary, fluency, content, overall)
 * 4. Call buildSpeakingFeedbackPrompt() with scores as context
 * 5. Parse detailed feedback response
 * 6. Combine all results and save to SpeakingFeedback entity
 * 7. Return orchestration result DTO
 */
public interface SpeakingAIAnalysisService {
    
    /**
     * Analyze a speaking attempt with AI
     * 
     * Performs comprehensive analysis:
     * - Grammar evaluation
     * - Vocabulary assessment
     * - Fluency analysis
     * - Content relevance check
     * - Detailed feedback generation
     * - Error correction suggestions
     * 
     * @param transcript Text from Speech-to-Text service
     * @param topic Speaking topic/theme
     * @param level User's English level (A1-C2)
     * @param attemptId Speaking attempt ID
     * @param sessionId Speaking session ID
     * @param userId User ID (for rate limiting and ownership verification)
     * @return SpeakingAnalysisResult with all analysis data
     * @throws SpeakingAIAnalysisException if analysis fails
     * @throws com.vocabularyai.common.exception.RateLimitExceededException if rate limit exceeded
     */
    SpeakingAnalysisResult analyzeSpeakingAttempt(
            String transcript,
            String topic,
            String level,
            Long attemptId,
            Long sessionId,
            Long userId
    );
}
