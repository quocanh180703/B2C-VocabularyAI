package com.vocabularyai.personalization.service;

import com.vocabularyai.personalization.dto.DailyPracticeDto;

/**
 * DailyPracticeService
 * 
 * Tạo gợi ý hoạt động học tập cho hôm nay từ kế hoạch 4 tuần
 */
public interface DailyPracticeService {
    
    /**
     * Lấy gợi ý hoạt động cho hôm nay
     * 
     * @param userId ID người dùng
     * @return DailyPracticeDto với hoạt động hôm nay
     * @throws DailyPracticeException nếu tạo thất bại
     * @throws RateLimitExceededException nếu vượt rate limit
     * @throws ResourceNotFoundException nếu không tìm người dùng
     */
    DailyPracticeDto getDailyPractice(Long userId);
    
    /**
     * Lấy gợi ý cho một ngày cụ thể
     * 
     * @param userId ID người dùng
     * @param weekNumber Số tuần (1-4)
     * @param dayOfWeek Ngày trong tuần
     * @return DailyPracticeDto
     */
    DailyPracticeDto getDailyPracticeForDay(Long userId, int weekNumber, String dayOfWeek);
    
    /**
     * Đánh dấu hoàn thành hoạt động hôm nay
     * 
     * @param userId ID người dùng
     * @param completed Đã hoàn thành hay chưa
     */
    void markDayAsCompleted(Long userId, boolean completed);
    
    /**
     * Lưu ghi chú cho hôm nay
     * 
     * @param userId ID người dùng
     * @param notes Ghi chú
     */
    void saveDailyNotes(Long userId, String notes);
    
    /**
     * TASK 11: Invalidate all cached daily practice data for a user
     * Called when user status changes (BANNED, INACTIVE)
     * 
     * @param userId ID người dùng
     */
    void invalidateCache(Long userId);
}
