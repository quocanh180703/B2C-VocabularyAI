package com.vocabularyai.learning.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Review Word Request DTO
 * 
 * Used when user reviews a word (answer correct or incorrect)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewWordRequest {
    
    @NotNull(message = "Correct flag is required")
    private Boolean correct;
}
