package com.vocabularyai.speaking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MilestonesResponse - Danh sách achievements/milestones của user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User's speaking milestones and achievements")
public class MilestonesResponse {
    
    @Schema(description = "List of achieved milestones", example = "[\"First session completed\", \"5 sessions completed\"]")
    private List<String> milestones;
    
    @Schema(description = "Total number of milestones achieved", example = "12")
    private Long totalMilestones;
    
    @Schema(description = "Timestamp when data was retrieved")
    private LocalDateTime retrievedAt;
}
