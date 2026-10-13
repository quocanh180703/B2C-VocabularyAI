package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho bước 2 của quên mật khẩu: Xác minh OTP
 * 
 * Bước này nhận email, OTP, và reset token từ người dùng,
 * xác minh OTP, và tạo verification token để reset mật khẩu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpForgotPasswordRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Mã OTP không được để trống")
    @Size(min = 6, max = 6, message = "Mã OTP phải đúng 6 chữ số")
    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP phải là 6 chữ số")
    private String otp;
    
    @NotBlank(message = "Reset token không được để trống")
    private String resetToken;
}
