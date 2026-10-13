package com.vocabularyai.ai.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration
 *
 * Configures Redis serialization to use Jackson JSON serializer instead of JDK serialization.
 * This enables proper caching of DTOs and responses via @Cacheable annotations.
 *
 * Benefits:
 * - Uses JSON serialization (more portable than JDK serialization)
 * - Works with any POJO (no need for implements Serializable)
 * - Human-readable cache values for debugging
 * - No SerializationException for non-Serializable objects
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate bean using Jackson2JsonRedisSerializer
     *
     * Configures RedisTemplate to use:
     * - StringRedisSerializer for keys (human-readable)
     * - Jackson2JsonRedisSerializer for values (JSON format)
     *
     * This configuration is used by Spring Cache (@Cacheable) and manual RedisTemplate usage.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Initializing RedisTemplate with Jackson2JsonRedisSerializer");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Jackson serializer configuration
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Enable polymorphic type handling (needed for complex objects)
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // Configure visibility for reflection
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // String serializer for keys (more readable than JSON-encoded strings)
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Configure key serialization
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // Configure value serialization
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        
        log.info("RedisTemplate initialized successfully with JSON serialization");
        return template;
    }
}
