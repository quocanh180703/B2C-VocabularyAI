package com.vocabularyai.speaking.service.analysis;

/**
 * SpeakingAIAnalysisException
 * 
 * Custom exception for Speaking AI Analysis Service errors
 * Raised when:
 * - Gemini API call fails
 * - JSON response parsing fails
 * - Invalid analysis results (scores outside 0-100)
 * - Malformed feedback response
 */
public class SpeakingAIAnalysisException extends RuntimeException {
    
    public SpeakingAIAnalysisException(String message) {
        super(message);
    }
    
    public SpeakingAIAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
