package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * System Overview Response DTO
 * 
 * Represents overall system statistics for dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOverviewResponse {
    
    private Long totalUsers;
    
    private Long activeUsersToday;
    
    private Long totalSessions;
    
    private Long totalQuizzes;
    
    private Long totalVocabularies;
    
    private Double avgUserEngagement;
}
