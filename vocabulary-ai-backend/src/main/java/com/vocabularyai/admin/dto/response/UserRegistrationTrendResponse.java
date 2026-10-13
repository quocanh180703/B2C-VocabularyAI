package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Registration Trend Response DTO
 * 
 * Represents user registration trend data for analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationTrendResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
    
    private Long count;
}
