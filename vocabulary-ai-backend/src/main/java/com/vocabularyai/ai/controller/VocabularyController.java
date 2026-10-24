package com.vocabularyai.ai.controller;

import com.vocabularyai.ai.dto.request.ExplainWordRequest;
import com.vocabularyai.ai.dto.request.ExplainWordsRequest;
import com.vocabularyai.ai.dto.response.ExplanationResponse;
import com.vocabularyai.ai.service.vocabulary.VocabularyExplanationService;
import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VocabularyController
 * 
 * REST API endpoints cho giải thích từ vựng:
 * - Giải thích một từ duy nhất
 * - Giải thích batch (nhiều từ)
 * 
 * Base path: /api/v1/ai/vocabulary
 * Authentication: Required (JWT)
 * 
 * Endpoints:
 * - POST /explain - Giải thích một từ
 * - POST /explain-batch - Giải thích multiple từ
 */
@RestController
@RequestMapping("/api/v1/ai/vocabulary")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class VocabularyController {
    
    private final VocabularyExplanationService vocabularyService;
    private final UserService userService;
    private final com.vocabularyai.payment.service.FeatureUsageService featureUsageService;
    
    // ==================== Single Word Explanation ====================
    
    /**
     * POST /api/v1/ai/vocabulary/explain
     * 
     * Giải thích một từ duy nhất
     * 
     * Request:
     * {
     *   "wordId": 123,
     *   "context": "optional context (business, casual, etc.)"
     * }
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "wordId": 123,
     *     "word": "example",
     *     "explanation": "Detailed explanation of the word...",
     *     "examples": [
     *       "This is an example of usage.",
     *       "Another example here."
     *     ],
     *     "etymology": "From Latin...",
     *     "relatedWords": [
     *       "sample",
     *       "instance",
     *       "pattern"
     *     ],
     *     "partOfSpeech": "noun"
     *   },
     *   "message": "Word explanation retrieved successfully"
     * }
     * 
     * Error responses:
     * - 400: Invalid word ID or missing context
     * - 404: Word not found
     * - 429: Rate limit exceeded
     * - 503: Gemini API unavailable
     */
    @PostMapping("/explain")
    public ResponseEntity<ApiResponse<ExplanationResponse>> explainWord(
            @Valid @RequestBody ExplainWordRequest request) {
        log.info("Explaining word: {}, context: {}", request.getWordId(), request.getContext());
        
        User user = userService.getCurrentUser();
        featureUsageService.checkAndConsumeQuota(user.getId(), com.vocabularyai.payment.entity.FeatureType.WORD_EXPLAIN);
        try {
            ExplanationResponse response = vocabularyService.explainWord(
                request.getWordId(),
                request.getContext(),
                user.getId()
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Word explanation retrieved successfully",
                response
            ));
        } catch (Exception e) {
            featureUsageService.rollbackQuota(user.getId(), com.vocabularyai.payment.entity.FeatureType.WORD_EXPLAIN);
            throw e;
        }
    }
    
    // ==================== Batch Words Explanation ====================
    
    /**
     * POST /api/v1/ai/vocabulary/explain-batch
     * 
     * Giải thích nhiều từ cùng lúc (batch)
     * 
     * Features:
     * - Smart caching: nếu từ nào cached, không gọi API
     * - Batch API call: gọi API 1 lần cho tất cả non-cached từ
     * - Efficient: max 100 từ
     * 
     * Request:
     * {
     *   "wordIds": [123, 124, 125]
     * }
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": [
     *     { ExplanationResponse },
     *     { ExplanationResponse },
     *     { ExplanationResponse }
     *   ],
     *   "message": "Words explained successfully"
     * }
     * 
     * Performance Notes:
     * - 100 words, 80 cached:
     *   └─ 1 API call for 20 non-cached words
     *   └─ Merge with 80 cached results
     *   └─ Total time: ~2-3 seconds
     * 
     * Error responses:
     * - 400: Empty list, > 100 words, or invalid word IDs
     * - 404: Some words not found
     * - 429: Rate limit exceeded
     * - 503: Gemini API unavailable
     */
    @PostMapping("/explain-batch")
    public ResponseEntity<ApiResponse<List<ExplanationResponse>>> explainWordsBatch(
            @Valid @RequestBody ExplainWordsRequest request) {
        log.info("Explaining batch of {} words", request.getWordIds().size());
        
        User user = userService.getCurrentUser();
        featureUsageService.checkAndConsumeQuota(user.getId(), com.vocabularyai.payment.entity.FeatureType.WORD_EXPLAIN);
        try {
            List<ExplanationResponse> response = vocabularyService.explainWords(
                request.getWordIds(),
                user.getId()
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Words explained successfully",
                response
            ));
        } catch (Exception e) {
            featureUsageService.rollbackQuota(user.getId(), com.vocabularyai.payment.entity.FeatureType.WORD_EXPLAIN);
            throw e;
        }
    }
    
    // ==================== Cache Management (Admin) ====================
    
    /**
     * DELETE /api/v1/ai/vocabulary/cache/{wordId}
     * 
     * Invalidate explanation cache cho một từ
     * 
     * Note: Chỉ admin hoặc developer dùng (test/debugging)
     * 
     * Response: 204 No Content
     */
    @DeleteMapping("/cache/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> invalidateWordCache(
            @PathVariable Long wordId) {
        log.info("Invalidating vocabulary cache for word: {}", wordId);
        
        vocabularyService.invalidateExplanationCache(wordId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DELETE /api/v1/ai/vocabulary/cache
     * 
     * Invalidate tất cả explanation caches
     * 
     * Note: Chỉ admin dùng (bulk update, testing)
     * 
     * Response: 204 No Content
     */
    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> invalidateAllCache() {
        log.warn("Invalidating ALL vocabulary caches");
        
        vocabularyService.invalidateAllExplanationCache();
        
        return ResponseEntity.noContent().build();
    }
}
