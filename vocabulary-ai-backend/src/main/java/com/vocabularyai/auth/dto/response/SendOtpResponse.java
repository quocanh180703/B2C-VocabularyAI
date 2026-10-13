package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho bước 1 của quên mật khẩu: Gửi OTP
 * 
 * Response này chứa:
 * - Thông báo thành công
 * - Email được xác thực
 * - Thời gian OTP hết hạn (tính bằng giây)
 * - Reset token dùng để xác minh OTP ở bước tiếp theo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendOtpResponse {
    
    /**
     * Thông báo xác nhận
     */
    private String message;
    
    /**
     * Email được xác thực
     */
    private String email;
    
    /**
     * Thời gian OTP hết hạn (tính bằng giây)
     * Ví dụ: 300 = 5 phút
     */
    private Integer expiresIn;
    
    /**
     * Reset token dùng để xác minh OTP ở bước tiếp theo
     * Token này là UUID được tạo bởi PasswordResetService
     * Được sử dụng để ngăn chặn tấn công
     */
    private String resetToken;
}
