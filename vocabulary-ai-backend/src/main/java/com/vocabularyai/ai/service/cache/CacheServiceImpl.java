package com.vocabularyai.ai.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.dto.response.ExplanationResponse;
import com.vocabularyai.ai.entity.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed implementation of CacheService
 * 
 * Provides caching for:
 * - AI conversation context (TTL: 30 minutes)
 * - Vocabulary explanations (TTL: 24 hours)
 * - Grammar check results (TTL: 1 hour)
 * - Learning hints (TTL: 1 hour)
 * - Rate limit tracking (TTL: 24 hours)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.cache.ttl-hours:24}")
    private long defaultTtlHours;
    
    @Value("${ai.cache.conversation-ttl-minutes:30}")
    private long conversationTtlMinutes;
    
    @Value("${ai.cache.grammar-ttl-hours:1}")
    private long grammarTtlHours;
    
    @Value("${ai.cache.hint-ttl-hours:1}")
    private long hintTtlHours;
    
    // ==================== Cache Key Prefix Constants ====================
    
    private static final String CONVERSATION_PREFIX = "ai:conversation:";
    private static final String EXPLANATION_PREFIX = "ai:explanation:";
    private static final String RATE_LIMIT_TOKENS_PREFIX = "ai:ratelimit:tokens:";
    private static final String RATE_LIMIT_REQUESTS_PREFIX = "ai:ratelimit:requests:";
    
    // ==================== Generic Cache Operations ====================
    
    @Override
    public void set(String key, Object value, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Cached key: {} with TTL: {} seconds", key, ttlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache key: {}, error: {}", key, e.getMessage());
            // Fail-open: continue without cache instead of throwing exception
        }
    }
    
    @Override
    public void set(String key, Object value) {
        set(key, value, TimeUnit.HOURS.toSeconds(defaultTtlHours));
    }
    
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }
            
            T value = objectMapper.readValue(json, type);
            log.debug("Cache hit for key: {}", key);
            return Optional.of(value);
        } catch (Exception e) {
            log.warn("Failed to deserialize cache key: {}, error: {}", key, e.getMessage());
            // Delete corrupted cache entry
            try {
                redisTemplate.delete(key);
            } catch (Exception ex) {
                log.warn("Failed to delete corrupted cache key: {}", key);
            }
            return Optional.empty();
        }
    }
    
    @Override
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Deleted cache key: {} (existed: {})", key, deleted);
        } catch (Exception e) {
            log.warn("Failed to delete cache key: {}, error: {}", key, e.getMessage());
        }
    }
    
    @Override
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.debug("Deleted {} keys matching pattern: {}", deletedCount, pattern);
            }
        } catch (Exception e) {
            log.warn("Failed to delete keys matching pattern: {}, error: {}", pattern, e.getMessage());
        }
    }
    
    // ==================== Conversation Context Operations ====================
    
    @Override
    public void cacheConversationContext(Long conversationId, List<AiMessage> messages, long ttlSeconds) {
        try {
            String key = CONVERSATION_PREFIX + conversationId;
            String json = objectMapper.writeValueAsString(messages);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Cached conversation context: {} with {} messages, TTL: {} seconds", 
                     conversationId, messages.size(), ttlSeconds);
        } catch (Exception e) {
            log.warn("Failed to cache conversation context: {}, error: {}", conversationId, e.getMessage());
        }
    }
    
    @Override
    public Optional<List<AiMessage>> getConversationContext(Long conversationId) {
        try {
            String key = CONVERSATION_PREFIX + conversationId;
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                log.debug("Cache miss for conversation context: {}", conversationId);
                return Optional.empty();
            }
            
            List<AiMessage> messages = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, AiMessage.class));
            log.debug("Cache hit for conversation context: {} with {} messages", conversationId, messages.size());
            return Optional.of(messages);
        } catch (Exception e) {
            log.warn("Failed to get conversation context: {}, error: {}", conversationId, e.getMessage());
            // Delete corrupted cache
            invalidateConversationContext(conversationId);
            return Optional.empty();
        }
    }
    
    @Override
    public void invalidateConversationContext(Long conversationId) {
        try {
            String key = CONVERSATION_PREFIX + conversationId;
            delete(key);
            log.debug("Invalidated conversation context: {}", conversationId);
        } catch (Exception e) {
            log.warn("Failed to invalidate conversation context: {}, error: {}", conversationId, e.getMessage());
        }
    }
    
    // ==================== Vocabulary Explanation Operations ====================
    
    @Override
    public void cacheExplanation(Long wordId, ExplanationResponse explanation) {
        cacheExplanationWithContext(wordId, null, explanation);
    }
    
    @Override
    public void cacheExplanationWithContext(Long wordId, String context, ExplanationResponse explanation) {
        try {
            String key = buildExplanationKey(wordId, context);
            long ttlSeconds = TimeUnit.HOURS.toSeconds(defaultTtlHours);
            String json = objectMapper.writeValueAsString(explanation);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Cached explanation for word: {}, TTL: {} hours", wordId, defaultTtlHours);
        } catch (Exception e) {
            log.warn("Failed to cache explanation for word: {}, error: {}", wordId, e.getMessage());
        }
    }
    
    @Override
    public Optional<ExplanationResponse> getExplanation(Long wordId) {
        return getExplanationWithContext(wordId, null);
    }
    
    @Override
    public Optional<ExplanationResponse> getExplanationWithContext(Long wordId, String context) {
        try {
            String key = buildExplanationKey(wordId, context);
            return get(key, ExplanationResponse.class);
        } catch (Exception e) {
            log.warn("Failed to get explanation for word: {}, error: {}", wordId, e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public void deleteExplanation(Long wordId) {
        try {
            String pattern = EXPLANATION_PREFIX + wordId + ":*";
            deletePattern(pattern);
            log.debug("Deleted explanations for word: {}", wordId);
        } catch (Exception e) {
            log.warn("Failed to delete explanations for word: {}, error: {}", wordId, e.getMessage());
        }
    }
    
    // ==================== Rate Limit Operations ====================
    
    @Override
    public void incrementRateLimitTokens(Long userId, long tokens) {
        try {
            String key = RATE_LIMIT_TOKENS_PREFIX + userId;
            redisTemplate.opsForValue().increment(key, tokens);
            
            // Set TTL if key was just created (24 hours)
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl == -1) { // No TTL set
                redisTemplate.expire(key, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
            }
            
            log.debug("Incremented rate limit tokens for user: {} by: {}", userId, tokens);
        } catch (Exception e) {
            log.warn("Failed to increment rate limit tokens for user: {}, error: {}", userId, e.getMessage());
        }
    }
    
    @Override
    public void incrementRateLimitRequests(Long userId) {
        try {
            String key = RATE_LIMIT_REQUESTS_PREFIX + userId;
            redisTemplate.opsForValue().increment(key);
            
            // Set TTL if key was just created (24 hours)
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl == -1) {
                redisTemplate.expire(key, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
            }
            
            log.debug("Incremented rate limit requests for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to increment rate limit requests for user: {}, error: {}", userId, e.getMessage());
        }
    }
    
    @Override
    public long getRateLimitTokensUsed(Long userId) {
        try {
            String key = RATE_LIMIT_TOKENS_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            log.warn("Failed to get rate limit tokens for user: {}, error: {}", userId, e.getMessage());
            return 0L;
        }
    }
    
    @Override
    public long getRateLimitRequestCount(Long userId) {
        try {
            String key = RATE_LIMIT_REQUESTS_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            log.warn("Failed to get rate limit requests for user: {}, error: {}", userId, e.getMessage());
            return 0L;
        }
    }
    
    @Override
    public void resetRateLimitCounters(Long userId) {
        try {
            delete(RATE_LIMIT_TOKENS_PREFIX + userId);
            delete(RATE_LIMIT_REQUESTS_PREFIX + userId);
            log.debug("Reset rate limit counters for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to reset rate limit counters for user: {}, error: {}", userId, e.getMessage());
        }
    }
    
    @Override
    public void setRateLimitCounters(Long userId, long tokens, long requests, long ttlSeconds) {
        try {
            String tokensKey = RATE_LIMIT_TOKENS_PREFIX + userId;
            String requestsKey = RATE_LIMIT_REQUESTS_PREFIX + userId;
            
            redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens), ttlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(requestsKey, String.valueOf(requests), ttlSeconds, TimeUnit.SECONDS);
            
            log.debug("Set rate limit counters for user: {} (tokens: {}, requests: {}, TTL: {} seconds)", 
                     userId, tokens, requests, ttlSeconds);
        } catch (Exception e) {
            log.warn("Failed to set rate limit counters for user: {}, error: {}", userId, e.getMessage());
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Build cache key for explanation with optional context
     * 
     * If context is provided, it's hashed to create a stable key
     * 
     * @param wordId ID of the word
     * @param context Optional context (e.g., user history, learning level)
     * @return Cache key
     */
    private String buildExplanationKey(Long wordId, String context) {
        if (context == null || context.trim().isEmpty()) {
            return EXPLANATION_PREFIX + wordId;
        }
        
        String contextHash = hashString(context);
        return EXPLANATION_PREFIX + wordId + ":" + contextHash;
    }
    
    /**
     * Hash a string using SHA-256
     * 
     * @param input String to hash
     * @return Hex representation of hash
     */
    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return HexFormat.of().formatHex(hash).substring(0, 16); // Use first 16 chars
        } catch (NoSuchAlgorithmException e) {
            // Fallback: use Base64 encoding if hash not available
            log.warn("SHA-256 not available, using Base64 encoding for context hash");
            return Base64.getEncoder().encodeToString(input.getBytes()).substring(0, 16);
        }
    }
}
