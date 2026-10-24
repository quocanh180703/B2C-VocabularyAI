package com.vocabularyai.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho bước 2 của đăng ký: Xác minh OTP và tạo tài khoản
 * 
 * Bước này nhận OTP từ email người dùng,
 * xác minh nó, và tạo tài khoản nếu OTP hợp lệ.
 * 
 * Input: Chỉ cần email và OTP
 * (Thông tin người dùng đã được lưu từ Step 1)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpRegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống")
    @Size(min = 6, max = 6, message = "Mã OTP phải đúng 6 chữ số")
    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP phải là 6 chữ số")
    private String otp;
}
