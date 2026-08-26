package com.vocabularyai.personalization.service;

/**
 * DailyPracticeException
 * 
 * Được ném khi tạo gợi ý hoạt động hàng ngày thất bại
 */
public class DailyPracticeException extends RuntimeException {
    
    public DailyPracticeException(String message) {
        super(message);
    }
    
    public DailyPracticeException(String message, Throwable cause) {
        super(message, cause);
    }
}
