package com.vocabularyai.personalization.service;

/**
 * RecommendationException
 * 
 * Được ném khi tạo gợi ý học tập thất bại:
 * - Gemini AI service không khả dụng
 * - Timeout phân tích
 * - Dữ liệu phân tích không hợp lệ
 * - Lỗi JSON parsing
 * - Rate limit vượt quá
 */
public class RecommendationException extends RuntimeException {
    
    public RecommendationException(String message) {
        super(message);
    }
    
    public RecommendationException(String message, Throwable cause) {
        super(message, cause);
    }
}
