package com.vocabularyai.auth.controller;

import com.vocabularyai.auth.service.AuthService;
import com.vocabularyai.auth.service.RegisterOtpService;
import com.vocabularyai.auth.service.ForgotPasswordService;
import com.vocabularyai.auth.dto.request.ValidateRegisterRequest;
import com.vocabularyai.auth.dto.request.VerifyOtpRegisterRequest;
import com.vocabularyai.auth.dto.request.SendOtpRequest;
import com.vocabularyai.auth.dto.request.VerifyOtpForgotPasswordRequest;
import com.vocabularyai.auth.dto.request.LoginRequest;
import com.vocabularyai.auth.dto.request.RefreshTokenRequest;
import com.vocabularyai.auth.dto.request.ResetPasswordRequest;
import com.vocabularyai.auth.dto.response.ValidateRegisterResponse;
import com.vocabularyai.auth.dto.response.AuthResponse;
import com.vocabularyai.auth.dto.response.RegisterSuccessResponse;
import com.vocabularyai.auth.dto.response.SendOtpResponse;
import com.vocabularyai.auth.dto.response.VerifyOtpForgotPasswordResponse;
import com.vocabularyai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * API endpoints for authentication operations:
 * - Register
 * - Login
 * - Refresh Token
 * - Logout
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final RegisterOtpService registerOtpService;
    private final ForgotPasswordService forgotPasswordService;

    /**
     * Step 1: Validate Registration Information & Send OTP
     * 
     * Kiểm tra email/username có hợp lệ không, gửi OTP
     */
    @PostMapping("/register/validate")
    @Operation(summary = "Validate registration and send OTP", description = "Step 1 of OTP registration: Validate email/username and send OTP code")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or user already exists")
    })
    public ResponseEntity<ApiResponse<ValidateRegisterResponse>> validateRegister(
            @Valid @RequestBody ValidateRegisterRequest request) {
        log.info("Validate registration request for email: {}", request.getEmail());

        ValidateRegisterResponse response = registerOtpService.validateRegisterAndSendOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("Mã OTP đã được gửi đến email", response));
    }

    /**
     * Step 2: Verify OTP & Create Account
     * 
     * Xác minh OTP, tạo tài khoản
     * KHÔNG tự động đăng nhập - người dùng phải quay lại Login
     */
    @PostMapping("/register/verify-otp")
    @Operation(summary = "Verify OTP and create account", description = "Step 2 of OTP registration: Verify OTP code and create user account (NO auto-login)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid OTP or request")
    })
    public ResponseEntity<ApiResponse<RegisterSuccessResponse>> verifyOtpRegister(
            @Valid @RequestBody VerifyOtpRegisterRequest request) {
        log.info("Verify OTP request for email: {}", request.getEmail());

        RegisterSuccessResponse response = registerOtpService.verifyOtpAndCreateAccount(request);

        return ResponseEntity.ok(
                ApiResponse.success("Đăng ký tài khoản thành công! Vui lòng đăng nhập.", response));
    }

    /**
     * Step 1: Send OTP for Forgot Password
     * 
     * Gửi OTP đến email cho quên mật khẩu
     */
    @PostMapping("/forgot-password/send-otp")
    @Operation(summary = "Send OTP for password reset", description = "Step 1 of forgot password: Send OTP code to user email")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtpForgotPassword(
            @Valid @RequestBody SendOtpRequest request) {
        log.info("Send OTP request for password reset to email: {}", request.getEmail());

        SendOtpResponse response = forgotPasswordService.sendOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("Mã OTP đã được gửi đến email", response));
    }

    /**
     * Step 2: Verify OTP for Forgot Password
     * 
     * Xác minh OTP quên mật khẩu
     */
    @PostMapping("/forgot-password/verify-otp")
    @Operation(summary = "Verify OTP for password reset", description = "Step 2 of forgot password: Verify OTP code")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid OTP or reset token")
    })
    public ResponseEntity<ApiResponse<VerifyOtpForgotPasswordResponse>> verifyOtpForgotPassword(
            @Valid @RequestBody VerifyOtpForgotPasswordRequest request) {
        log.info("Verify OTP request for password reset at email: {}", request.getEmail());

        VerifyOtpForgotPasswordResponse response = forgotPasswordService.verifyOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("Mã OTP đã được xác minh thành công!", response));
    }

    /**
     * Step 3: Reset Password (with auto-login)
     * 
     * Reset mật khẩu và TỰ ĐỘNG ĐĂNG NHẬP
     */
    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Reset password and auto-login", description = "Step 3 of forgot password: Reset password and get JWT tokens (auto-login)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successful, JWT tokens returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid verification token or request")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password request for email: {}", request.getEmail());

        AuthResponse response = forgotPasswordService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Mật khẩu đã được đặt lại thành công! Bạn đã tự động đăng nhập.", response));
    }

    /**
     * User Login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT tokens")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request for emailOrUsername: {}", request.getEmailOrUsername());

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Refresh Access Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get a new access token using refresh token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Access token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshAccessToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request");

        AuthResponse authResponse = authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success("Access token refreshed successfully", authResponse));
    }

    /**
     * User Logout
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Revoke refresh token and logout user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    })
    public ResponseEntity<ApiResponse<?>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request");

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success("Logout successful"));
    }
}
