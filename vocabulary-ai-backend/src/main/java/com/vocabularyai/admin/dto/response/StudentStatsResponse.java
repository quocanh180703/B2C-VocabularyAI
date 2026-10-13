package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Student Statistics Summary Response DTO
 * 
 * Provides aggregate statistics about all students
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Aggregate statistics for all students")
public class StudentStatsResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "Total number of students", example = "1250")
    private long totalStudents;
    
    @Schema(description = "Number of active students (with subscriptions)", example = "450")
    private long activeStudents;
    
    @Schema(description = "Number of inactive students", example = "800")
    private long inactiveStudents;
    
    @Schema(description = "Number of banned students", example = "10")
    private long bannedStudents;
    
    @Schema(description = "Average sessions per student", example = "45.7")
    private double avgSessionsPerStudent;
    
    @Schema(description = "Average words learned per student", example = "380")
    private long avgWordsPerStudent;
    
    @Schema(description = "Average accuracy rate across students", example = "75.3")
    private double avgAccuracyRate;
    
    @Schema(description = "Students with active subscriptions today", example = "450")
    private long activeSubscriptionsToday;
    
    @Schema(description = "Students who were active today", example = "280")
    private long activeTodayCount;
    
    @Schema(description = "New students registered this month", example = "85")
    private long newStudentsThisMonth;
    
    @Schema(description = "Churn rate (percentage)", example = "12.5")
    private double churnRate;
}
