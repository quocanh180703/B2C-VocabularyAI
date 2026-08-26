package com.vocabularyai.personalization.service;

import com.vocabularyai.personalization.dto.LearningPlanDto;

import java.util.List;

/**
 * LearningPlanService
 * 
 * Tạo kế hoạch học tập 4 tuần có cấu trúc từ gợi ý
 * 
 * Trách nhiệm:
 * - Nhận RecommendationDto từ TASK 3
 * - Xây dựng prompt có cấu trúc cho Gemini AI
 * - Phân tích phản hồi JSON từ AI
 * - Cache kết quả với khóa userId
 * - Cung cấp các endpoint kế hoạch
 * 
 * Tái sử dụng:
 * - GeminiAIClientService (Phase 7) - Engine AI
 * - PromptEngineeringService (Phase 7) - Xây dựng prompt
 * - CacheService (Phase 9) - Redis caching
 * - RateLimitService (Phase 9) - Rate limiting
 * - RecommendationService (Phase 10 TASK 3) - Dữ liệu gợi ý
 * - WeaknessStrengthService (Phase 10 TASK 2) - Dữ liệu phân tích
 * 
 * Redis Keys:
 * - ai:learning-plan:{userId} [TTL: 7 days]
 */
public interface LearningPlanService {
    
    /**
     * Tạo kế hoạch học tập 4 tuần từ gợi ý
     * 
     * Quy trình:
     * 1. Xác thực người dùng tồn tại
     * 2. Kiểm tra cache cho kế hoạch hiện có
     * 3. Lấy gợi ý (TASK 3)
     * 4. Xây dựng prompt phân tích cho Gemini
     * 5. Gọi Gemini AI để tạo kế hoạch
     * 6. Phân tích phản hồi JSON
     * 7. Cache kết quả
     * 8. Trả về kế hoạch
     * 
     * @param userId ID người dùng (từ JWT token)
     * @return LearningPlanDto với kế hoạch 4 tuần chi tiết
     * @throws LearningPlanException nếu tạo kế hoạch thất bại
     * @throws RateLimitExceededException nếu vượt rate limit
     * @throws ResourceNotFoundException nếu không tìm thấy người dùng
     */
    LearningPlanDto createLearningPlan(Long userId);
    
    /**
     * Lấy kế hoạch được cache
     * 
     * Trả về kế hoạch nếu cached và chưa hết hạn
     * Trả về null nếu chưa cache hoặc hết hạn
     * 
     * @param userId ID người dùng
     * @return Kế hoạch được cache hoặc null
     */
    LearningPlanDto getCachedPlan(Long userId);
    
    /**
     * Vô hiệu hóa kế hoạch được cache
     * 
     * Được gọi khi:
     * - Tiến độ học tập thay đổi đáng kể
     * - Người dùng yêu cầu kế hoạch mới
     * - Cache hết hạn
     * 
     * @param userId ID người dùng
     */
    void invalidateCache(Long userId);
    
    /**
     * Lấy danh sách các tuần trong kế hoạch
     * 
     * Thực hiện tạo kế hoạch đầy đủ nếu chưa cache
     * 
     * @param userId ID người dùng
     * @return Danh sách 4 tuần
     */
    List<LearningPlanDto.WeekPlanDto> getWeeklyBreakdown(Long userId);
    
    /**
     * Lấy các cột mốc (milestone) của kế hoạch
     * 
     * Thực hiện tạo kế hoạch đầy đủ nếu chưa cache
     * 
     * @param userId ID người dùng
     * @return Danh sách các cột mốc
     */
    List<LearningPlanDto.MilestoneDto> getMilestones(Long userId);
    
    /**
     * Lấy chi tiết các nhiệm vụ cho một tuần cụ thể
     * 
     * @param userId ID người dùng
     * @param weekNumber Số tuần (1-4)
     * @return Danh sách ngày với các nhiệm vụ
     */
    List<LearningPlanDto.DayPlanDto> getWeekDetails(Long userId, int weekNumber);
    
    /**
     * Lấy chi tiết nhiệm vụ cho một ngày cụ thể
     * 
     * @param userId ID người dùng
     * @param weekNumber Số tuần (1-4)
     * @param dayOfWeek Ngày trong tuần (MONDAY, TUESDAY, ...)
     * @return Chi tiết ngày với danh sách nhiệm vụ
     */
    LearningPlanDto.DayPlanDto getDayDetails(Long userId, int weekNumber, String dayOfWeek);
}
