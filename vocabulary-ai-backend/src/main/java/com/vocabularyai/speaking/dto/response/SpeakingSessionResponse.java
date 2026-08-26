package com.vocabularyai.speaking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho Speaking Session
 * 
 * Dùng để trả về thông tin phiên speaking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingSessionResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("topic")
    private String topic;
    
    @JsonProperty("level")
    private SpeakingLevel level;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("status")
    private SpeakingStatus status;
    
    @JsonProperty("startedAt")
    private LocalDateTime startedAt;
    
    @JsonProperty("completedAt")
    private LocalDateTime completedAt;
    
    @JsonProperty("overallScore")
    private Double overallScore;
    
    @JsonProperty("attemptCount")
    private Integer attemptCount;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
