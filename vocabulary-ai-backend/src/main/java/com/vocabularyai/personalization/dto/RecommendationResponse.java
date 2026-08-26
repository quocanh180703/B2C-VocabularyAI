package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API response wrapper cho gợi ý học tập
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    
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
     * Dữ liệu gợi ý
     */
    @JsonProperty("data")
    private RecommendationDto data;
    
    /**
     * Timestamp khi response được tạo
     */
    @JsonProperty("timestamp")
    private long timestamp;
}
