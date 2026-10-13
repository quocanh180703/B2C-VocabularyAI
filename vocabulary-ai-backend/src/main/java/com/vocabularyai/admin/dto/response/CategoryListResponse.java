package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Admin Category List Response
 * 
 * Category information with word count
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryListResponse implements Serializable {
    
    @Schema(description = "Category ID", example = "1")
    private Long id;
    
    @Schema(description = "Category name", example = "Business English")
    private String name;
    
    @Schema(description = "Category description", example = "Words and phrases for business contexts")
    private String description;
    
    @Schema(description = "Number of words in this category", example = "250")
    private Long wordCount;
    
    @Schema(description = "Created date (ISO 8601)", example = "2024-01-15T10:30:00")
    private String createdAt;
}
