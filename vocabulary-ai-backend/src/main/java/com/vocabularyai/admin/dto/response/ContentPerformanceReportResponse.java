package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Content Performance Report Response DTO
 * 
 * Represents content (quiz) performance metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPerformanceReportResponse {
    
    private Long quizId;
    
    private String quizTitle;
    
    private String level;
    
    private Integer attemptCount;
    
    private Double avgScore;
    
    private Integer completionRate;
}
