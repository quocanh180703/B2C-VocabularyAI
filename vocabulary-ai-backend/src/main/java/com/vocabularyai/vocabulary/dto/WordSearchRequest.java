package com.vocabularyai.vocabulary.dto;

import com.vocabularyai.vocabulary.entity.CEFRLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WordSearchRequest DTO
 * 
 * Request parameters for word search and filtering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Word search and filter parameters")
public class WordSearchRequest {

    @Schema(description = "Search query (word or definition)", example = "hello")
    private String query;

    @Schema(description = "CEFR level filter (A1, A2, B1, B2, C1)", example = "B1")
    private CEFRLevel level;

    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "Page must be >= 0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size (items per page)", example = "20", defaultValue = "20")
    @Min(value = 1, message = "Size must be >= 1")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "Sort by field", example = "lemma", defaultValue = "lemma")
    @Builder.Default
    private String sortBy = "lemma";

    @Schema(description = "Sort order (asc/desc)", example = "asc", defaultValue = "asc")
    @Builder.Default
    private String sortOrder = "asc";
}
