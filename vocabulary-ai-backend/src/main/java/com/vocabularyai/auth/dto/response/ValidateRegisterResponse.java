package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho bước 1 của đăng ký: Xác thực thông tin
 * 
 * Response này chứa:
 * - Thông báo thành công
 * - Email được xác thực
 * - Thời gian OTP hết hạn (tính bằng giây)
 * 
 * KHÔNG chứa JWT token vì người dùng chưa xác thực OTP
 * Thông tin người dùng được lưu tạm vào pending_registrations table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateRegisterResponse {
    
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
}

