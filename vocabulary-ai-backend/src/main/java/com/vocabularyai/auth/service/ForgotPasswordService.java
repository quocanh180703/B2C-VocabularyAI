package com.vocabularyai.ai.service.auth;

import com.vocabularyai.ai.dto.request.SendOtpRequest;
import com.vocabularyai.ai.dto.request.VerifyOtpForgotPasswordRequest;
import com.vocabularyai.ai.dto.request.ResetPasswordRequest;
import com.vocabularyai.ai.dto.response.SendOtpResponse;
import com.vocabularyai.ai.dto.response.VerifyOtpForgotPasswordResponse;
import com.vocabularyai.auth.dto.AuthResponse;

/**
 * Service interface cho quy trình quên mật khẩu với OTP
 * 
 * Xử lý:
 * - Bước 1: Gửi OTP đến email
 * - Bước 2: Xác minh OTP
 * - Bước 3: Reset mật khẩu + TỰ ĐỘNG ĐĂNG NHẬP
 */
public interface ForgotPasswordService {
    
    /**
     * Bước 1: Gửi OTP quên mật khẩu
     * 
     * Kiểm tra:
     * - Email có tồn tại trong hệ thống không
     * - Người dùng có hoạt động không (status = ACTIVE)
     * 
     * Sau đó:
     * - Tạo OTP 6 chữ số
     * - Gửi OTP đến email
     * - Tạo reset token
     * - Trả về email, thời gian OTP hết hạn, reset token
     * 
     * @param request Email người dùng
     * @return Response với email, expiresIn, resetToken
     * @throws RuntimeException nếu email không tồn tại hoặc user không hoạt động
     */
    SendOtpResponse sendOtp(SendOtpRequest request);
    
    /**
     * Bước 2: Xác minh OTP quên mật khẩu
     * 
     * Kiểm tra:
     * - Reset token hợp lệ
     * - OTP chưa hết hạn
     * - OTP chúng được đúng
     * - Số lần thử < 5 lần
     * 
     * Sau đó:
     * - Tạo verification token (Base64)
     * - Xóa OTP (invalidate)
     * - Trả về verification token
     * 
     * @param request Email, OTP, resetToken
     * @return Response với verification token
     * @throws RuntimeException nếu OTP không hợp lệ hoặc reset token sai
     */
    VerifyOtpForgotPasswordResponse verifyOtp(VerifyOtpForgotPasswordRequest request);
    
    /**
     * Bước 3: Reset mật khẩu và TỰ ĐỘNG ĐĂNG NHẬP
     * 
     * Kiểm tra:
     * - Verification token hợp lệ
     * - Email và token khớp nhau
     * 
     * Sau đó:
     * - Cập nhật mật khẩu mới (mã hóa bằng BCrypt)
     * - Xóa reset token (đánh dấu đã sử dụng)
     * - Gửi email xác nhận reset thành công
     * - TỰ ĐỘNG TẠO JWT TOKENS (đăng nhập user)
     * - Trả về AuthResponse với access token, refresh token, user info
     * 
     * @param request Email, verification token, mật khẩu mới
     * @return AuthResponse với JWT tokens (user đã được đăng nhập)
     * @throws RuntimeException nếu verification token không hợp lệ
     */
    AuthResponse resetPassword(ResetPasswordRequest request);
}
