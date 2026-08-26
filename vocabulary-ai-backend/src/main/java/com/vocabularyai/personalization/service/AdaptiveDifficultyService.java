package com.vocabularyai.personalization.service;

import com.vocabularyai.personalization.dto.AdaptiveDifficultyDto;

/**
 * AdaptiveDifficultyService
 * 
 * Điều chỉnh độ khó tự động dựa trên hiệu suất
 * 
 * Trách nhiệm:
 * - Theo dõi hiệu suất người dùng từ lịch sử hoạt động
 * - Tính toán độ khó tối ưu cho từng chủ đề
 * - Sử dụng Gemini AI để phân tích xu hướng
 * - Gợi ý điều chỉnh độ khó
 * - Cache kết quả với TTL dài (30 ngày)
 * 
 * Redis Keys:
 * - ai:adaptive-difficulty:{userId} [TTL: 30 days]
 * - ai:difficulty-profile:{userId} [TTL: 30 days]
 */
public interface AdaptiveDifficultyService {
    
    /**
     * Phân tích hiệu suất và gợi ý điều chỉnh độ khó
     * 
     * Quy trình:
     * 1. Xác thực người dùng tồn tại
     * 2. Kiểm tra cache
     * 3. Lấy lịch sử hoạt động/scores gần đây
     * 4. Tính toán hiệu suất theo chủ đề
     * 5. Gọi Gemini AI để phân tích và gợi ý
     * 6. Phân tích phản hồi JSON
     * 7. Cache kết quả (30 ngày)
     * 8. Trả về gợi ý điều chỉnh
     * 
     * @param userId ID người dùng (từ JWT token)
     * @return AdaptiveDifficultyDto với gợi ý điều chỉnh
     * @throws AdaptiveDifficultyException nếu phân tích thất bại
     * @throws RateLimitExceededException nếu vượt rate limit
     * @throws ResourceNotFoundException nếu không tìm thấy người dùng
     */
    AdaptiveDifficultyDto analyzeAndRecommendDifficulty(Long userId);
    
    /**
     * Ghi nhận điều chỉnh độ khó
     * 
     * Được gọi khi người dùng hoặc hệ thống thực hiện điều chỉnh
     * 
     * @param userId ID người dùng
     * @param topic Chủ đề (VOCABULARY, GRAMMAR, SPEAKING, ...)
     * @param oldDifficulty Độ khó cũ (1-10)
     * @param newDifficulty Độ khó mới (1-10)
     * @param reason Lý do điều chỉnh
     */
    void recordDifficultyAdjustment(Long userId, String topic, Double oldDifficulty, 
                                     Double newDifficulty, String reason);
    
    /**
     * Lấy phân tích hiện tại từ cache (nếu có)
     * 
     * @param userId ID người dùng
     * @return AdaptiveDifficultyDto hoặc null nếu chưa cache
     */
    AdaptiveDifficultyDto getCachedAnalysis(Long userId);
    
    /**
     * Vô hiệu hóa cache phân tích
     * 
     * @param userId ID người dùng
     */
    void invalidateCache(Long userId);
    
    /**
     * Lấy các chủ đề nên làm dễ hơn
     * 
     * @param userId ID người dùng
     * @return Danh sách chủ đề cần làm dễ hơn
     */
    java.util.List<String> getTopicsToEase(Long userId);
    
    /**
     * Lấy các chủ đề nên làm khó hơn
     * 
     * @param userId ID người dùng
     * @return Danh sách chủ đề cần làm khó hơn
     */
    java.util.List<String> getTopicsToChallenge(Long userId);
    
    /**
     * Lấy độ khó được đề xuất cho một chủ đề cụ thể
     * 
     * @param userId ID người dùng
     * @param topic Chủ đề
     * @return Độ khó được đề xuất (1-10)
     */
    Double getRecommendedDifficultyForTopic(Long userId, String topic);
    
    /**
     * Cập nhật độ khó cho chủ đề dựa trên gợi ý
     * 
     * @param userId ID người dùng
     * @param topic Chủ đề
     * @param newDifficulty Độ khó mới
     */
    void updateTopicDifficulty(Long userId, String topic, Double newDifficulty);
}
