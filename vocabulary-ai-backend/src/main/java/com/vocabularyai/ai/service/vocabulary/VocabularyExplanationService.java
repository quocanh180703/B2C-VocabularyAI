package com.vocabularyai.ai.service.vocabulary;

import com.vocabularyai.ai.dto.response.ExplanationResponse;

import java.util.List;

/**
 * VocabularyExplanationService Interface
 * 
 * Quản lý giải thích từ vựng:
 * - Giải thích một từ duy nhất
 * - Giải thích batch nhiều từ
 * - Cache explanations (24 giờ)
 * - Support context-aware explanations
 * 
 * Tích hợp với:
 * - GeminiAIClientService (gọi AI để tạo explanation)
 * - PromptEngineeringService (xây dựng vocabulary prompt)
 * - CacheService (cache explanations)
 * - WordRepository (load word data)
 */
public interface VocabularyExplanationService {
    
    /**
     * Giải thích một từ duy nhất
     * 
     * Flow:
     * 1. Kiểm tra cache trước (key: wordId hoặc wordId + context)
     * 2. Nếu có cache → trả về (cache hit)
     * 3. Nếu không → load word từ DB
     * 4. Xây dựng vocabulary prompt với context
     * 5. Gọi Gemini API để tạo explanation
     * 6. Parse response thành ExplanationResponse
     * 7. Cache kết quả (24 giờ)
     * 8. Trả về response
     * 
     * @param wordId ID của từ cần giải thích
     * @param context Optional context để tạo explanation phù hợp
     *                Ví dụ: "business English", "casual conversation", etc.
     * @param userId ID người dùng (cho rate limiting)
     * @return ExplanationResponse chứa explanation, examples, etymology, related words
     * @throws IllegalArgumentException nếu word không tồn tại
     * @throws com.vocabularyai.common.exception.RateLimitExceededException nếu vượt giới hạn
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu Gemini API lỗi
     */
    ExplanationResponse explainWord(Long wordId, String context, Long userId);
    
    /**
     * Giải thích nhiều từ vựng cùng lúc (batch)
     * 
     * Flow:
     * 1. Validate word IDs (max 100)
     * 2. Load tất cả words từ DB
     * 3. Kiểm tra cache cho từng word
     * 4. Nhóm words: cached vs non-cached
     * 5. Gọi Gemini batch API cho non-cached words
     * 6. Cache tất cả results
     * 7. Merge cached + new results
     * 8. Trả về danh sách explanations
     * 
     * @param wordIds Danh sách ID từ (max 100)
     * @param userId ID người dùng (cho rate limiting)
     * @return Danh sách ExplanationResponse
     * @throws IllegalArgumentException nếu word IDs list rỗng hoặc > 100
     * @throws IllegalArgumentException nếu một hoặc nhiều words không tồn tại
     * @throws com.vocabularyai.common.exception.RateLimitExceededException nếu vượt giới hạn
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu Gemini API lỗi
     */
    List<ExplanationResponse> explainWords(List<Long> wordIds, Long userId);
    
    /**
     * Invalidate explanation cache cho một word
     * 
     * Dùng khi:
     * - Word definition thay đổi
     * - Cần force refresh explanation
     * 
     * @param wordId ID của từ
     */
    void invalidateExplanationCache(Long wordId);
    
    /**
     * Invalidate tất cả explanations
     * 
     * Dùng khi cập nhật toàn bộ definitions hoặc test
     */
    void invalidateAllExplanationCache();
    
    /**
     * Kiểm tra explanation đã được cache chưa
     * 
     * @param wordId ID của từ
     * @param context Optional context
     * @return true nếu explanation đã cache
     */
    boolean isExplanationCached(Long wordId, String context);
    
    /**
     * Lấy explanation từ cache (không gọi AI)
     * 
     * Dùng cho monitoring hoặc testing
     * 
     * @param wordId ID của từ
     * @param context Optional context
     * @return ExplanationResponse nếu cached, null nếu không
     */
    ExplanationResponse getCachedExplanation(Long wordId, String context);
}
