package com.vocabularyai.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Google Login Request DTO
 * 
 * Chứa Google ID token (JWT do Google ký) được gửi từ Frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    @NotBlank(message = "ID token Google không được để trống")
    private String idToken;
}

