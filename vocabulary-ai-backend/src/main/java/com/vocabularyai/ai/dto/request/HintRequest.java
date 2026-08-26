package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for getting learning hint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HintRequest {
    
    @NotNull(message = "Learning progress ID cannot be null")
    @Min(value = 1, message = "Learning progress ID must be positive")
    private Long learningProgressId;
    
    @Min(value = 0, message = "Attempt count cannot be negative")
    private Integer attemptCount;
}
