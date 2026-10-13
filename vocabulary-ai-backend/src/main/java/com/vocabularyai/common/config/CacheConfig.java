package com.vocabularyai.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * CacheConfig
 * 
 * Redis caching configuration for VocabularyAI
 * 
 * Cache strategy:
 * - words (default): 1 hour TTL
 *   - word:{id} - Individual word by ID
 *   - word:lemma:{lemma} - Individual word by text
 *   - words:level:{level}:page:{page} - Words by CEFR level
 *   - words:search:{query} - Search results
 *   - words:autocomplete:{prefix} - Autocomplete results
 *   - words:statistics - Word count statistics
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Redis cache manager with custom TTLs
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // Default cache config: 1 hour
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();

        // Custom cache configs
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // words cache: 1 hour (most common queries)
        cacheConfigs.put("words", RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues());

        // Build cache manager
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
