package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * System Health Response DTO
 * 
 * Represents system health and performance metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthResponse {
    
    private Long totalUsers;
    
    private Long apiCallsToday;
    
    private Long averageResponseTime;
    
    private Double errorRate;
    
    private String status;
}
