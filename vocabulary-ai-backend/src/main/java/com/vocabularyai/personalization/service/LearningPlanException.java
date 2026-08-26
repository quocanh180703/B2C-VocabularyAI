package com.vocabularyai.personalization.service;

/**
 * LearningPlanException
 * 
 * Được ném khi tạo kế hoạch học tập thất bại:
 * - Gemini AI service không khả dụng
 * - Timeout phân tích
 * - Dữ liệu gợi ý không hợp lệ
 * - Lỗi JSON parsing
 * - Rate limit vượt quá
 */
public class LearningPlanException extends RuntimeException {
    
    public LearningPlanException(String message) {
        super(message);
    }
    
    public LearningPlanException(String message, Throwable cause) {
        super(message, cause);
    }
}
