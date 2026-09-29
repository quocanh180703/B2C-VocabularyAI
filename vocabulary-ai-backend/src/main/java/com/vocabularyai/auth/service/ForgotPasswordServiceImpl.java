package com.vocabularyai.ai.service.auth;

import com.vocabularyai.ai.dto.request.SendOtpRequest;
import com.vocabularyai.ai.dto.request.VerifyOtpForgotPasswordRequest;
import com.vocabularyai.ai.dto.request.ResetPasswordRequest;
import com.vocabularyai.ai.dto.response.SendOtpResponse;
import com.vocabularyai.ai.dto.response.VerifyOtpForgotPasswordResponse;
import com.vocabularyai.ai.entity.OtpVerification;
import com.vocabularyai.ai.entity.PasswordResetToken;
import com.vocabularyai.auth.config.JwtProperties;
import com.vocabularyai.auth.dto.AuthResponse;
import com.vocabularyai.auth.entity.RefreshToken;
import com.vocabularyai.auth.repository.RefreshTokenRepository;
import com.vocabularyai.auth.security.JwtProvider;
import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.user.dto.UserResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Implementation của ForgotPasswordService
 * 
 * Xử lý quy trình quên mật khẩu 3 bước với OTP
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @Override
    public SendOtpResponse sendOtp(SendOtpRequest request) {
        log.info("Sending OTP for password reset to email: {}", request.getEmail());
        
        // Kiểm tra email có tồn tại không
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("User not found with email: {}", request.getEmail());
                return ResourceNotFoundException.of("User", "email", request.getEmail());
            });
        
        // Kiểm tra user có hoạt động không (status = ACTIVE)
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            log.warn("Cannot send OTP for inactive user: {}", user.getUsername());
            throw new BadRequestException("Tài khoản của bạn không hoạt động. Vui lòng liên hệ với hỗ trợ.");
        }
        
        log.info("User found and active. Generating OTP for email: {}", request.getEmail());
        
        // Tạo OTP 6 chữ số
        OtpVerification otp = otpService.generateAndSaveOtp(
            request.getEmail(),
            OtpVerification.OtpType.FORGOT_PASSWORD
        );
        
        // Tạo reset token
        PasswordResetToken resetToken = passwordResetService.createResetToken(user);
        
        // Gửi OTP đến email
        try {
            emailService.sendOtpEmail(request.getEmail(), otp.getOtpCode(), "FORGOT_PASSWORD");
            log.info("OTP email sent successfully to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", request.getEmail(), e);
            // Xóa OTP vì không gửi được email
            otpService.invalidateOtp(request.getEmail(), OtpVerification.OtpType.FORGOT_PASSWORD);
            throw new RuntimeException("Không thể gửi OTP đến email. Vui lòng thử lại.");
        }
        
        log.info("Password reset OTP sent successfully for email: {}", request.getEmail());
        
        return SendOtpResponse.builder()
            .message("Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra email trong 5 phút.")
            .email(request.getEmail())
            .expiresIn(otpExpiryMinutes * 60) // Chuyển đổi thành giây
            .resetToken(resetToken.getResetToken())
            .build();
    }
    
    @Override
    public VerifyOtpForgotPasswordResponse verifyOtp(VerifyOtpForgotPasswordRequest request) {
        log.info("Verifying OTP for password reset at email: {}", request.getEmail());
        
        // Kiểm tra reset token có tồn tại không
        PasswordResetToken resetToken = passwordResetService.getResetToken(request.getResetToken())
            .orElseThrow(() -> {
                log.warn("Invalid reset token");
                throw new BadRequestException("Reset token không hợp lệ. Vui lòng bắt đầu lại quy trình.");
            });
        
        // Kiểm tra reset token còn hiệu lực không (không hết hạn, không được dùng)
        if (!resetToken.isValid()) {
            log.warn("Reset token is expired or already used");
            throw new BadRequestException("Reset token đã hết hạn hoặc đã được sử dụng. Vui lòng bắt đầu lại.");
        }
        
        // Kiểm tra email khớp với token
        if (!resetToken.getEmail().equals(request.getEmail())) {
            log.warn("Email does not match reset token");
            throw new BadRequestException("Email không khớp với reset token.");
        }
        
        // Xác minh OTP
        boolean isOtpValid = otpService.verifyOtp(
            request.getEmail(),
            request.getOtp(),
            OtpVerification.OtpType.FORGOT_PASSWORD
        );
        
        if (!isOtpValid) {
            // Lấy số lần thử còn lại
            int remainingAttempts = otpService.getRemainingAttempts(
                request.getEmail(),
                OtpVerification.OtpType.FORGOT_PASSWORD
            );
            
            if (remainingAttempts <= 0) {
                log.warn("Max OTP attempts exceeded for email: {}", request.getEmail());
                otpService.invalidateOtp(request.getEmail(), OtpVerification.OtpType.FORGOT_PASSWORD);
                throw new BadRequestException("Số lần nhập mã OTP đã vượt quá. Vui lòng bắt đầu lại quy trình.");
            }
            
            log.warn("Invalid OTP for email: {}. Remaining attempts: {}", request.getEmail(), remainingAttempts);
            throw new BadRequestException("Mã OTP không hợp lệ. Vui lòng thử lại. Còn " + remainingAttempts + " lần thử.");
        }
        
        log.info("OTP verified successfully for email: {}", request.getEmail());
        
        // Tạo verification token (Base64)
        String verificationToken = Base64.getEncoder().encodeToString(
            (resetToken.getResetToken() + "|" + System.currentTimeMillis()).getBytes()
        );
        
        // Xóa OTP (invalidate) để không thể dùng lại
        otpService.invalidateOtp(request.getEmail(), OtpVerification.OtpType.FORGOT_PASSWORD);
        log.info("OTP invalidated for email: {}", request.getEmail());
        
        log.info("OTP verification completed successfully for email: {}", request.getEmail());
        
        return VerifyOtpForgotPasswordResponse.builder()
            .message("Mã OTP đã được xác minh thành công!")
            .verificationToken(verificationToken)
            .instruction("Vui lòng nhập mật khẩu mới để hoàn tất quá trình đặt lại mật khẩu.")
            .build();
    }
    
    @Override
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for email: {}", request.getEmail());
        
        // Kiểm tra email có tồn tại không
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("User not found with email: {}", request.getEmail());
                return ResourceNotFoundException.of("User", "email", request.getEmail());
            });
        
        // Giải mã verification token để lấy reset token
        String decodedToken;
        try {
            decodedToken = new String(Base64.getDecoder().decode(request.getVerificationToken()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid verification token format");
            throw new BadRequestException("Verification token không hợp lệ.");
        }
        
        // Extract reset token từ verification token (format: resetToken|timestamp)
        String resetTokenString = decodedToken.split("\\|")[0];
        
        // Kiểm tra reset token
        PasswordResetToken resetToken = passwordResetService.getResetToken(resetTokenString)
            .orElseThrow(() -> {
                log.warn("Reset token not found");
                throw new BadRequestException("Reset token không hợp lệ.");
            });
        
        // Kiểm tra reset token còn hiệu lực không
        if (!resetToken.isValid()) {
            log.warn("Reset token is expired or already used");
            throw new BadRequestException("Reset token đã hết hạn hoặc đã được sử dụng.");
        }
        
        // Kiểm tra email khớp
        if (!resetToken.getEmail().equals(request.getEmail())) {
            log.warn("Email does not match reset token");
            throw new BadRequestException("Email không khớp với reset token.");
        }
        
        log.info("Verification successful. Updating password for user: {}", user.getUsername());
        
        // Cập nhật mật khẩu mới (mã hóa bằng BCrypt)
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", user.getUsername());
        
        // Đánh dấu reset token đã sử dụng
        passwordResetService.markTokenAsUsed(resetTokenString);
        log.info("Reset token marked as used");
        
        // Gửi email xác nhận reset thành công
        try {
            emailService.sendPasswordResetConfirmationEmail(user.getEmail());
            log.info("Password reset confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send password reset confirmation email to: {}", user.getEmail(), e);
            // Không throw exception vì password đã được reset
        }
        
        // TỰ ĐỘNG ĐĂNG NHẬP: Tạo JWT tokens
        log.info("Generating JWT tokens for auto-login: {}", user.getUsername());
        return generateAuthResponse(user);
    }
    
    /**
     * Tạo AuthResponse với JWT tokens (auto-login)
     */
    private AuthResponse generateAuthResponse(User user) {
        // Tạo access token
        String accessToken = jwtProvider.generateAccessToken(
            user.getUsername(),
            user.getId(),
            user.getRole().name()
        );
        
        // Tạo refresh token
        String refreshTokenString = jwtProvider.generateRefreshToken(
            user.getUsername(),
            user.getId()
        );
        
        // Lưu refresh token vào database
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(refreshTokenString)
            .expiresAt(LocalDateTime.now().plusSeconds(
                jwtProperties.getRefreshToken().getExpiration() / 1000
            ))
            .revoked(false)
            .build();
        
        refreshTokenRepository.save(refreshToken);
        
        log.info("JWT tokens generated for user: {} (auto-login after password reset)", user.getUsername());
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenString)
            .tokenType("Bearer")
            .expiresIn(jwtProperties.getAccessToken().getExpiration() / 1000)
            .user(UserResponse.fromEntity(user))
            .build();
    }
}
