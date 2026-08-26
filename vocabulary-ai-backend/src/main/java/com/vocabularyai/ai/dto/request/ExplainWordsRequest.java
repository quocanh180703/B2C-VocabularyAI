package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for explaining multiple words
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplainWordsRequest {
    
    @NotEmpty(message = "Word IDs list cannot be empty")
    @Size(max = 100, message = "Can explain maximum 100 words at a time")
    private List<Long> wordIds;
}
