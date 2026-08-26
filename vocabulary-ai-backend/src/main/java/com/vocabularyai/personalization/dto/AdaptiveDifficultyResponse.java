package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AdaptiveDifficultyResponse
 * 
 * Wrapper cho API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdaptiveDifficultyResponse {
    
    @JsonProperty("statusCode")
    private Integer statusCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private AdaptiveDifficultyDto data;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("fromCache")
    private Boolean fromCache;
    
    @JsonProperty("cacheTtlSeconds")
    private Long cacheTtlSeconds;
}
