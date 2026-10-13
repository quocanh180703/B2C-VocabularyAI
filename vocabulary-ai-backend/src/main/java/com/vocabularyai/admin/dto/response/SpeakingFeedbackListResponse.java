package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Lightweight speaking feedback list item")
public class SpeakingFeedbackListResponse {
    
    @Schema(description = "Feedback ID", example = "123")
    private Long id;
    
    @Schema(description = "Session ID if this is session-level feedback")
    private Long sessionId;
    
    @Schema(description = "Attempt ID if this is attempt-level feedback")
    private Long attemptId;
    
    @Schema(description = "Overall score", example = "85.5")
    private Double overallScore;
    
    @Schema(description = "Grammar score", example = "80.0")
    private Double grammarScore;
    
    @Schema(description = "Vocabulary score", example = "85.0")
    private Double vocabularyScore;
    
    @Schema(description = "Fluency score", example = "90.0")
    private Double fluencyScore;
    
    @Schema(description = "Content score", example = "88.0")
    private Double contentScore;
    
    @Schema(description = "Pronunciation score", example = "82.0")
    private Double pronunciationScore;
    
    @Schema(description = "Brief feedback summary (truncated)")
    private String feedbackSummary;
    
    @Schema(description = "Whether this is attempt-level or session-level feedback")
    private String feedbackType;
    
    @Schema(description = "Creation timestamp", example = "2026-10-03T10:30:00")
    private LocalDateTime createdAt;
}
