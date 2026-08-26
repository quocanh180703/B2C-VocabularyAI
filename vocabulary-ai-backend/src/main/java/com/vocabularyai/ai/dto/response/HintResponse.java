package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for learning hint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HintResponse {
    
    private Long learningProgressId;
    private String hint;
    private Integer hintLevel; // 1=minimal, 2=moderate, 3=detailed
    private List<String> strategies;
    private String explanation;
}
