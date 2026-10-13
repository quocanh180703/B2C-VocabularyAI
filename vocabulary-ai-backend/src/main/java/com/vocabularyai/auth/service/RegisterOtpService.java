package com.vocabularyai.ai.service.auth;

import com.vocabularyai.ai.dto.request.ValidateRegisterRequest;
import com.vocabularyai.ai.dto.request.VerifyOtpRegisterRequest;
import com.vocabularyai.ai.dto.response.ValidateRegisterResponse;
import com.vocabularyai.ai.dto.response.RegisterSuccessResponse;

/**
 * Service interface cho quy trình đăng ký với OTP
 * 
 * Xử lý:
 * - Bước 1: Xác thực thông tin đăng ký + gửi OTP
 * - Bước 2: Xác minh OTP + tạo tài khoản
 */
public interface RegisterOtpService {
    
    /**
     * Bước 1: Xác thực thông tin đăng ký
     * 
     * Kiểm tra:
     * - Email chưa được đăng ký
     * - Username chưa được đăng ký
     * - Thông tin hợp lệ
     * 
     * Sau đó:
     * - Tạo OTP 6 chữ số
     * - Gửi OTP đến email
     * - Trả về temp token (email mã hóa)
     * 
     * @param request Thông tin đăng ký
     * @return Response với email, thời gian hết hạn OTP, temp token
     * @throws RuntimeException nếu email hoặc username đã tồn tại
     */
    ValidateRegisterResponse validateRegisterAndSendOtp(ValidateRegisterRequest request);
    
    /**
     * Bước 2: Xác minh OTP và tạo tài khoản
     * 
     * Kiểm tra:
     * - Temp token hợp lệ (khớp với email)
     * - OTP chưa hết hạn
     * - OTP chúng được đúng
     * - Số lần thử < 5 lần
     * 
     * Sau đó:
     * - Tạo User mới với role STUDENT, status ACTIVE
     * - Xóa OTP (invalidate)
     * - Gửi email xác nhận
     * 
     * @param request Thông tin OTP + temp token
     * @return Response với thông tin tài khoản (KHÔNG có JWT)
     * @throws RuntimeException nếu OTP không hợp lệ hoặc temp token sai
     */
    RegisterSuccessResponse verifyOtpAndCreateAccount(VerifyOtpRegisterRequest request);
}
