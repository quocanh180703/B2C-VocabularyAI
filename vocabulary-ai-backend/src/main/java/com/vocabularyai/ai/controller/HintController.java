package com.vocabularyai.ai.controller;

import com.vocabularyai.ai.dto.request.HintRequest;
import com.vocabularyai.ai.dto.response.HintResponse;
import com.vocabularyai.ai.service.learning.LearningHintService;
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
 * HintController
 * 
 * REST API endpoints cho gợi ý học tập:
 * - Tạo gợi ý cho một learning progress
 * - Tạo hints batch (nhiều progresses)
 * - Progressive difficulty levels (1-3)
 * - Cache hints (30 phút)
 * 
 * Difficulty Levels:
 * - Level 1 (attempt 0-1): Minimal hint (chỉ definition)
 * - Level 2 (attempt 2): Moderate hint (definition + 1 example)
 * - Level 3 (attempt 3+): Detailed hint (full explanation + strategies)
 * 
 * Base path: /api/v1/ai/hints
 * Authentication: Required (JWT)
 * 
 * Endpoints:
 * - POST /generate - Tạo gợi ý cho một progress
 * - POST /generate-batch - Tạo hints cho multiple progresses
 * - DELETE /cache/{progressId}/{attemptCount} - Admin: invalidate cache
 * - DELETE /cache - Admin: invalidate all caches
 */
@RestController
@RequestMapping("/api/v1/ai/hints")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class HintController {
    
    private final LearningHintService hintService;
    private final UserService userService;
    
    // ==================== Single Progress Hint ====================
    
    /**
     * POST /api/v1/ai/hints/generate
     * 
     * Tạo gợi ý cho một learning progress
     * 
     * Features:
     * - Progressive difficulty dựa trên attemptCount
     * - Caching 30 phút
     * - Context-aware hints từ learning history
     * 
     * Difficulty Levels:
     * - Level 1 (attempt 0-1):
     *     "Think about the definition"
     *     → Minimal hint, encourage thinking
     *
     * - Level 2 (attempt 2):
     *     "Definition: [def]"
     *     "Example: [1 example]"
     *     → Moderate help, 1 example
     *
     * - Level 3 (attempt 3+):
     *     "Definition: [def]"
     *     "Examples: [multiple]"
     *     "Learning strategies:"
     *     "- Mnemonic devices"
     *     "- Etymology connection"
     *     "- Practice repetition"
     *     → Full explanation + multiple strategies
     * 
     * Request:
     * {
     *   "learningProgressId": 123,
     *   "attemptCount": 2
     * }
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "learningProgressId": 123,
     *     "hint": "Definition: ...\nExample: ...",
     *     "hintLevel": 2,
     *     "strategies": [
     *       "Use flashcards for review",
     *       "Practice in context sentences"
     *     ],
     *     "explanation": "This word appears frequently in...",
     *     "responseTimeMs": 1500,
     *     "tokensUsed": 120
     *   },
     *   "message": "Hint generated successfully"
     * }
     * 
     * Features:
     *   ✓ Progressive difficulty (Level 1-3)
     *   ✓ Strategies list for learning
     *   ✓ Detailed explanation
     *   ✓ Cache 30 phút
     *   ✓ Fast response (< 100ms if cached)
     * 
     * Error responses:
     * - 400: Invalid progress ID or attempt count
     * - 404: Learning progress not found
     * - 429: Rate limit exceeded
     * - 503: Gemini API unavailable
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<HintResponse>> generateHint(
            @Valid @RequestBody HintRequest request) {
        log.info("Generating hint for progress: {}, attempt: {}", 
            request.getLearningProgressId(), request.getAttemptCount());
        
        User user = userService.getCurrentUser();
        HintResponse response = hintService.generateHint(
            request.getLearningProgressId(),
            request.getAttemptCount(),
            user.getId()
        );
        
        return ResponseEntity.ok(ApiResponse.success(
            "Hint generated successfully",
            response
        ));
    }
    
    // ==================== Batch Hints Generation ====================
    
    /**
     * POST /api/v1/ai/hints/generate-batch
     * 
     * Tạo hints cho nhiều learning progresses cùng lúc (batch)
     * 
     * Features:
     * - Smart caching: nếu hint nào cached, không gọi AI
     * - Batch API call: gọi API 1 lần cho tất cả non-cached hints
     * - Efficient: max 50 hints
     * 
     * Request:
     * {
     *   "progressIds": [123, 124, 125],
     *   "attemptCounts": [1, 2, 3]
     * }
     * 
     * Note: attemptCounts list phải match size của progressIds list
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": [
     *     { HintResponse (level 1) },
     *     { HintResponse (level 2) },
     *     { HintResponse (level 3) }
     *   ],
     *   "message": "Hints generated successfully"
     * }
     * 
     * Performance Notes:
     * - 50 progresses, 30 cached (different attempt counts):
     *   └─ 1 batch API call for 20 new hints
     *   └─ Merge with 30 cached results
     *   └─ Total time: ~2-3 seconds
     * 
     * - 50 progresses, all new:
     *   └─ 1 batch API call
     *   └─ Parse + cache all
     *   └─ Total time: ~3-4 seconds
     * 
     * Error responses:
     * - 400: Empty list, > 50 hints, or mismatched sizes
     * - 404: Some progresses not found
     * - 429: Rate limit exceeded
     * - 503: Gemini API unavailable
     */
    @PostMapping("/generate-batch")
    public ResponseEntity<ApiResponse<List<HintResponse>>> generateHintsBatch(
            @Valid @RequestBody GenerateHintsBatchRequest request) {
        log.info("Generating batch of {} hints", request.getProgressIds().size());
        
        // Validate batch request
        if (request.getProgressIds() == null || request.getProgressIds().isEmpty()) {
            throw new IllegalArgumentException("Progress IDs list cannot be empty");
        }
        
        if (request.getAttemptCounts() == null || 
            request.getProgressIds().size() != request.getAttemptCounts().size()) {
            throw new IllegalArgumentException("Attempt counts list must match progress IDs size");
        }
        
        if (request.getProgressIds().size() > 50) {
            throw new IllegalArgumentException("Cannot generate hints for more than 50 progresses");
        }
        
        User user = userService.getCurrentUser();
        List<HintResponse> response = hintService.generateHintsBatch(
            request.getProgressIds(),
            request.getAttemptCounts(),
            user.getId()
        );
        
        return ResponseEntity.ok(ApiResponse.success(
            "Hints generated successfully",
            response
        ));
    }
    
    // ==================== Cache Management (Admin) ====================
    
    /**
     * DELETE /api/v1/ai/hints/cache/{progressId}/{attemptCount}
     * 
     * Invalidate hint cache cho một learning progress + attempt count (Admin only)
     * 
     * Note: Cache key = "ai:hint:progress:{progressId}:attempt:{attemptCount}"
     *       Each hint cached separately based on attempt count
     * 
     * Response: 204 No Content
     * 
     * Usage: Testing, learning history updates
     */
    @DeleteMapping("/cache/{progressId}/{attemptCount}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> invalidateHintCache(
            @PathVariable Long progressId,
            @PathVariable Integer attemptCount) {
        log.info("Invalidating hint cache for progress: {}, attempt: {}", progressId, attemptCount);
        
        hintService.invalidateHintCache(progressId, attemptCount);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DELETE /api/v1/ai/hints/cache
     * 
     * Invalidate tất cả hint caches (Admin only)
     * 
     * Response: 204 No Content
     * 
     * Usage: Bulk updates, system maintenance
     */
    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> invalidateAllHintCache() {
        log.warn("Invalidating ALL hint caches");
        
        hintService.invalidateAllHintCache();
        
        return ResponseEntity.noContent().build();
    }
    
    // ==================== Helper DTOs ====================
    
    /**
     * Request DTO for batch hint generation
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GenerateHintsBatchRequest {
        @jakarta.validation.constraints.NotEmpty(message = "Progress IDs list cannot be empty")
        @jakarta.validation.constraints.Size(max = 50, message = "Can generate hints for maximum 50 progresses at a time")
        private List<Long> progressIds;
        
        @jakarta.validation.constraints.NotEmpty(message = "Attempt counts list cannot be empty")
        private List<Integer> attemptCounts;
    }
}
