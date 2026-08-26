package com.vocabularyai.auth.dto;

import com.vocabularyai.common.validation.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 * 
 * Used for user login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = ValidationConstants.USERNAME_NOT_BLANK)
    private String username;
    
    @NotBlank(message = ValidationConstants.PASSWORD_NOT_BLANK)
    private String password;
}
