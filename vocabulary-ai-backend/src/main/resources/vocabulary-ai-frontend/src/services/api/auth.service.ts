/**
 * Auth Service
 * Xử lý tất cả authentication-related API calls
 * 
 * IMPORTANT: Response DTOs now match actual backend format
 * - access_token (not token)
 * - refresh_token (not refreshToken)
 * - user object structure
 * - Register: 2-step OTP flow
 * - Forgot Password: 3-step OTP flow with auto-login
 */

import { apiClient } from "./client";
import { AUTH_ENDPOINTS } from "./endpoints";
import {
  LoginRequest,
  AuthResponse,
  ValidateRegisterRequest,
  ValidateRegisterResponse,
  VerifyOtpRegisterRequest,
  RegisterSuccessResponse,
  SendOtpRequest,
  SendOtpResponse,
  VerifyOtpForgotPasswordRequest,
  VerifyOtpForgotPasswordResponse,
  ResetPasswordRequest,
  RefreshTokenRequest,
  ApiResponse,
} from "./types";

export class AuthService {
  /**
   * Login with email or username
   */
  static async login(request: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>(
      AUTH_ENDPOINTS.LOGIN,
      request
    );
    return response.data.data!;
  }

  /**
   * Register Step 1: Validate registration info and send OTP
   */
  static async validateRegister(
    request: ValidateRegisterRequest
  ): Promise<ValidateRegisterResponse> {
    const response = await apiClient.post<ValidateRegisterResponse>(
      AUTH_ENDPOINTS.REGISTER_VALIDATE,
      request
    );
    return response.data.data!;
  }

  /**
   * Register Step 2: Verify OTP and create account
   * Note: Does NOT return JWT - user must login manually
   */
  static async verifyOtpRegister(
    request: VerifyOtpRegisterRequest
  ): Promise<RegisterSuccessResponse> {
    const response = await apiClient.post<RegisterSuccessResponse>(
      AUTH_ENDPOINTS.REGISTER_VERIFY_OTP,
      request
    );
    return response.data.data!;
  }

  /**
   * Refresh access token using refresh token
   */
  static async refreshToken(
    request: RefreshTokenRequest
  ): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>(
      AUTH_ENDPOINTS.REFRESH,
      request
    );
    return response.data.data!;
  }

  /**
   * Logout (revoke refresh token)
   */
  static async logout(refreshToken: string): Promise<void> {
    await apiClient.post(AUTH_ENDPOINTS.LOGOUT, { refreshToken });
  }

  /**
   * Forgot Password Step 1: Send OTP to email
   */
  static async sendOtpForgotPassword(
    request: SendOtpRequest
  ): Promise<SendOtpResponse> {
    const response = await apiClient.post<SendOtpResponse>(
      AUTH_ENDPOINTS.FORGOT_PASSWORD_SEND_OTP,
      request
    );
    return response.data.data!;
  }

  /**
   * Forgot Password Step 2: Verify OTP and get reset token
   */
  static async verifyOtpForgotPassword(
    request: VerifyOtpForgotPasswordRequest
  ): Promise<VerifyOtpForgotPasswordResponse> {
    const response = await apiClient.post<VerifyOtpForgotPasswordResponse>(
      AUTH_ENDPOINTS.FORGOT_PASSWORD_VERIFY_OTP,
      request
    );
    return response.data.data!;
  }

  /**
   * Forgot Password Step 3: Reset password and auto-login
   * Note: Returns JWT tokens (auto-login)
   */
  static async resetPassword(request: ResetPasswordRequest): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>(
      AUTH_ENDPOINTS.FORGOT_PASSWORD_RESET,
      request
    );
    return response.data.data!;
  }
}

export default AuthService;
