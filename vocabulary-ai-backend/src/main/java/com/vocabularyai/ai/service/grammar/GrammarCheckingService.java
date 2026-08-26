package com.vocabularyai.ai.service.grammar;

import com.vocabularyai.ai.dto.response.GrammarCheckResponse;

import java.util.List;

/**
 * GrammarCheckingService Interface
 * 
 * Quản lý kiểm tra ngữ pháp:
 * - Kiểm tra grammar, spelling, style, punctuation
 * - Batch checking nhiều texts
 * - Cache results (1 giờ)
 * - Generate suggestions + explanations
 * 
 * Tích hợp với:
 * - GeminiAIClientService (gọi AI để kiểm tra)
 * - PromptEngineeringService (xây dựng grammar prompt)
 * - CacheService (cache check results)
 */
public interface GrammarCheckingService {
    
    /**
     * Kiểm tra ngữ pháp một text
     * 
     * Flow:
     * 1. Kiểm tra cache (key: textHash)
     * 2. Nếu có cache → trả về (cache hit)
     * 3. Nếu không → xây dựng grammar prompt
     * 4. Gọi Gemini API để kiểm tra
     * 5. Parse response thành GrammarCheckResponse
     *    - originalText: text gốc
     *    - correctedText: text đã fix
     *    - errors: list lỗi (startOffset, endOffset, type, suggestion)
     *    - suggestions: tóm tắt suggestions
     * 6. Cache kết quả (1 giờ TTL)
     * 7. Trả về response
     * 
     * @param text Text cần kiểm tra
     * @param language Ngôn ngữ (default: English)
     * @param includeSuggestions Có bao gồm suggestions không
     * @param userId ID người dùng (cho rate limiting)
     * @return GrammarCheckResponse chứa errors, suggestions, corrected text
     * @throws IllegalArgumentException nếu text rỗng
     * @throws com.vocabularyai.common.exception.RateLimitExceededException nếu vượt giới hạn
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu Gemini API lỗi
     */
    GrammarCheckResponse checkGrammar(String text, String language, Boolean includeSuggestions, Long userId);
    
    /**
     * Kiểm tra ngữ pháp batch (nhiều texts)
     * 
     * Flow:
     * 1. Validate texts (max 10 texts)
     * 2. Kiểm tra cache cho từng text
     * 3. Nhóm texts: cached vs non-cached
     * 4. Gọi Gemini batch API cho non-cached texts
     * 5. Cache tất cả results
     * 6. Merge cached + new results
     * 7. Trả về List<GrammarCheckResponse>
     * 
     * @param texts Danh sách texts cần kiểm tra (max 10)
     * @param userId ID người dùng (cho rate limiting)
     * @return List<GrammarCheckResponse>
     * @throws IllegalArgumentException nếu texts list rỗng hoặc > 10
     * @throws com.vocabularyai.common.exception.RateLimitExceededException nếu vượt giới hạn
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu Gemini API lỗi
     */
    List<GrammarCheckResponse> checkGrammarBatch(List<String> texts, Long userId);
    
    /**
     * Invalidate grammar check cache cho một text
     * 
     * Dùng khi:
     * - Test hoặc demo
     * - Cần force refresh
     * 
     * @param textHash Hash của text (SHA-256)
     */
    void invalidateGrammarCache(String textHash);
    
    /**
     * Invalidate tất cả grammar check caches
     * 
     * Dùng khi bulk update hoặc test
     */
    void invalidateAllGrammarCache();
    
    /**
     * Kiểm tra grammar check đã được cache chưa
     * 
     * @param textHash Hash của text
     * @return true nếu cached
     */
    boolean isGrammarCheckCached(String textHash);
    
    /**
     * Lấy grammar check từ cache (không gọi AI)
     * 
     * @param textHash Hash của text
     * @return GrammarCheckResponse nếu cached, null nếu không
     */
    GrammarCheckResponse getCachedGrammarCheck(String textHash);
}
