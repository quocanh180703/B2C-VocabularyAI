package com.vocabularyai.personalization.service;

import com.vocabularyai.personalization.dto.ProgressAnalysisDto;

import java.util.List;

/**
 * ProgressAnalysisService
 * 
 * Phân tích tiến độ hướng tới mục tiêu
 * 
 * Trách nhiệm:
 * - Theo dõi tiến độ kế hoạch học tập
 * - Tính toán velocity (tốc độ cải thiện)
 * - Dự đoán ngày hoàn thành
 * - Gợi ý điều chỉnh nếu cần
 * - Cache kết quả với TTL dài (1 ngày)
 * 
 * Redis Keys:
 * - ai:progress-analysis:{userId} [TTL: 1 day]
 * - ai:progress-history:{userId} [TTL: unlimited]
 */
public interface ProgressAnalysisService {
    
    /**
     * Phân tích tiến độ của người dùng
     * 
     * Quy trình:
     * 1. Xác thực người dùng tồn tại
     * 2. Kiểm tra cache
     * 3. Lấy lịch sử học tập (7-30 ngày gần đây)
     * 4. Lấy kế hoạch hiện tại
     * 5. Tính toán tiến độ, velocity, dự đoán
     * 6. Gọi Gemini AI để phân tích và gợi ý
     * 7. Cache kết quả (1 ngày)
     * 8. Trả về phân tích
     * 
     * @param userId ID người dùng (từ JWT token)
     * @return ProgressAnalysisDto với phân tích tiến độ
     * @throws ProgressAnalysisException nếu phân tích thất bại
     * @throws RateLimitExceededException nếu vượt rate limit
     * @throws ResourceNotFoundException nếu không tìm thấy người dùng
     */
    ProgressAnalysisDto analyzeProgress(Long userId);
    
    /**
     * Lấy phân tích tiến độ từ cache (nếu có)
     * 
     * @param userId ID người dùng
     * @return ProgressAnalysisDto hoặc null nếu chưa cache
     */
    ProgressAnalysisDto getCachedAnalysis(Long userId);
    
    /**
     * Vô hiệu hóa cache phân tích
     * 
     * @param userId ID người dùng
     */
    void invalidateCache(Long userId);
    
    /**
     * Lấy tiến độ hoàn thành kế hoạch (%)
     * 
     * @param userId ID người dùng
     * @return Tỉ lệ hoàn thành (0-100)
     */
    Double getCompletionPercentage(Long userId);
    
    /**
     * Lấy ngày dự kiến hoàn thành kế hoạch
     * 
     * @param userId ID người dùng
     * @return LocalDateTime hoặc null
     */
    java.time.LocalDateTime getEstimatedCompletionDate(Long userId);
    
    /**
     * Lấy danh sách các milestone đã hoàn thành
     * 
     * @param userId ID người dùng
     * @return Danh sách milestones
     */
    List<ProgressAnalysisDto.MilestoneProgressItem> getCompletedMilestones(Long userId);
    
    /**
     * Lấy milestone hiện tại
     * 
     * @param userId ID người dùng
     * @return Milestone hiện tại hoặc null
     */
    ProgressAnalysisDto.MilestoneProgressItem getCurrentMilestone(Long userId);
    
    /**
     * Lấy milestone tiếp theo
     * 
     * @param userId ID người dùng
     * @return Milestone tiếp theo hoặc null
     */
    ProgressAnalysisDto.MilestoneProgressItem getNextMilestone(Long userId);
    
    /**
     * Tính toán velocity (tốc độ cải thiện)
     * 
     * @param userId ID người dùng
     * @return VelocityAnalysis
     */
    ProgressAnalysisDto.VelocityAnalysis calculateVelocity(Long userId);
    
    /**
     * Lấy số ngày sớm/muộn so với kế hoạch
     * 
     * @param userId ID người dùng
     * @return Số ngày (dương = sớm, âm = muộn)
     */
    Integer getDaysAheadOrBehind(Long userId);
    
    /**
     * Lấy xác suất hoàn thành đúng hạn (%)
     * 
     * @param userId ID người dùng
     * @return Xác suất (0-100)
     */
    Double getOnTimeCompletionProbability(Long userId);
}
