package com.vocabularyai.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho bước 2 của đăng ký: Xác minh OTP thành công
 * 
 * Response này chứa thông tin tài khoản được tạo:
 * - Thông báo thành công
 * - Email
 * - Username
 * - Tên đầy đủ
 * - Trạng thái (luôn là "ACTIVE")
 * 
 * KHÔNG chứa JWT token vì:
 * - Người dùng KHÔNG tự động đăng nhập sau đăng ký
 * - Người dùng PHẢI quay lại trang Login để đăng nhập thủ công
 * - Điều này tăng tính bảo mật và kiểm soát
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterSuccessResponse {

    /**
     * Thông báo xác nhận đăng ký thành công
     */
    private String message;

    /**
     * Email của tài khoản vừa tạo
     */
    private String email;

    /**
     * Username của tài khoản vừa tạo
     */
    private String username;

    /**
     * Tên đầy đủ của tài khoản vừa tạo
     */
    private String fullName;

    /**
     * Trạng thái tài khoản (luôn là "ACTIVE")
     */
    private String status;

    /**
     * Thông báo chỉ dẫn người dùng
     */
    private String instruction;
}
