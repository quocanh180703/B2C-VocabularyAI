package com.vocabularyai.vocabulary.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Category Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    
    @Size(
        min = 2,
        max = 100,
        message = "Category name must be between 2 and 100 characters"
    )
    private String name;
    
    private String description;
}
