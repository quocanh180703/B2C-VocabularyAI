package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho bước 1 của quên mật khẩu: Gửi OTP
 * 
 * Bước này nhận email từ người dùng,
 * kiểm tra email có tồn tại không,
 * sau đó gửi OTP đến email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendOtpRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
}
