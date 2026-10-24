package com.vocabularyai.speaking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho Speaking Attempt
 * 
 * Chứa thông tin về một lần ghi âm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingAttemptResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("sessionId")
    private Long sessionId;
    
    @JsonProperty("audioUrl")
    private String audioUrl;
    
    @JsonProperty("audioDurationSeconds")
    private Integer audioDurationSeconds;
    
    @JsonProperty("transcript")
    private String transcript;
    
    @JsonProperty("transcriptConfidence")
    private Double transcriptConfidence;
    
    @JsonProperty("attemptNumber")
    private Integer attemptNumber;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    /**
     * Feedback cho attempt này (nếu có)
     */
    @JsonProperty("feedback")
    private SpeakingFeedbackResponse feedback;

    @JsonProperty("overallScore")
    public Double getOverallScore() {
        return feedback != null ? feedback.getOverallScore() : null;
    }
}
