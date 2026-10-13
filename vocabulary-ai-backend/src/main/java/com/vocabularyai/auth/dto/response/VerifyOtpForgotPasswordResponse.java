package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho bước 2 của quên mật khẩu: Xác minh OTP
 * 
 * Response này chứa:
 * - Thông báo thành công
 * - Verification token dùng cho bước reset mật khẩu
 * - Hướng dẫn tiếp theo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpForgotPasswordResponse {
    
    /**
     * Thông báo xác nhận OTP hợp lệ
     */
    private String message;
    
    /**
     * Verification token dùng cho bước reset mật khẩu
     * Token này là một chuỗi mã hóa Base64 từ reset token + timestamp
     * Được sử dụng để ngăn chặn tấn công
     */
    private String verificationToken;
    
    /**
     * Hướng dẫn tiếp theo
     */
    private String instruction;
}
