package com.vocabularyai.vocabulary.dto;

import com.vocabularyai.common.validation.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Category Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    
    @NotBlank(message = ValidationConstants.CATEGORY_NAME_NOT_BLANK)
    @Size(
        min = 2,
        max = 100,
        message = ValidationConstants.CATEGORY_NAME_SIZE
    )
    private String name;
    
    private String description;
}
