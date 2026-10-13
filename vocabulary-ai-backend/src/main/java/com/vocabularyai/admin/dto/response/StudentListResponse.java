package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Student List Item Response DTO
 * 
 * Used for paginated list of students (lighter than full detail)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Student information for list view")
public class StudentListResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "User ID", example = "42")
    private Long userId;
    
    @Schema(description = "Username", example = "user123")
    private String username;
    
    @Schema(description = "Email address", example = "student@example.com")
    private String email;
    
    @Schema(description = "Full name", example = "Nguyễn Văn A")
    private String fullName;
    
    @Schema(description = "User status (ACTIVE, INACTIVE, BANNED)", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Has active subscription", example = "true")
    private boolean hasActiveSubscription;
    
    @Schema(description = "Total learning sessions", example = "150")
    private long totalSessions;
    
    @Schema(description = "Account creation date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last login date", example = "2024-01-20T14:22:00")
    private LocalDateTime lastLoginAt;
}
