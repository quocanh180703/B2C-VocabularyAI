package com.vocabularyai.ai.service.gemini;

/**
 * Interface for Gemini AI API client
 * 
 * Responsible for:
 * - Direct communication with Google Gemini API
 * - Request/response handling
 * - Rate limiting and quota tracking
 * - Streaming response support
 * - Retry logic with exponential backoff
 * - Circuit breaker pattern
 * - Token counting
 */
public interface GeminiAIClientService {
    
    /**
     * Generate response from Gemini API for a given prompt
     * 
     * Performs:
     * - Rate limit pre-check
     * - Token counting
     * - API call with retry logic
     * - Response validation
     * - Rate limit tracking
     * 
     * @param prompt The prompt to send to Gemini
     * @param userId ID of the user (for rate limiting)
     * @return Generated response text from Gemini
     * @throws com.vocabularyai.common.exception.RateLimitExceededException if rate limit exceeded
     * @throws com.vocabularyai.common.exception.ExternalServiceException if API fails after retries
     */
    String generateResponse(String prompt, Long userId);
    
    /**
     * Generate response from Gemini API for a given prompt, with option to disable local intelligent fallback
     * (crucial for JSON/classification pipelines where local conversational fallback produces invalid output).
     * 
     * @param prompt The prompt to send to Gemini
     * @param userId ID of the user (nullable for system jobs)
     * @param allowFallback true to fallback to local mock/conversational text, false to throw exception
     * @return Generated response text from Gemini
     */
    String generateResponse(String prompt, Long userId, boolean allowFallback);
    
    /**
     * Generate response with streaming support
     * 
     * Callbacks are invoked as response is streamed from API
     * 
     * @param prompt The prompt to send
     * @param userId ID of the user
     * @param callback Callback to handle streamed chunks
     * @throws com.vocabularyai.common.exception.RateLimitExceededException if rate limit exceeded
     * @throws com.vocabularyai.common.exception.ExternalServiceException if API fails
     */
    void generateResponseStream(String prompt, Long userId, StreamCallback callback);
    
    /**
     * Check if user has exceeded rate limit
     * 
     * Checks:
     * - Daily token limit (1,000,000 tokens)
     * - Daily request limit (1,000 requests)
     * 
     * @param userId ID of the user
     * @return true if rate limit exceeded, false otherwise
     */
    boolean isRateLimitExceeded(Long userId);
    
    /**
     * Get remaining tokens for user today
     * 
     * @param userId ID of the user
     * @return Remaining tokens (0 if limit exceeded)
     */
    long getRemainingTokens(Long userId);
    
    /**
     * Get remaining requests for user today
     * 
     * @param userId ID of the user
     * @return Remaining requests (0 if limit exceeded)
     */
    long getRemainingRequests(Long userId);
    
    /**
     * Count tokens in text without API call
     * 
     * Uses local approximation: ~1 token per 4 characters
     * 
     * @param text Text to count tokens for
     * @return Estimated token count
     */
    int countTokens(String text);
    
    /**
     * Get circuit breaker status
     * 
     * Used for health checks and monitoring
     * 
     * @return true if circuit breaker is OPEN (failing fast), false if CLOSED (normal)
     */
    boolean isCircuitBreakerOpen();
    
    /**
     * Callback interface for streaming responses
     */
    interface StreamCallback {
        
        /**
         * Called when a chunk of response is received
         * 
         * @param chunk Text chunk from response
         */
        void onContent(String chunk);
        
        /**
         * Called when response streaming completes
         * 
         * @param totalTokens Total tokens used
         */
        void onComplete(int totalTokens);
        
        /**
         * Called when error occurs during streaming
         * 
         * @param error Exception that occurred
         */
        void onError(Exception error);
    }
}
