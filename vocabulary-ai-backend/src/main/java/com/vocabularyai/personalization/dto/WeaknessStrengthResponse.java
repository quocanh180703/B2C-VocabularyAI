package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API response wrapper for weakness/strength analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeaknessStrengthResponse {
    
    /**
     * HTTP status code
     */
    @JsonProperty("statusCode")
    private Integer statusCode;
    
    /**
     * Response message
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Weakness and strength analysis data
     */
    @JsonProperty("data")
    private WeaknessStrengthDto data;
    
    /**
     * Timestamp when response was generated
     */
    @JsonProperty("timestamp")
    private long timestamp;
}
