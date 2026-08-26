package com.vocabularyai.personalization.service;

import com.vocabularyai.personalization.dto.RecommendationDto;

import java.util.List;

/**
 * RecommendationService
 * 
 * Tạo gợi ý học tập cá nhân hóa dựa trên phân tích điểm yếu/điểm mạnh
 * 
 * Trách nhiệm:
 * - Nhận WeaknessStrengthDto từ TASK 2
 * - Xây dựng prompt có cấu trúc cho Gemini AI
 * - Phân tích phản hồi JSON từ AI
 * - Cache kết quả với khóa userId
 * - Cung cấp các endpoint gợi ý
 * 
 * Tái sử dụng:
 * - GeminiAIClientService (Phase 7) - Engine AI
 * - PromptEngineeringService (Phase 7) - Xây dựng prompt
 * - CacheService (Phase 9) - Redis caching
 * - RateLimitService (Phase 9) - Rate limiting
 * - WeaknessStrengthService (Phase 10 TASK 2) - Dữ liệu phân tích
 * - LearningProfileService (Phase 10 TASK 1) - Dữ liệu hồ sơ
 * 
 * Redis Keys:
 * - ai:recommendations:{userId} [TTL: 3 hours]
 */
public interface RecommendationService {
    
    /**
     * Tạo gợi ý học tập từ phân tích điểm yếu/điểm mạnh
     * 
     * Quy trình:
     * 1. Xác thực người dùng tồn tại
     * 2. Kiểm tra cache cho gợi ý hiện có
     * 3. Lấy phân tích điểm yếu/điểm mạnh (TASK 2)
     * 4. Xây dựng prompt phân tích cho Gemini
     * 5. Gọi Gemini AI để tạo gợi ý
     * 6. Phân tích phản hồi JSON
     * 7. Cache kết quả
     * 8. Trả về gợi ý
     * 
     * @param userId ID người dùng (từ JWT token)
     * @return RecommendationDto với danh sách gợi ý được xếp hạng
     * @throws RecommendationException nếu tạo gợi ý thất bại
     * @throws RateLimitExceededException nếu vượt rate limit
     * @throws ResourceNotFoundException nếu không tìm thấy người dùng
     */
    RecommendationDto generateRecommendations(Long userId);
    
    /**
     * Lấy gợi ý được cache
     * 
     * Trả về gợi ý nếu cached và chưa hết hạn
     * Trả về null nếu chưa cache hoặc hết hạn
     * 
     * @param userId ID người dùng
     * @return Gợi ý được cache hoặc null
     */
    RecommendationDto getCachedRecommendations(Long userId);
    
    /**
     * Vô hiệu hóa gợi ý được cache
     * 
     * Được gọi khi:
     * - Tiến độ học tập thay đổi
     * - Người dùng yêu cầu gợi ý mới
     * - Cache hết hạn
     * 
     * @param userId ID người dùng
     */
    void invalidateCache(Long userId);
    
    /**
     * Lấy danh sách gợi ý nhanh chóng
     * 
     * Thực hiện phân tích đầy đủ nếu chưa cache
     * Lọc chỉ các item nhanh chóng
     * 
     * @param userId ID người dùng
     * @return Danh sách gợi ý nhanh chóng
     */
    List<RecommendationDto.RecommendationItem> getQuickWins(Long userId);
    
    /**
     * Lấy danh sách gợi ý dài hạn
     * 
     * Thực hiện phân tích đầy đủ nếu chưa cache
     * Lọc chỉ các mục tiêu dài hạn
     * 
     * @param userId ID người dùng
     * @return Danh sách gợi ý dài hạn
     */
    List<RecommendationDto.RecommendationItem> getLongTermGoals(Long userId);
    
    /**
     * Lấy gợi ý khẩn cấp
     * 
     * Thực hiện phân tích đầy đủ nếu chưa cache
     * Lọc chỉ gợi ý khẩn cấp (HIGH hoặc CRITICAL impact)
     * 
     * @param userId ID người dùng
     * @return Danh sách gợi ý khẩn cấp
     */
    List<RecommendationDto.RecommendationItem> getCriticalRecommendations(Long userId);
    
    /**
     * Lấy lịch trình hàng tuần
     * 
     * Thực hiện phân tích đầy đủ nếu chưa cache
     * Trả về chi tiết lịch trình cho 7 ngày
     * 
     * @param userId ID người dùng
     * @return Danh sách mục lịch trình
     */
    List<RecommendationDto.WeeklyScheduleItem> getWeeklySchedule(Long userId);
}
