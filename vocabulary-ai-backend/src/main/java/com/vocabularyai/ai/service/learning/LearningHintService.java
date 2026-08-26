package com.vocabularyai.ai.service.learning;

import com.vocabularyai.ai.dto.response.HintResponse;

import java.util.List;

/**
 * LearningHintService Interface
 * 
 * Quản lý gợi ý học tập:
 * - Generate hints dựa trên attempt count
 * - Progressive difficulty levels
 * - Batch hint generation
 * - Cache hints (30 phút)
 * 
 * Hint Difficulty Levels:
 * - Level 1 (attempt 0-1): Minimal - chỉ definition
 * - Level 2 (attempt 2): Moderate - definition + 1 example
 * - Level 3 (attempt 3+): Detailed - full explanation + strategies
 * 
 * Tích hợp với:
 * - GeminiAIClientService (gọi AI để tạo hints)
 * - PromptEngineeringService (xây dựng hint prompts)
 * - CacheService (cache hints)
 * - LearningProgressRepository (load progress data)
 * - WordRepository (load word data)
 */
public interface LearningHintService {
    
    /**
     * Tạo gợi ý cho một learning progress
     * 
     * Flow:
     * 1. Load LearningProgress từ DB
     * 2. Load Word entity
     * 3. Xác định difficulty level dựa trên attemptCount
     * 4. Kiểm tra cache (key: progressId + attemptCount)
     * 5. Nếu có cache → trả về
     * 6. Xây dựng hint prompt với difficulty level
     * 7. Gọi Gemini API để tạo hint
     * 8. Parse response → HintResponse
     *    - hint: Gợi ý chính
     *    - hintLevel: 1/2/3
     *    - strategies: Danh sách chiến lược học
     *    - explanation: Giải thích chi tiết
     * 9. Cache kết quả (30 phút)
     * 10. Trả về HintResponse
     * 
     * Difficulty Levels:
     * - Level 1 (attemptCount 0-1):
     *     → Minimal hint: "Think about the definition"
     * - Level 2 (attemptCount 2):
     *     → Moderate: Definition + 1 example
     * - Level 3 (attemptCount 3+):
     *     → Detailed: Full explanation + learning strategies
     * 
     * @param progressId ID của LearningProgress
     * @param attemptCount Số lần thử (determines difficulty)
     * @param userId ID người dùng (cho rate limiting)
     * @return HintResponse chứa hint, strategies, explanation
     * @throws IllegalArgumentException nếu progress không tồn tại
     * @throws com.vocabularyai.common.exception.RateLimitExceededException nếu vượt giới hạn
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu Gemini API lỗi
     */
    HintResponse generateHint(Long progressId, Integer attemptCount, Long userId);
    
    /**
     * Tạo hints batch cho multiple learning progresses
     * 
     * Flow:
     * 1. Validate progressIds (max 50)
     * 2. Load tất cả LearningProgress từ DB
     * 3. Kiểm tra cache cho từng progress
     * 4. Nhóm: cached vs non-cached
     * 5. Gọi Gemini batch API cho non-cached
     * 6. Cache tất cả results
     * 7. Merge + sort theo input order
     * 8. Trả về List<HintResponse>
     * 
     * @param progressIds Danh sách progress IDs (max 50)
     * @param attemptCounts List attempt counts tương ứng
     * @param userId ID người dùng
     * @return List<HintResponse>
     * @throws IllegalArgumentException nếu progressIds list rỗng hoặc > 50
     * @throws IllegalArgumentException nếu một hoặc nhiều progresses không tồn tại
     * @throws com.vocabularyai.common.exception.RateLimitExceededException nếu vượt giới hạn
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu Gemini API lỗi
     */
    List<HintResponse> generateHintsBatch(List<Long> progressIds, List<Integer> attemptCounts, Long userId);
    
    /**
     * Invalidate hint cache cho một progress
     * 
     * @param progressId ID của progress
     * @param attemptCount Attempt count (part of cache key)
     */
    void invalidateHintCache(Long progressId, Integer attemptCount);
    
    /**
     * Invalidate tất cả hint caches
     * 
     * Dùng khi testing hoặc bulk updates
     */
    void invalidateAllHintCache();
    
    /**
     * Kiểm tra hint đã được cache chưa
     * 
     * @param progressId ID của progress
     * @param attemptCount Attempt count
     * @return true nếu cached
     */
    boolean isHintCached(Long progressId, Integer attemptCount);
    
    /**
     * Lấy hint từ cache (không gọi AI)
     * 
     * @param progressId ID của progress
     * @param attemptCount Attempt count
     * @return HintResponse nếu cached, null nếu không
     */
    HintResponse getCachedHint(Long progressId, Integer attemptCount);
}
