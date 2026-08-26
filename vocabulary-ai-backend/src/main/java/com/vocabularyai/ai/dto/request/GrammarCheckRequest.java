package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for grammar checking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarCheckRequest {
    
    @NotBlank(message = "Text cannot be blank")
    @Size(min = 1, max = 10000, message = "Text must be between 1 and 10000 characters")
    private String text;
    
    @Size(max = 100, message = "Language must not exceed 100 characters")
    @Builder.Default
    private String language = "English";
    
    @Builder.Default
    private Boolean includeSuggestions = true;
}
