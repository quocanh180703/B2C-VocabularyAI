package com.vocabularyai.speaking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho Speaking Session Detail (kèm attempts và feedback)
 * 
 * Dùng để trả về thông tin đầy đủ của một phiên speaking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingSessionSummaryResponse {
    
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
    
    /**
     * Danh sách các attempts trong phiên này
     */
    @JsonProperty("attempts")
    private List<SpeakingAttemptResponse> attempts;
    
    /**
     * Feedback tổng hợp cho cả phiên
     */
    @JsonProperty("feedback")
    private SpeakingFeedbackResponse feedback;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
