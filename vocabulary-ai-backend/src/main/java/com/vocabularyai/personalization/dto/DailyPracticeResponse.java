package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API response wrapper cho hoạt động hàng ngày
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPracticeResponse {
    
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
     * Dữ liệu hoạt động hôm nay
     */
    @JsonProperty("data")
    private DailyPracticeDto data;
    
    /**
     * Timestamp
     */
    @JsonProperty("timestamp")
    private long timestamp;
}
