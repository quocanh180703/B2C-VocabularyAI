package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Learning progress report")
public class LearningProgressReportResponse {
    
    @Schema(description = "Total learning attempts", example = "1500")
    private Long totalAttempts;
    
    @Schema(description = "Average score percentage", example = "78.5")
    private Double avgScore;
    
    @Schema(description = "Average completion time in seconds", example = "450")
    private Long avgCompletionTime;
    
    @Schema(description = "Number of unique users", example = "250")
    private Long uniqueUsers;
}
