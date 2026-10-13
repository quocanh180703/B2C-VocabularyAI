package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Speaking Statistics Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeakingStatsResponse {
    
    private Long totalSessions;
    private Long completedSessions;
    private Long inProgressSessions;
    private Long cancelledSessions;
    
    // By level
    private Long sessionsA1;
    private Long sessionsA2;
    private Long sessionsB1;
    private Long sessionsB2;
    private Long sessionsC1;
    private Long sessionsC2;
    
    // Performance
    private Double avgScore;
    private Double avgAttemptsPerSession;
    private Long activeUsers;        // Users who did speaking
    private Double avgSessionDuration; // in minutes
    
    // Recent activity
    private Long sessionsLast7Days;
    private Long sessionsLast30Days;
}
