package com.vocabularyai.user.dto;

import com.vocabularyai.common.validation.ValidationConstants;
import com.vocabularyai.common.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Change Password Request DTO
 * 
 * Used when user changes their password
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    
    @NotBlank(message = ValidationConstants.PASSWORD_NOT_BLANK)
    private String oldPassword;
    
    @NotBlank(message = ValidationConstants.PASSWORD_NOT_BLANK)
    @ValidPassword
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
