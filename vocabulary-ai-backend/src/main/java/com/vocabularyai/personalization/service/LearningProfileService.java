package com.vocabularyai.personalization.service;

import com.vocabularyai.personalization.dto.LearningProfileDto;

/**
 * LearningProfileService Interface
 * 
 * Responsible for:
 * - Aggregating user learning data from all modules
 * - Calculating composite metrics
 * - Identifying learning patterns
 * - Detecting strengths and weaknesses
 * 
 * Data sources:
 * - Vocabulary Learning (Phase 4) - LearningProgress, Word
 * - Grammar Learning (Phase 5) - LearningHistory
 * - Quiz Results (Phase 3) - LearningProgress
 * - Speaking Progress (Phase 8) - SpeakingSession, SpeakingFeedback
 * - AI Interactions (Phase 7) - AiConversation history
 * 
 * Tái sử dụng existing services:
 * - LearningService (Phase 3)
 * - SpeakingProgressService (Phase 8)
 * - CacheService (Phase 9)
 * 
 * NO duplicate service creation
 * NO new data persistence
 * Pure aggregation + calculation from existing data
 */
public interface LearningProfileService {
    
    /**
     * Get comprehensive learning profile for user
     * 
     * Aggregates:
     * 1. Vocabulary progress
     *    - Total words learned
     *    - Mastered words
     *    - Accuracy percentage
     *    - Recent vocabulary performance
     * 
     * 2. Grammar progress
     *    - Grammar topics covered
     *    - Grammar accuracy
     *    - Recent grammar performance
     * 
     * 3. Speaking progress
     *    - Speaking sessions completed
     *    - Average speaking score
     *    - Pronunciation score
     *    - Fluency score
     * 
     * 4. Overall statistics
     *    - Learning streaks
     *    - Total learning days
     *    - Recent activities
     *    - Achievements
     * 
     * 5. Trends
     *    - 7-day trend
     *    - 30-day trend
     *    - Direction (improving/stable/declining)
     * 
     * 6. Patterns
     *    - Weak topics
     *    - Strong topics
     *    - Learning frequency
     *    - Average session duration
     * 
     * Caching:
     * - Cache key: ai:profile:{userId}
     * - TTL: 1 hour (configurable)
     * - Invalidation: On activity update, manual flush
     * 
     * @param userId User ID
     * @param useCache Whether to use Redis cache (default: true)
     * @return Comprehensive learning profile
     * @throws IllegalArgumentException if user not found
     * @throws com.vocabularyai.common.exception.ExternalServiceException if data retrieval fails
     */
    LearningProfileDto getLearningProfile(Long userId, boolean useCache);
    
    /**
     * Convenience method with cache enabled by default
     * 
     * @param userId User ID
     * @return Learning profile
     */
    LearningProfileDto getLearningProfile(Long userId);
    
    /**
     * Invalidate cached learning profile
     * 
     * Called when:
     * - User completes vocabulary lesson
     * - User completes grammar lesson
     * - User completes speaking session
     * - User completes quiz
     * - Manual admin invalidation
     * 
     * @param userId User ID
     * @return true if cache was invalidated, false if not in cache
     */
    boolean invalidateLearningProfile(Long userId);
    
    /**
     * Get estimated current English level (A1-C2)
     * 
     * Based on:
     * - Overall learning progress
     * - Speaking performance
     * - Grammar proficiency
     * - Vocabulary breadth
     * 
     * @param userId User ID
     * @return Level string (A1, A2, B1, B2, C1, C2)
     */
    String estimateCurrentLevel(Long userId);
    
    /**
     * Get weakest skill areas
     * 
     * Returns topics/areas where user has <60% accuracy
     * 
     * @param userId User ID
     * @return List of weak topics
     */
    java.util.List<String> getWeakTopics(Long userId);
    
    /**
     * Get strongest skill areas
     * 
     * Returns topics/areas where user has >80% accuracy
     * 
     * @param userId User ID
     * @return List of strong topics
     */
    java.util.List<String> getStrongTopics(Long userId);
    
    /**
     * Get learning frequency (activities per week)
     * 
     * Analyzes recent 30 days of activity
     * 
     * @param userId User ID
     * @return Activities per week
     */
    Double getLearningFrequency(Long userId);
    
    /**
     * Get average learning session duration (minutes)
     * 
     * Analyzes recent sessions from all learning types
     * 
     * @param userId User ID
     * @return Average duration in minutes
     */
    Double getAverageSessionDuration(Long userId);
}
