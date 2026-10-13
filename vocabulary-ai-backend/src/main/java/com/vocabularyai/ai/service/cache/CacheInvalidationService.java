package com.vocabularyai.ai.service.cache;

import com.vocabularyai.personalization.service.AdaptiveDifficultyServiceImpl;
import com.vocabularyai.personalization.service.DailyPracticeServiceImpl;
import com.vocabularyai.personalization.service.LearningPlanServiceImpl;
import com.vocabularyai.personalization.service.ProgressAnalysisServiceImpl;
import com.vocabularyai.personalization.service.RecommendationServiceImpl;
import com.vocabularyai.personalization.service.WeaknessStrengthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Cache Invalidation Service - TASK 11
 * 
 * Centralized cache invalidation for all user-specific caches
 * Called when user status changes (ACTIVE → INACTIVE or ACTIVE → BANNED)
 * 
 * Invalidates all 8 user-specific cache types:
 * 1. Adaptive Difficulty (ai:adaptive-difficulty:{userId})
 * 2. Recommendations (ai:recommendations:{userId})
 * 3. Weakness/Strength (ai:weakness-strength:{userId})
 * 4. Progress Analysis (ai:progress-analysis:{userId})
 * 5. Learning Plan (ai:learning-plan:{userId})
 * 6. Daily Practice (ai:daily-practice:{userId}:*)
 * 7. Rate Limit Tokens (ai:ratelimit:tokens:{userId})
 * 8. Rate Limit Requests (ai:ratelimit:requests:{userId})
 * 
 * Does NOT invalidate:
 * - Grammar checks (text-based, shared)
 * - Vocabulary explanations (word-based, shared)
 * - Conversation context (not user-specific in key)
 * - Learning hints (progress-based, shared)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheInvalidationService {
    
    private final AdaptiveDifficultyServiceImpl adaptiveDifficultyService;
    private final RecommendationServiceImpl recommendationService;
    private final WeaknessStrengthServiceImpl weaknessStrengthService;
    private final ProgressAnalysisServiceImpl progressAnalysisService;
    private final LearningPlanServiceImpl learningPlanService;
    private final DailyPracticeServiceImpl dailyPracticeService;
    private final CacheService cacheService;
    
    /**
     * TASK 11: Invalidate all user-specific caches when user status changes
     * 
     * Called when:
     * - ACTIVE → INACTIVE (account suspended)
     * - ACTIVE → BANNED (account banned)
     * 
     * Ensures:
     * - No stale data remains in Redis
     * - Future requests will recompute/revalidate
     * - Authorization checks remain in place
     * - Database is source of truth
     * 
     * @param userId User ID whose caches should be invalidated
     */
    public void invalidateAllUserCaches(Long userId) {
        log.info("[TASK 11] Invalidating all user-specific caches for userId: {}", userId);
        
        try {
            // 1. Adaptive Difficulty (30-day TTL)
            log.debug("[TASK 11] Invalidating adaptive-difficulty cache for userId: {}", userId);
            adaptiveDifficultyService.invalidateCache(userId);
            
            // 2. Recommendations (3-hour TTL)
            log.debug("[TASK 11] Invalidating recommendations cache for userId: {}", userId);
            recommendationService.invalidateCache(userId);
            
            // 3. Weakness/Strength Analysis (2-hour TTL)
            log.debug("[TASK 11] Invalidating weakness-strength cache for userId: {}", userId);
            weaknessStrengthService.invalidateCache(userId);
            
            // 4. Progress Analysis (1-day TTL)
            log.debug("[TASK 11] Invalidating progress-analysis cache for userId: {}", userId);
            progressAnalysisService.invalidateCache(userId);
            
            // 5. Learning Plan (7-day TTL)
            log.debug("[TASK 11] Invalidating learning-plan cache for userId: {}", userId);
            learningPlanService.invalidateCache(userId);
            
            // 6. Daily Practice (multiple entries: ai:daily-practice:{userId}:YYYY-MM-DD)
            log.debug("[TASK 11] Invalidating daily-practice cache for userId: {}", userId);
            dailyPracticeService.invalidateCache(userId);
            
            // 7. Rate Limit Tokens (24-hour TTL)
            log.debug("[TASK 11] Invalidating rate-limit-tokens cache for userId: {}", userId);
            cacheService.delete("ai:ratelimit:tokens:" + userId);
            
            // 8. Rate Limit Requests (24-hour TTL)
            log.debug("[TASK 11] Invalidating rate-limit-requests cache for userId: {}", userId);
            cacheService.delete("ai:ratelimit:requests:" + userId);
            
            log.info("[TASK 11] All user-specific caches invalidated successfully for userId: {}", userId);
            
        } catch (Exception e) {
            log.error("[TASK 11] Error invalidating user caches for userId: {}: {}", 
                      userId, e.getMessage(), e);
            
            // IMPORTANT: Don't throw exception - cache is optimization layer, not security-critical
            // Authorization and status checks still apply even if cache invalidation fails
            // Log the error for monitoring but allow operation to continue
        }
    }
    
    /**
     * TASK 11: Invalidate all user-specific caches by pattern deletion
     * 
     * Alternative method using pattern matching
     * Less reliable than individual service calls but useful for cleanup
     * 
     * @param userId User ID whose caches should be invalidated
     */
    public void invalidateAllUserCachesByPattern(Long userId) {
        log.info("[TASK 11] Invalidating all user-specific caches by pattern for userId: {}", userId);
        
        try {
            // Delete all Redis keys matching pattern: "ai:*:{userId}" or similar
            // This is less precise but can catch keys we missed
            String userPatterns[] = {
                "ai:adaptive-difficulty:" + userId,
                "ai:recommendations:" + userId,
                "ai:weakness-strength:" + userId,
                "ai:progress-analysis:" + userId,
                "ai:learning-plan:" + userId,
                "ai:daily-practice:" + userId + ":*",
                "ai:ratelimit:tokens:" + userId,
                "ai:ratelimit:requests:" + userId
            };
            
            for (String pattern : userPatterns) {
                try {
                    cacheService.deletePattern(pattern);
                    log.debug("[TASK 11] Deleted pattern: {}", pattern);
                } catch (Exception e) {
                    log.warn("[TASK 11] Failed to delete pattern {}: {}", pattern, e.getMessage());
                }
            }
            
            log.info("[TASK 11] Pattern-based cache invalidation completed for userId: {}", userId);
            
        } catch (Exception e) {
            log.error("[TASK 11] Error in pattern-based cache invalidation for userId: {}: {}", 
                      userId, e.getMessage(), e);
        }
    }
}
