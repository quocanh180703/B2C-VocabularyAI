package com.vocabularyai.ai.service.cache;

import com.vocabularyai.ai.dto.response.ExplanationResponse;
import com.vocabularyai.ai.entity.AiMessage;
import java.util.List;
import java.util.Optional;

/**
 * Interface for caching AI responses and context
 * 
 * Provides abstraction over Redis for caching:
 * - AI conversation context
 * - Vocabulary explanations
 * - Rate limit tracking
 */
public interface CacheService {
    
    /**
     * Set a key-value pair in cache with custom TTL
     * 
     * @param key Cache key
     * @param value Value to cache (will be serialized to JSON)
     * @param ttlSeconds Time to live in seconds
     */
    void set(String key, Object value, long ttlSeconds);
    
    /**
     * Set a key-value pair in cache with default TTL
     * 
     * @param key Cache key
     * @param value Value to cache
     */
    void set(String key, Object value);
    
    /**
     * Get value from cache by key
     * 
     * @param key Cache key
     * @param type Type to deserialize to
     * @return Optional containing value if found
     */
    <T> Optional<T> get(String key, Class<T> type);
    
    /**
     * Delete a specific key from cache
     * 
     * @param key Cache key to delete
     */
    void delete(String key);
    
    /**
     * Delete multiple keys matching a pattern
     * 
     * @param pattern Redis pattern (e.g., "ai:explanation:*")
     */
    void deletePattern(String pattern);
    
    // ==================== Conversation Context Operations ====================
    
    /**
     * Cache conversation context (message history) with custom TTL
     * 
     * @param conversationId ID of the conversation
     * @param messages Message history to cache
     * @param ttlSeconds Time to live in seconds (default: 30 minutes)
     */
    void cacheConversationContext(Long conversationId, List<AiMessage> messages, long ttlSeconds);
    
    /**
     * Get cached conversation context
     * 
     * @param conversationId ID of the conversation
     * @return Optional containing message history
     */
    Optional<List<AiMessage>> getConversationContext(Long conversationId);
    
    /**
     * Invalidate conversation context cache
     * 
     * @param conversationId ID of the conversation
     */
    void invalidateConversationContext(Long conversationId);
    
    // ==================== Vocabulary Explanation Operations ====================
    
    /**
     * Cache vocabulary explanation with 24-hour TTL
     * 
     * @param wordId ID of the word
     * @param explanation Explanation response to cache
     */
    void cacheExplanation(Long wordId, ExplanationResponse explanation);
    
    /**
     * Cache explanation with context-aware key (for context-specific explanations)
     * 
     * @param wordId ID of the word
     * @param context Context string (will be hashed)
     * @param explanation Explanation to cache
     */
    void cacheExplanationWithContext(Long wordId, String context, ExplanationResponse explanation);
    
    /**
     * Get cached vocabulary explanation
     * 
     * @param wordId ID of the word
     * @return Optional containing explanation
     */
    Optional<ExplanationResponse> getExplanation(Long wordId);
    
    /**
     * Get cached explanation with context-aware key
     * 
     * @param wordId ID of the word
     * @param context Context string
     * @return Optional containing explanation
     */
    Optional<ExplanationResponse> getExplanationWithContext(Long wordId, String context);
    
    /**
     * Delete cached explanation
     * 
     * @param wordId ID of the word
     */
    void deleteExplanation(Long wordId);
    
    // ==================== Rate Limit Operations ====================
    
    /**
     * Increment tokens used counter for a user
     * 
     * Uses atomic Redis INCR command for thread safety
     * 
     * @param userId ID of the user
     * @param tokens Number of tokens to add
     */
    void incrementRateLimitTokens(Long userId, long tokens);
    
    /**
     * Increment request count for a user
     * 
     * Uses atomic Redis INCR command for thread safety
     * 
     * @param userId ID of the user
     */
    void incrementRateLimitRequests(Long userId);
    
    /**
     * Get current tokens used by user
     * 
     * @param userId ID of the user
     * @return Tokens used counter (0 if not found)
     */
    long getRateLimitTokensUsed(Long userId);
    
    /**
     * Get current request count for user
     * 
     * @param userId ID of the user
     * @return Request count (0 if not found)
     */
    long getRateLimitRequestCount(Long userId);
    
    /**
     * Reset rate limit counters for a user
     * 
     * @param userId ID of the user
     */
    void resetRateLimitCounters(Long userId);
    
    /**
     * Set rate limit counters with TTL
     * 
     * @param userId ID of the user
     * @param tokens Tokens to set
     * @param requests Requests to set
     * @param ttlSeconds Time to live in seconds
     */
    void setRateLimitCounters(Long userId, long tokens, long requests, long ttlSeconds);
}
