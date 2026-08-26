package com.vocabularyai.ai.controller;

import com.vocabularyai.ai.dto.request.GrammarCheckRequest;
import com.vocabularyai.ai.dto.response.GrammarCheckResponse;
import com.vocabularyai.ai.service.grammar.GrammarCheckingService;
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
 * GrammarController
 * 
 * REST API endpoints cho kiểm tra ngữ pháp:
 * - Kiểm tra grammar một text
 * - Kiểm tra grammar batch (nhiều texts)
 * - Phát hiện lỗi: grammar, spelling, style, punctuation
 * - Cung cấp suggestions + corrections
 * 
 * Base path: /api/v1/ai/grammar
 * Authentication: Required (JWT)
 * 
 * Endpoints:
 * - POST /check - Kiểm tra một text
 * - POST /check-batch - Kiểm tra multiple texts
 * - DELETE /cache/{textHash} - Admin: invalidate cache
 * - DELETE /cache - Admin: invalidate all caches
 */
@RestController
@RequestMapping("/api/v1/ai/grammar")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class GrammarController {
    
    private final GrammarCheckingService grammarService;
    private final UserService userService;
    
    // ==================== Single Text Grammar Check ====================
    
    /**
     * POST /api/v1/ai/grammar/check
     * 
     * Kiểm tra ngữ pháp một text
     * 
     * Phát hiện lỗi:
     * - GRAMMAR: Subject-verb agreement, tense mismatch, etc.
     * - SPELLING: Typos, misspelled words
     * - STYLE: Wordiness, awkward phrasing, etc.
     * - PUNCTUATION: Missing/extra commas, periods, etc.
     * 
     * Request:
     * {
     *   "text": "He go to school yesterday",
     *   "language": "English" (default),
     *   "includeSuggestions": true (default)
     * }
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "originalText": "He go to school yesterday",
     *     "correctedText": "He went to school yesterday",
     *     "errors": [
     *       {
     *         "startOffset": 3,
     *         "endOffset": 5,
     *         "errorType": "GRAMMAR",
     *         "suggestion": "went",
     *         "explanation": "Should use past tense 'went' instead of 'go'"
     *       }
     *     ],
     *     "suggestions": [
     *       {
     *         "errorType": "GRAMMAR",
     *         "suggestion": "Use correct verb tense",
     *         "explanation": "Past tense is required for time reference 'yesterday'"
     *       }
     *     ]
     *   },
     *   "message": "Grammar check completed"
     * }
     * 
     * Features:
     *   ✓ Detailed error positions (startOffset, endOffset)
     *   ✓ Error type classification
     *   ✓ Specific suggestions for each error
     *   ✓ Corrected text
     *   ✓ Summary suggestions
     *   ✓ Cache 1 giờ
     * 
     * Error responses:
     * - 400: Text is blank or too long (> 10000 chars)
     * - 429: Rate limit exceeded
     * - 503: Gemini API unavailable
     */
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<GrammarCheckResponse>> checkGrammar(
            @Valid @RequestBody GrammarCheckRequest request) {
        log.info("Checking grammar: text length={}, language={}", 
            request.getText().length(), request.getLanguage());
        
        User user = userService.getCurrentUser();
        GrammarCheckResponse response = grammarService.checkGrammar(
            request.getText(),
            request.getLanguage(),
            request.getIncludeSuggestions(),
            user.getId()
        );
        
        return ResponseEntity.ok(ApiResponse.success(
            "Grammar check completed",
            response
        ));
    }
    
    // ==================== Batch Text Grammar Check ====================
    
    /**
     * POST /api/v1/ai/grammar/check-batch
     * 
     * Kiểm tra ngữ pháp nhiều texts cùng lúc (batch)
     * 
     * Features:
     * - Smart caching: nếu text nào cached, không gọi API
     * - Batch API call: gọi API 1 lần cho tất cả non-cached texts
     * - Efficient: max 10 texts (để tránh context quá dài)
     * 
     * Request:
     * {
     *   "texts": [
     *     "He go to school",
     *     "She dont like apples",
     *     "I am learning english"
     *   ]
     * }
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": [
     *     { GrammarCheckResponse },
     *     { GrammarCheckResponse },
     *     { GrammarCheckResponse }
     *   ],
     *   "message": "Grammar checks completed"
     * }
     * 
     * Performance Notes:
     * - 10 texts, 7 cached:
     *   └─ 1 batch API call for 3 non-cached texts
     *   └─ Merge with 7 cached results
     *   └─ Total time: ~1-2 seconds
     * 
     * - 10 texts, all new:
     *   └─ 1 batch API call
     *   └─ Parse + cache all
     *   └─ Total time: ~2-3 seconds
     * 
     * Error responses:
     * - 400: Empty list, > 10 texts, or text > 10000 chars
     * - 429: Rate limit exceeded
     * - 503: Gemini API unavailable
     */
    @PostMapping("/check-batch")
    public ResponseEntity<ApiResponse<List<GrammarCheckResponse>>> checkGrammarBatch(
            @Valid @RequestBody CheckGrammarBatchRequest request) {
        log.info("Checking grammar batch: {} texts", request.getTexts().size());
        
        // Validate batch size
        if (request.getTexts() == null || request.getTexts().isEmpty()) {
            throw new IllegalArgumentException("Texts list cannot be empty");
        }
        
        if (request.getTexts().size() > 10) {
            throw new IllegalArgumentException("Cannot check more than 10 texts at once");
        }
        
        User user = userService.getCurrentUser();
        List<GrammarCheckResponse> response = grammarService.checkGrammarBatch(
            request.getTexts(),
            user.getId()
        );
        
        return ResponseEntity.ok(ApiResponse.success(
            "Grammar checks completed",
            response
        ));
    }
    
    // ==================== Cache Management (Admin) ====================
    
    /**
     * DELETE /api/v1/ai/grammar/cache/{textHash}
     * 
     * Invalidate grammar check cache cho một text (Admin only)
     * 
     * Note: textHash = SHA-256 của text, first 32 chars
     * 
     * Response: 204 No Content
     * 
     * Usage: Testing, text updates
     */
    @DeleteMapping("/cache/{textHash}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> invalidateGrammarCache(
            @PathVariable String textHash) {
        log.info("Invalidating grammar cache for text hash: {}", textHash);
        
        grammarService.invalidateGrammarCache(textHash);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DELETE /api/v1/ai/grammar/cache
     * 
     * Invalidate tất cả grammar check caches (Admin only)
     * 
     * Response: 204 No Content
     * 
     * Usage: Bulk updates, system maintenance
     */
    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> invalidateAllGrammarCache() {
        log.warn("Invalidating ALL grammar caches");
        
        grammarService.invalidateAllGrammarCache();
        
        return ResponseEntity.noContent().build();
    }
    
    // ==================== Helper DTOs ====================
    
    /**
     * Request DTO for batch grammar check
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CheckGrammarBatchRequest {
        @jakarta.validation.constraints.NotEmpty(message = "Texts list cannot be empty")
        @jakarta.validation.constraints.Size(max = 10, message = "Can check maximum 10 texts at a time")
        private List<String> texts;
    }
}
