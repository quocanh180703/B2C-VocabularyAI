package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * LearningProfileResponse
 * 
 * API response DTO for GET /api/ai/personalized/profile
 * 
 * Returns user's aggregated learning profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProfileResponse {
    
    /**
     * Aggregated learning profile data
     */
    @JsonProperty("profile")
    private LearningProfileDto profile;
    
    /**
     * Whether profile was returned from cache
     */
    @JsonProperty("fromCache")
    private Boolean fromCache;
    
    /**
     * Cache TTL remaining in seconds (if cached)
     */
    @JsonProperty("cacheTtlSeconds")
    private Long cacheTtlSeconds;
    
    /**
     * Response timestamp
     */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
