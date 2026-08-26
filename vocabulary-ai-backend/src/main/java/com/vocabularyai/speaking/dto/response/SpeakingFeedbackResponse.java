package com.vocabularyai.speaking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho Speaking Feedback
 * 
 * Chứa đánh giá chi tiết từ AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingFeedbackResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("attemptId")
    private Long attemptId;
    
    @JsonProperty("sessionId")
    private Long sessionId;
    
    @JsonProperty("overallScore")
    private Double overallScore;
    
    @JsonProperty("grammarScore")
    private Double grammarScore;
    
    @JsonProperty("vocabularyScore")
    private Double vocabularyScore;
    
    @JsonProperty("fluencyScore")
    private Double fluencyScore;
    
    @JsonProperty("contentScore")
    private Double contentScore;
    
    @JsonProperty("pronunciationScore")
    private Double pronunciationScore;
    
    @JsonProperty("feedback")
    private String feedback;
    
    @JsonProperty("corrections")
    private String corrections;
    
    @JsonProperty("suggestions")
    private String suggestions;
    
    @JsonProperty("strengths")
    private String strengths;
    
    @JsonProperty("weaknesses")
    private String weaknesses;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
