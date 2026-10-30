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
import com.vocabularyai.ai.entity.AiRequestLogStatus;
import com.vocabularyai.ai.service.tracking.AiUsageTrackingService;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
 * - Intelligent local fallback when Gemini API key is unconfigured or service returns 401/503
 * 
 * Note: Calls Gemini API via REST endpoint instead of direct SDK
 */
@Service
@Slf4j
public class GeminiAIClientServiceImpl implements GeminiAIClientService {
    
    private final RestTemplate restTemplate;
    private final RateLimitRepository rateLimitRepository;
    private final UserRepository userRepository;
    private final PromptEngineeringService promptEngineeringService;
    private final ObjectMapper objectMapper;
    private final AiUsageTrackingService aiUsageTrackingService;
    
    public GeminiAIClientServiceImpl(
            RestTemplate restTemplate,
            RateLimitRepository rateLimitRepository,
            UserRepository userRepository,
            PromptEngineeringService promptEngineeringService,
            ObjectMapper objectMapper,
            AiUsageTrackingService aiUsageTrackingService) {
        this.restTemplate = restTemplate;
        this.rateLimitRepository = rateLimitRepository;
        this.userRepository = userRepository;
        this.promptEngineeringService = promptEngineeringService;
        this.objectMapper = objectMapper;
        this.aiUsageTrackingService = aiUsageTrackingService;
    }
    
    @Value("${ai.chat.url:${gemini.api-url:https://api.xah.io/v1/chat/completions}}")
    private String aiApiUrl;

    @Value("${ai.chat.api-key:${gemini.api-key:sk-4858c9d3176689b889b6a9bc54d391d32502deebc79970b4e4dfb98833e3c68b}}")
    private String geminiApiKey;
    
    @Value("${ai.chat.model:${gemini.model:thanhnhan9023/grok-4.6}}")
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
        if (userId == null) {
            return false;
        }
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
        if (userId == null) {
            return dailyTokenLimit;
        }
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
        if (userId == null) {
            return dailyRequestLimit;
        }
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
    public String generateResponse(String prompt, Long userId) {
        return generateResponse(prompt, userId, true);
    }

    @Override
    public String generateResponse(String prompt, Long userId, boolean allowFallback) {
        String feature = inferFeature(prompt);

        // 1. Check circuit breaker
        if (isCircuitBreakerOpen()) {
            if (!allowFallback) {
                log.warn("Gemini circuit breaker is OPEN - failing fast without local fallback for feature: {}", feature);
                if (aiUsageTrackingService != null) {
                    aiUsageTrackingService.recordLog(userId, feature, modelName, 0, 0, 0,
                            AiRequestLogStatus.CIRCUIT_BREAKER_OPEN, "Circuit breaker is open");
                }
                throw new ExternalServiceException("AI circuit breaker is OPEN - service temporarily unavailable");
            }
            log.warn("Gemini circuit breaker is OPEN - generating fallback response");
            if (aiUsageTrackingService != null) {
                aiUsageTrackingService.recordLog(userId, feature, modelName, 0, 0, 0,
                        AiRequestLogStatus.CIRCUIT_BREAKER_OPEN, "Circuit breaker is open");
            }
            return generateFallbackResponse(prompt);
        }
        
        // 2. Check rate limit
        if (userId != null && isRateLimitExceeded(userId)) {
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
        if (userId != null) {
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
        }
        
        // 4. Call Gemini API with retry logic
        long startTime = System.currentTimeMillis();
        String response;
        AiRequestLogStatus logStatus = AiRequestLogStatus.SUCCESS;
        String logError = null;

        try {
            response = callGeminiWithRetry(prompt);
            consecutiveFailures.set(0);
        } catch (Exception e) {
            if (!allowFallback) {
                log.error("Gemini API call failed without fallback for feature {}: {}", feature, e.getMessage());
                if (aiUsageTrackingService != null) {
                    aiUsageTrackingService.recordLog(
                            userId,
                            feature,
                            modelName,
                            estimatedTokens,
                            0,
                            System.currentTimeMillis() - startTime,
                            AiRequestLogStatus.FAILED,
                            e.getMessage()
                    );
                }
                if (e instanceof ExternalServiceException) {
                    throw (ExternalServiceException) e;
                }
                throw new ExternalServiceException("Gemini API call failed: " + e.getMessage(), e);
            }

            log.warn("Gemini API call failed ({}), falling back to local intelligent response", e.getMessage());
            response = generateFallbackResponse(prompt);
            logStatus = AiRequestLogStatus.FALLBACK;
            logError = e.getMessage();
        }
        long responseTime = System.currentTimeMillis() - startTime;
        
        // 5. Update rate limit tracking (only for authenticated users)
        int actualTokens = estimateActualTokens(response);
        if (userId != null) {
            updateRateLimit(userId, actualTokens);
        }
        
        // 6. Record usage metrics for AI Cost Optimizer
        if (aiUsageTrackingService != null) {
            aiUsageTrackingService.recordLog(
                    userId,
                    feature,
                    modelName,
                    estimatedTokens,
                    actualTokens,
                    responseTime,
                    logStatus,
                    logError
            );
        }

        log.info("Generated response for user: {}, tokens: {}, time: {}ms", 
                 userId, actualTokens, responseTime);
        
        return response;
    }

    private String inferFeature(String prompt) {
        if (prompt == null) return "AI_TUTOR_CHAT";
        if (prompt.contains("12 chủ đề") || prompt.contains("TOPIC") || prompt.contains("chủ đề cố định") || prompt.contains("phân loại chủ đề")) {
            return "TOPIC_CLASSIFY";
        }
        if (prompt.contains("Text to check:") || prompt.contains("correctedText") || prompt.contains("grammar")) {
            return "GRAMMAR_CHECK";
        }
        if (prompt.contains("pronunciation") || prompt.contains("phonetic") || prompt.contains("speaking")) {
            return "SPEAKING_FEEDBACK";
        }
        if (prompt.contains("Vocabulary:") || prompt.contains("Explain the word") || prompt.contains("\"meaning\"")) {
            return "VOCAB_EXPLAIN";
        }
        if (prompt.contains("quiz") || prompt.contains("question") || prompt.contains("multiple-choice")) {
            return "QUIZ_GEN";
        }
        return "AI_TUTOR_CHAT";
    }
    
    // ==================== Streaming Response ====================
    
    @Override
    public void generateResponseStream(String prompt, Long userId, StreamCallback callback) {
        try {
            // Pre-checks same as non-streaming
            if (isCircuitBreakerOpen()) {
                String fallback = generateFallbackResponse(prompt);
                callback.onContent(fallback);
                callback.onComplete(estimateActualTokens(fallback));
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
        if (aiApiUrl != null && (aiApiUrl.contains("chat/completions") || !aiApiUrl.contains("generativelanguage.googleapis.com"))) {
            return callOpenAiChatAPI(prompt);
        }
        return callGoogleGeminiAPI(prompt);
    }

    private String callOpenAiChatAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(geminiApiKey.trim());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.debug("[TASK AI] Calling OpenAI-compatible Chat API - url: {}, model: {}", aiApiUrl, modelName);

            var response = restTemplate.postForEntity(
                new URI(aiApiUrl),
                entity,
                Map.class
            );

            log.debug("[TASK AI] AI API response status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        String text = (String) message.get("content");
                        if (text != null && !text.trim().isEmpty()) {
                            log.debug("[TASK AI] AI response received, length: {} chars", text.length());
                            return text;
                        }
                    }
                }

                log.error("[TASK AI] Invalid AI response structure: {}", body);
                throw new ExternalServiceException("Invalid AI response structure - missing choices/message");
            }

            log.error("[TASK AI] AI API HTTP error - status: {}", response.getStatusCode());
            throw new ExternalServiceException("AI API HTTP " + response.getStatusCode() + " - " + response.getBody());

        } catch (RestClientException e) {
            log.error("[TASK AI] AI API HTTP error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[TASK AI] Unexpected error calling AI API: {}", e.getMessage(), e);
            throw new RestClientException("Failed to call AI API: " + e.getMessage(), e);
        }
    }

    private String callGoogleGeminiAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> parts = new HashMap<>();

            parts.put("text", prompt);
            content.put("parts", new Object[]{parts});
            requestBody.put("contents", new Object[]{content});

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = GEMINI_API_URL.replace("{model}", modelName) + "?key=" + geminiApiKey;

            log.debug("[TASK AI] Calling Google Gemini API - model: {}, timeout: 30s", modelName);

            var response = restTemplate.postForEntity(
                new URI(url),
                entity,
                Map.class
            );

            log.debug("[TASK AI] Gemini API response status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
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
                throw new ExternalServiceException("Invalid Gemini response structure - missing candidates/content/parts");
            }

            log.error("[TASK AI] Gemini API HTTP error - status: {}", response.getStatusCode());
            throw new ExternalServiceException("Gemini API HTTP " + response.getStatusCode() + " - " + response.getBody());

        } catch (RestClientException e) {
            log.error("[TASK AI] Gemini API HTTP error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[TASK AI] Unexpected error calling Gemini API: {}", e.getMessage(), e);
            throw new RestClientException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if error is retryable (network errors, timeouts, 429, 5xx)
     */
    private boolean isRetryableError(Exception e) {
        if (e == null) {
            return false;
        }

        // 1. Direct Spring/Java I/O or network timeout exceptions
        if (e instanceof org.springframework.web.client.ResourceAccessException) {
            return true;
        }
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof java.net.SocketTimeoutException
                    || cause instanceof java.net.ConnectException
                    || cause instanceof java.net.SocketException) {
                return true;
            }
            cause = cause.getCause();
        }

        // 2. HTTP status code inspection
        if (e instanceof org.springframework.web.client.HttpStatusCodeException httpEx) {
            int statusCode = httpEx.getStatusCode().value();
            // 429 Too Many Requests, or 5xx Server Errors are retryable
            if (statusCode == 429 || (statusCode >= 500 && statusCode <= 599)) {
                return true;
            }
            // 400, 401, 403, 404, etc. are non-retryable client errors
            if (statusCode >= 400 && statusCode < 500) {
                return false;
            }
        }

        // 3. Fallback message inspection (case-insensitive)
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        String lower = message.toLowerCase();
        return lower.contains("timed out")
                || lower.contains("timeout")
                || lower.contains("connection")
                || lower.contains("reset")
                || lower.contains("429")
                || lower.contains("500")
                || lower.contains("502")
                || lower.contains("503")
                || lower.contains("504")
                || lower.contains("rate limit")
                || lower.contains("temporarily unavailable");
    }
    
    /**
     * Calculate exponential backoff delay
     * 1.5s, 3s, 6s, 12s (capped at 16s)
     */
    private long calculateBackoffDelay(int attempt) {
        long delayMs = (long) (initialDelayMs * Math.pow(2, attempt - 1));
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
    
    private void updateRateLimit(Long userId, int tokensUsed) {
        if (userId == null) {
            return;
        }
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

    // ==================== Intelligent Local Fallback ====================

    private String generateFallbackResponse(String prompt) {
        if (prompt.contains("Text to check:") || prompt.contains("\"correctedText\"")) {
            return generateGrammarFallback(prompt);
        }
        if (prompt.contains("Vocabulary:") || prompt.contains("Explain the word") || prompt.contains("\"meaning\"")) {
            return generateVocabularyFallback(prompt);
        }
        if (prompt.contains("Hint for learning") || prompt.contains("hint") || prompt.contains("attemptCount")) {
            return generateHintFallback(prompt);
        }
        return generateChatFallback(prompt);
    }

    private String generateGrammarFallback(String prompt) {
        String text = "";
        int start = prompt.indexOf("---\n");
        if (start != -1) {
            int end = prompt.indexOf("\n---", start + 4);
            if (end != -1) {
                text = prompt.substring(start + 4, end).trim();
            }
        }
        if (text.isEmpty()) {
            text = "Hello world";
        }

        String original = text;
        String corrected = text;
        List<Map<String, Object>> errors = new ArrayList<>();
        List<Map<String, Object>> suggestions = new ArrayList<>();

        // 1. Capitalization of first letter or standalone 'i'
        if (corrected.startsWith("i ") || corrected.equals("i")) {
            corrected = "I" + corrected.substring(1);
            Map<String, Object> err = new HashMap<>();
            err.put("startOffset", 0);
            err.put("endOffset", 1);
            err.put("errorType", "PUNCTUATION");
            err.put("suggestion", "I");
            err.put("explanation", "Đại từ 'I' (tôi) luôn luôn phải viết hoa trong tiếng Anh.");
            errors.add(err);
        }

        // 2. Past tense mismatch with yesterday / last / ago
        boolean hasPastIndicator = original.matches("(?i).*\\b(yesterday|last night|last week|ago|in the past)\\b.*");
        if (hasPastIndicator) {
            if (corrected.matches("(?i).*\\bgo\\b.*")) {
                int goIdx = corrected.toLowerCase().indexOf("go");
                corrected = corrected.substring(0, goIdx) + "went" + corrected.substring(goIdx + 2);
                Map<String, Object> err = new HashMap<>();
                err.put("startOffset", goIdx);
                err.put("endOffset", goIdx + 2);
                err.put("errorType", "GRAMMAR");
                err.put("suggestion", "went");
                err.put("explanation", "Khi trong câu có từ chỉ thời gian quá khứ ('yesterday'), động từ 'go' cần chia thì quá khứ đơn (V2) thành 'went'.");
                errors.add(err);
            } else if (corrected.matches("(?i).*\\bsee\\b.*")) {
                int seeIdx = corrected.toLowerCase().indexOf("see");
                corrected = corrected.substring(0, seeIdx) + "saw" + corrected.substring(seeIdx + 3);
                Map<String, Object> err = new HashMap<>();
                err.put("startOffset", seeIdx);
                err.put("endOffset", seeIdx + 3);
                err.put("errorType", "GRAMMAR");
                err.put("suggestion", "saw");
                err.put("explanation", "Động từ 'see' ở thì quá khứ đơn đổi thành 'saw'.");
                errors.add(err);
            } else if (corrected.matches("(?i).*\\bbuy\\b.*")) {
                int buyIdx = corrected.toLowerCase().indexOf("buy");
                corrected = corrected.substring(0, buyIdx) + "bought" + corrected.substring(buyIdx + 3);
                Map<String, Object> err = new HashMap<>();
                err.put("startOffset", buyIdx);
                err.put("endOffset", buyIdx + 3);
                err.put("errorType", "GRAMMAR");
                err.put("suggestion", "bought");
                err.put("explanation", "Động từ 'buy' ở thì quá khứ đơn đổi thành 'bought'.");
                errors.add(err);
            }
        }

        // 3. Subject-verb agreement (he/she/it + don't)
        if (corrected.matches("(?i).*\\b(he|she|it)\\s+don'?t\\b.*")) {
            int idx = corrected.toLowerCase().indexOf("don");
            int endIdx = corrected.toLowerCase().indexOf(" ", idx);
            if (endIdx == -1) endIdx = corrected.length();
            corrected = corrected.substring(0, idx) + "doesn't" + corrected.substring(endIdx);
            Map<String, Object> err = new HashMap<>();
            err.put("startOffset", idx);
            err.put("endOffset", endIdx);
            err.put("errorType", "GRAMMAR");
            err.put("suggestion", "doesn't");
            err.put("explanation", "Chủ ngữ ngôi thứ ba số ít (He/She/It) đi với trợ động từ phủ định 'doesn't', không dùng 'don't'.");
            errors.add(err);
        }

        // 4. End punctuation
        if (!corrected.endsWith(".") && !corrected.endsWith("?") && !corrected.endsWith("!")) {
            corrected = corrected + ".";
            Map<String, Object> sug = new HashMap<>();
            sug.put("errorType", "STYLE");
            sug.put("suggestion", corrected);
            sug.put("explanation", "Kết thúc câu bằng dấu chấm câu để hoàn chỉnh câu văn.");
            suggestions.add(sug);
        }

        // Fallback capitalization if start of sentence is lower case
        if (errors.isEmpty() && Character.isLowerCase(corrected.charAt(0))) {
            corrected = Character.toUpperCase(corrected.charAt(0)) + corrected.substring(1);
            Map<String, Object> err = new HashMap<>();
            err.put("startOffset", 0);
            err.put("endOffset", 1);
            err.put("errorType", "PUNCTUATION");
            err.put("suggestion", corrected.substring(0, 1));
            err.put("explanation", "Viết hoa chữ cái đầu câu.");
            errors.add(err);
        }

        try {
            Map<String, Object> root = new HashMap<>();
            root.put("correctedText", corrected);
            root.put("errors", errors);
            root.put("suggestions", suggestions);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"correctedText\":\"" + corrected + "\",\"errors\":[],\"suggestions\":[]}";
        }
    }

    private String generateChatFallback(String prompt) {
        return "Hello! I am your AI English Learning Assistant. How can I help you practice your English speaking today?";
    }

    private String generateVocabularyFallback(String prompt) {
        return "{\"meaning\":\"Ý nghĩa và giải thích từ vựng tiếng Anh theo ngữ cảnh thực tế.\",\"examples\":[\"Practice makes perfect when learning vocabulary.\"],\"synonyms\":[\"word\",\"lexicon\"]}";
    }

    private String generateHintFallback(String prompt) {
        return "{\"hint\":\"Gợi ý: Hãy nhớ lại nghĩa và cách phát âm của từ trong các bài học trước nhé!\",\"difficulty\":\"medium\"}";
    }
}
