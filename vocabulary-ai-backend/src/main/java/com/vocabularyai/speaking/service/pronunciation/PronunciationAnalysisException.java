package com.vocabularyai.speaking.service.pronunciation;

/**
 * PronunciationAnalysisException
 * 
 * Custom exception cho Pronunciation Analysis Service
 * 
 * Được raise khi:
 * - Provider không khả dụng
 * - Audio file không hợp lệ
 * - Gemini API call thất bại
 * - JSON response parsing thất bại
 * - Pronunciation assessment không thành công
 */
public class PronunciationAnalysisException extends RuntimeException {
    
    public PronunciationAnalysisException(String message) {
        super(message);
    }
    
    public PronunciationAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
