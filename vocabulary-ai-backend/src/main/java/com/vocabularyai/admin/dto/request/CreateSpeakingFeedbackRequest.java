package com.vocabularyai.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create speaking feedback")
public class CreateSpeakingFeedbackRequest {
    
    @Schema(description = "Session ID for session-level feedback (one of session or attempt must be provided)")
    private Long sessionId;
    
    @Schema(description = "Attempt ID for attempt-level feedback (one of session or attempt must be provided)")
    private Long attemptId;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Overall score (0-100)", example = "85.5")
    private Double overallScore;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Grammar score (0-100)", example = "80.0")
    private Double grammarScore;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Vocabulary score (0-100)", example = "85.0")
    private Double vocabularyScore;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Fluency score (0-100)", example = "90.0")
    private Double fluencyScore;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Content score (0-100)", example = "88.0")
    private Double contentScore;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Pronunciation score (0-100)", example = "82.0")
    private Double pronunciationScore;
    
    @Size(max = 5000)
    @Schema(description = "General feedback text")
    private String feedback;
    
    @Schema(description = "Array of corrections: [{\"original\": \"...\", \"corrected\": \"...\", \"reason\": \"...\"}]")
    private List<CorrectionItem> corrections;
    
    @Schema(description = "Array of suggestions: [{\"area\": \"grammar\", \"suggestion\": \"...\"}]")
    private List<SuggestionItem> suggestions;
    
    @Schema(description = "Array of strengths: [{\"strength\": \"...\"}]")
    private List<StrengthItem> strengths;
    
    @Schema(description = "Array of weaknesses: [{\"weakness\": \"...\"}]")
    private List<WeaknessItem> weaknesses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorrectionItem {
        @NotBlank
        private String original;
        @NotBlank
        private String corrected;
        private String reason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestionItem {
        @NotBlank
        private String area;
        @NotBlank
        private String suggestion;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrengthItem {
        @NotBlank
        private String strength;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeaknessItem {
        @NotBlank
        private String weakness;
    }
}
