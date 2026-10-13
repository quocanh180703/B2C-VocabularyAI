package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Top Student Response DTO
 * 
 * Represents a single top-performing student for dashboard display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Top student information for dashboard leaderboard")
public class TopStudentResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "User ID", example = "42")
    private Long userId;
    
    @Schema(description = "Full name", example = "Nguyễn Văn A")
    private String fullName;
    
    @Schema(description = "Email address", example = "student@example.com")
    private String email;
    
    @Schema(description = "Total learning sessions completed", example = "150")
    private long totalSessions;
    
    @Schema(description = "Total words learned", example = "1250")
    private long wordsLearned;
    
    @Schema(description = "Learning accuracy rate (0-100)", example = "87.5")
    private double accuracyRate;
    
    @Schema(description = "Rank position (1, 2, 3, etc.)", example = "1")
    private int rank;
}
