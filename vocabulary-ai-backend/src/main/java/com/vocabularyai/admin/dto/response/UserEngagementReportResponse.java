package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Engagement Report Response DTO
 * 
 * Represents daily active user data for engagement tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEngagementReportResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
    
    private Long activeUsers;
}
