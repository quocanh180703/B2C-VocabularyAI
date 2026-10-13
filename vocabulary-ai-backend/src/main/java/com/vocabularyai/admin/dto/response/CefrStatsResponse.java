package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * CEFR Statistics Response
 * 
 * Word count distribution by CEFR level
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CefrStatsResponse implements Serializable {
    
    @Schema(description = "A1 level word count", example = "1000")
    private Long a1Count;
    
    @Schema(description = "A2 level word count", example = "800")
    private Long a2Count;
    
    @Schema(description = "B1 level word count", example = "1000")
    private Long b1Count;
    
    @Schema(description = "B2 level word count", example = "1000")
    private Long b2Count;
    
    @Schema(description = "C1 level word count", example = "1160")
    private Long c1Count;
    
    @Schema(description = "Total word count across all levels", example = "4960")
    private Long totalCount;
}
