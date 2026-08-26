package com.vocabularyai.user.dto;

import com.vocabularyai.common.validation.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update User Request DTO
 * 
 * Used when user updates their profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Size(
        min = 2,
        max = 150,
        message = ValidationConstants.FULL_NAME_SIZE
    )
    private String fullName;
    
    private String avatarUrl;
}
