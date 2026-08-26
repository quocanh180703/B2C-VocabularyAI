package com.vocabularyai.personalization.service;

import com.vocabularyai.personalization.dto.LearningProfileDto;
import com.vocabularyai.personalization.dto.WeaknessStrengthDto;

/**
 * WeaknessStrengthService
 * 
 * AI-powered analysis of user weaknesses and strengths based on learning profile
 * 
 * Responsibilities:
 * - Accept LearningProfileDto from TASK 1
 * - Build structured prompt for Gemini AI
 * - Parse JSON response from AI analysis
 * - Cache analysis results with userId key
 * - Provide analysis endpoints
 * 
 * Dependencies:
 * - GeminiAIClientService (Phase 7) - AI engine
 * - PromptEngineeringService (Phase 7) - Prompt building
 * - CacheService (Phase 9) - Redis caching
 * - RateLimitService (Phase 9) - Rate limiting
 * - LearningProfileService (Phase 10 TASK 1) - Profile data
 * 
 * Redis Keys:
 * - ai:weakness-strength:{userId} [TTL: 2 hours]
 */
public interface WeaknessStrengthService {
    
    /**
     * Analyze learning profile to detect weaknesses and strengths
     * 
     * Process:
     * 1. Validate learning profile data
     * 2. Build analysis prompt with profile data
     * 3. Call Gemini AI for analysis
     * 4. Parse JSON response
     * 5. Cache results with userId key
     * 
     * @param userId User ID (from JWT token)
     * @return WeaknessStrengthDto with analysis results
     * @throws WeaknessStrengthException if analysis fails
     * @throws RateLimitExceededException if rate limit exceeded
     * @throws ResourceNotFoundException if user not found
     */
    WeaknessStrengthDto analyzeWeaknessesAndStrengths(Long userId);
    
    /**
     * Get cached weakness/strength analysis
     * 
     * Returns analysis if cached and not expired
     * Returns null if not cached or expired
     * 
     * @param userId User ID
     * @return Cached analysis or null
     */
    WeaknessStrengthDto getCachedAnalysis(Long userId);
    
    /**
     * Invalidate cached weakness/strength analysis
     * 
     * Called when:
     * - Learning progress changes
     * - User requests fresh analysis
     * - Cache expiration triggered
     * 
     * @param userId User ID
     */
    void invalidateCache(Long userId);
    
    /**
     * Get list of critical weaknesses (severity = CRITICAL)
     * 
     * Performs full analysis if not cached
     * Filters for critical items only
     * 
     * @param userId User ID
     * @return List of critical weaknesses
     */
    java.util.List<WeaknessStrengthDto.WeaknessItem> getCriticalWeaknesses(Long userId);
    
    /**
     * Get list of outstanding strengths (excellence level = OUTSTANDING)
     * 
     * Performs full analysis if not cached
     * Filters for outstanding items only
     * 
     * @param userId User ID
     * @return List of outstanding strengths
     */
    java.util.List<WeaknessStrengthDto.StrengthItem> getOutstandingStrengths(Long userId);
    
    /**
     * Get top priority actions from analysis
     * 
     * Performs full analysis if not cached
     * Returns top N actions (typically 5-10)
     * Sorted by priority
     * 
     * @param userId User ID
     * @param limit Number of actions to return
     * @return List of priority actions
     */
    java.util.List<WeaknessStrengthDto.PriorityAction> getTopPriorityActions(Long userId, int limit);
}
