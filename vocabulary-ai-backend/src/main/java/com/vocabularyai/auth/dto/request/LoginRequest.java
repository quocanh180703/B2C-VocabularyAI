package com.vocabularyai.auth.dto.request;

import com.vocabularyai.common.validation.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 * 
 * Hỗ trợ login bằng email HOẶC username
 * Trường emailOrUsername chấp nhận cả email và username
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email hoặc username không được để trống")
    private String emailOrUsername;

    @NotBlank(message = ValidationConstants.PASSWORD_NOT_BLANK)
    private String password;
}
