package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for explaining a single word
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplainWordRequest {
    
    @NotNull(message = "Word ID cannot be null")
    @Min(value = 1, message = "Word ID must be positive")
    private Long wordId;
    
    @Size(max = 500, message = "Context must not exceed 500 characters")
    private String context;
}
