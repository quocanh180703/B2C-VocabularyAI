package com.vocabularyai.speaking.service;

/**
 * SpeakingPracticeException
 * 
 * Custom exception cho SpeakingPracticeService
 * 
 * Được raise khi:
 * - Audio file không hợp lệ (size, format, duration)
 * - AudioStorageService lỗi
 * - SpeechToTextService lỗi
 * - SpeakingAIAnalysisService lỗi
 * - Session không tồn tại hoặc user không sở hữu
 * - Rate limit exceeded
 * - Transaction rollback
 */
public class SpeakingPracticeException extends RuntimeException {
    
    public SpeakingPracticeException(String message) {
        super(message);
    }
    
    public SpeakingPracticeException(String message, Throwable cause) {
        super(message, cause);
    }
}
