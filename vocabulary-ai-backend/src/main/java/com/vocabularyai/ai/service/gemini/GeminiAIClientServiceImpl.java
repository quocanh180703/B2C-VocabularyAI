package com.vocabularyai.ai.service.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.entity.RateLimit;
import com.vocabularyai.ai.repository.RateLimitRepository;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import com.vocabularyai.common.exception.ExternalServiceException;
import com.vocabularyai.common.exception.RateLimitExceededException;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of GeminiAIClientService using REST API calls
 * 
 * Features:
 * - Rate limiting (daily token and request quotas)
 * - Exponential backoff retry logic (1s, 2s, 4s, 8s, 16s)
 * - Circuit breaker pattern (fail fast after 5 consecutive failures)
 * - Token counting and tracking
 * - Comprehensive error handling
 * 
 * Note: Calls Gemini API via REST endpoint instead of direct SDK
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiAIClientServiceImpl implements GeminiAIClientService {
    
    private final RestTemplate restTemplate;
    private final RateLimitRepository rateLimitRepository;
    private final UserRepository userRepository;
    private final PromptEngineeringService promptEngineeringService;
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.api-key:}")
    private String geminiApiKey;
    
    @Value("${gemini.model:gemini-3.1-flash-lite}")
    private String modelName;
    
    @Value("${ai.rate-limit.daily-tokens:1000000}")
    private long dailyTokenLimit;
    
    @Value("${ai.rate-limit.daily-requests:1000}")
    private long dailyRequestLimit;
    
    @Value("${ai.retry.max-attempts:5}")
    private int maxRetryAttempts;
    
    @Value("${ai.retry.initial-delay-ms:1000}")
    private int initialDelayMs;
    
    @Value("${ai.circuit-breaker.failure-threshold:5}")
    private int circuitBreakerFailureThreshold;
    
    @Value("${ai.circuit-breaker.reset-timeout-seconds:30}")
    private int circuitBreakerResetTimeoutSeconds;
    
    // ==================== Circuit Breaker State ====================
    
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong circuitBreakerOpenTime = new AtomicLong(0);
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
    
    // ==================== Rate Limit Checking ====================
    
    @Override
    public boolean isRateLimitExceeded(Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime periodStart = now.minus(1, ChronoUnit.DAYS);
            
            var rateLimitOpt = rateLimitRepository
                .findByUserIdAndPeriodStartBeforeAndPeriodEndAfter(userId, periodStart, now);
            
            if (rateLimitOpt.isPresent()) {
                var rateLimit = rateLimitOpt.get();
                return rateLimit.isLimitExceeded(dailyTokenLimit, (int) dailyRequestLimit);
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking rate limit for user: {}, error: {}", userId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public long getRemainingTokens(Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime periodStart = now.minus(1, ChronoUnit.DAYS);
            
            var rateLimitOpt = rateLimitRepository
                .findByUserIdAndPeriodStartBeforeAndPeriodEndAfter(userId, periodStart, now);
            
            if (rateLimitOpt.isPresent()) {
                return rateLimitOpt.get().getRemainingTokens(dailyTokenLimit);
            }
            
            return dailyTokenLimit;
        } catch (Exception e) {
            log.warn("Error getting remaining tokens for user: {}, error: {}", userId, e.getMessage());
            return 0;
        }
    }
    
    @Override
    public long getRemainingRequests(Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime periodStart = now.minus(1, ChronoUnit.DAYS);
            
            var rateLimitOpt = rateLimitRepository
                .findByUserIdAndPeriodStartBeforeAndPeriodEndAfter(userId, periodStart, now);
            
            if (rateLimitOpt.isPresent()) {
                return rateLimitOpt.get().getRemainingRequests((int) dailyRequestLimit);
            }
            
            return dailyRequestLimit;
        } catch (Exception e) {
            log.warn("Error getting remaining requests for user: {}, error: {}", userId, e.getMessage());
            return 0;
        }
    }
    
    // ==================== Main Response Generation ====================
    
    @Override
    @Transactional
    public String generateResponse(String prompt, Long userId) {
        // 1. Check circuit breaker
        if (isCircuitBreakerOpen()) {
            throw new ExternalServiceException(
                "Gemini service temporarily unavailable (circuit breaker open). Reset in " + 
                circuitBreakerResetTimeoutSeconds + "s"
            );
        }
        
        // 2. Check rate limit
        if (isRateLimitExceeded(userId)) {
            long remainingTokens = getRemainingTokens(userId);
            log.warn("Rate limit exceeded for user: {}", userId);
            throw new RateLimitExceededException(
                "Daily token limit exceeded. Remaining: " + remainingTokens,
                System.currentTimeMillis() / 1000 + 86400,
                "DAILY"
            );
        }
        
        // 3. Estimate tokens
        int estimatedTokens = promptEngineeringService.estimateTokenCount(prompt);
        long remainingTokens = getRemainingTokens(userId);
        
        if (estimatedTokens > remainingTokens) {
            log.warn("Not enough tokens for user: {}, needed: {}, remaining: {}", 
                     userId, estimatedTokens, remainingTokens);
            throw new RateLimitExceededException(
                "Not enough tokens for this request",
                System.currentTimeMillis() / 1000 + 86400,
                "DAILY"
            );
        }
        
        // 4. Call Gemini API with retry logic
        long startTime = System.currentTimeMillis();
        String response = callGeminiWithRetry(prompt);
        long responseTime = System.currentTimeMillis() - startTime;
        
        // 5. Update rate limit tracking
        int actualTokens = estimateActualTokens(response);
        updateRateLimit(userId, actualTokens);
        
        // 6. Reset circuit breaker on success
        consecutiveFailures.set(0);
        
        log.info("Generated response for user: {}, tokens: {}, time: {}ms", 
                 userId, actualTokens, responseTime);
        
        return response;
    }
    
    // ==================== Streaming Response ====================
    
    @Override
    public void generateResponseStream(String prompt, Long userId, StreamCallback callback) {
        try {
            // Pre-checks same as non-streaming
            if (isCircuitBreakerOpen()) {
                callback.onError(new ExternalServiceException(
                    "Gemini service temporarily unavailable"
                ));
                return;
            }
            
            if (isRateLimitExceeded(userId)) {
                callback.onError(new RateLimitExceededException(
                    "Daily token limit exceeded",
                    System.currentTimeMillis() / 1000 + 86400,
                    "DAILY"
                ));
                return;
            }
            
            // For now, non-streaming implementation (can enhance with streaming API later)
            String response = generateResponse(prompt, userId);
            callback.onContent(response);
            
            int tokens = estimateActualTokens(response);
            callback.onComplete(tokens);
            
        } catch (Exception e) {
            log.error("Error in streaming response for user: {}, error: {}", userId, e.getMessage());
            callback.onError(e);
        }
    }
    
    // ==================== Retry Logic with Exponential Backoff ====================
    
    private String callGeminiWithRetry(String prompt) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                log.debug("Calling Gemini API, attempt: {}/{}", attempt, maxRetryAttempts);
                
                String response = callGeminiAPI(prompt);
                
                if (response != null && !response.trim().isEmpty()) {
                    log.debug("Gemini API call successful on attempt: {}", attempt);
                    return response;
                }
                
                throw new ExternalServiceException(
                    "Empty response from Gemini API"
                );
                
            } catch (RestClientException e) {
                lastException = e;
                
                if (isRetryableError(e)) {
                    if (attempt < maxRetryAttempts) {
                        long delayMs = calculateBackoffDelay(attempt);
                        log.warn("Gemini API error (retryable), attempt: {}, delay: {}ms, error: {}", 
                                 attempt, delayMs, e.getMessage());
                        
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new ExternalServiceException(
                                "Interrupted during retry"
                            );
                        }
                    } else {
                        log.error("Gemini API failed after {} attempts", maxRetryAttempts);
                        recordCircuitBreakerFailure();
                    }
                } else {
                    log.error("Non-retryable Gemini API error: {}", e.getMessage());
                    recordCircuitBreakerFailure();
                    throw new ExternalServiceException(
                        "Gemini API error: " + e.getMessage(), e
                    );
                }
                
            } catch (Exception e) {
                lastException = e;
                log.error("Unexpected error calling Gemini API: {}", e.getMessage());
                recordCircuitBreakerFailure();
                throw new ExternalServiceException(
                    "Unexpected error: " + e.getMessage(), e
                );
            }
        }
        
        // All retries failed
        recordCircuitBreakerFailure();
        throw new ExternalServiceException(
            "Gemini API failed after " + maxRetryAttempts + " attempts: " + lastException.getMessage(),
            lastException
        );
    }
    
    /**
     * Call Gemini API via REST endpoint
     * 
     * Makes actual HTTP POST request to Google Gemini API:
     * https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
     */
    private String callGeminiAPI(String prompt) {
        try {
            // Build request body
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();
            
            parts.put("text", prompt);
            content.put("parts", new Object[]{parts});
            requestBody.put("contents", new Object[]{content});
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Build URL with API key
            String url = GEMINI_API_URL.replace("{model}", modelName) + "?key=" + geminiApiKey;
            
            // Make actual REST call to Gemini API
            log.debug("[TASK AI] Calling Gemini API - model: {}, timeout: 30s", modelName);
            
            var response = restTemplate.postForEntity(
                new URI(url),
                entity,
                Map.class
            );
            
            log.debug("[TASK AI] Gemini API response status: {}", response.getStatusCode());
            
            // Parse response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                // Extract text from: response.candidates[0].content.parts[0].text
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                
                if (candidates != null && !candidates.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> candidate = candidates.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                    
                    if (contentMap != null) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> partsList = (List<Map<String, Object>>) contentMap.get("parts");
                        
                        if (partsList != null && !partsList.isEmpty()) {
                            String text = (String) partsList.get(0).get("text");
                            
                            if (text != null && !text.trim().isEmpty()) {
                                log.debug("[TASK AI] Gemini response received, length: {} chars", text.length());
                                return text;
                            }
                        }
                    }
                }
                
                log.error("[TASK AI] Invalid Gemini response structure: {}", body);
                throw new ExternalServiceException(
                    "Invalid Gemini response structure - missing candidates/content/parts"
                );
            }
            
            log.error("[TASK AI] Gemini API HTTP error - status: {}", response.getStatusCode());
            throw new ExternalServiceException(
                "Gemini API HTTP " + response.getStatusCode() + " - " + response.getBody()
            );
            
        } catch (RestClientException e) {
            log.error("[TASK AI] Gemini API HTTP error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[TASK AI] Unexpected error calling Gemini API: {}", e.getMessage(), e);
            throw new RestClientException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if error is retryable (network errors, timeouts)
     */
    private boolean isRetryableError(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        
        return message.contains("timeout") ||
               message.contains("connection") ||
               message.contains("500") ||
               message.contains("503") ||
               message.contains("temporarily unavailable");
    }
    
    /**
     * Calculate exponential backoff delay
     * 1s, 2s, 4s, 8s, 16s (capped at 16s)
     */
    private long calculateBackoffDelay(int attempt) {
        long delayMs = initialDelayMs * (long) Math.pow(2, attempt - 1);
        return Math.min(delayMs, 16000); // Cap at 16 seconds
    }
    
    // ==================== Circuit Breaker Pattern ====================
    
    @Override
    public boolean isCircuitBreakerOpen() {
        if (consecutiveFailures.get() < circuitBreakerFailureThreshold) {
            return false;
        }
        
        long openedTime = circuitBreakerOpenTime.get();
        if (openedTime == 0) {
            circuitBreakerOpenTime.set(System.currentTimeMillis());
            return true;
        }
        
        long elapsedMs = System.currentTimeMillis() - openedTime;
        if (elapsedMs > circuitBreakerResetTimeoutSeconds * 1000L) {
            log.info("Circuit breaker timeout expired, attempting to reset");
            consecutiveFailures.set(0);
            circuitBreakerOpenTime.set(0);
            return false;
        }
        
        return true;
    }
    
    private void recordCircuitBreakerFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        
        if (failures >= circuitBreakerFailureThreshold) {
            log.error("Circuit breaker OPEN after {} consecutive failures", failures);
            circuitBreakerOpenTime.set(System.currentTimeMillis());
        }
    }
    
    // ==================== Token Counting & Rate Limit Tracking ====================
    
    @Override
    public int countTokens(String text) {
        return promptEngineeringService.estimateTokenCount(text);
    }
    
    private int estimateActualTokens(String response) {
        return countTokens(response);
    }
    
    @Transactional
    private void updateRateLimit(Long userId, int tokensUsed) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime periodStart = now.minus(1, ChronoUnit.DAYS);
            LocalDateTime periodEnd = now.plus(1, ChronoUnit.DAYS);
            
            var rateLimitOpt = rateLimitRepository
                .findByUserIdAndPeriodStartBeforeAndPeriodEndAfter(userId, periodStart, now);
            
            if (rateLimitOpt.isPresent()) {
                var rateLimit = rateLimitOpt.get();
                rateLimit.setTokensUsed(rateLimit.getTokensUsed() + tokensUsed);
                rateLimit.setRequestCount(rateLimit.getRequestCount() + 1);
                rateLimitRepository.save(rateLimit);
            } else {
                // Create new rate limit record
                var newRateLimit = RateLimit.builder()
                    .user(user)
                    .tokensUsed((long) tokensUsed)
                    .requestCount(1)
                    .periodStart(now)
                    .periodEnd(now.plus(1, ChronoUnit.DAYS))
                    .build();
                rateLimitRepository.save(newRateLimit);
            }
            
            log.debug("Updated rate limit for user: {}, tokens: {}", userId, tokensUsed);
            
        } catch (Exception e) {
            log.warn("Failed to update rate limit for user: {}, error: {}", userId, e.getMessage());
        }
    }
}
