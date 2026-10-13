package com.vocabularyai.ai.service.auth;

import com.vocabularyai.ai.dto.PaymentEmailData;

/**
 * Service interface for email operations
 * 
 * Handles sending emails for:
 * - OTP verification
 * - Password reset confirmation
 * - Payment success notification
 */
public interface EmailService {
    
    /**
     * Send OTP email to user
     * 
     * Includes the OTP code and instructions for verification
     * 
     * @param email Recipient email address
     * @param otpCode The 6-digit OTP code
     * @param otpType Type of OTP (REGISTER or FORGOT_PASSWORD) for context in email
     * @throws RuntimeException if email sending fails
     */
    void sendOtpEmail(String email, String otpCode, String otpType);
    
    /**
     * Send password reset confirmation email
     * 
     * Sent after successful password reset
     * 
     * @param email Recipient email address
     * @throws RuntimeException if email sending fails
     */
    void sendPasswordResetConfirmationEmail(String email);
    
    /**
     * Send registration success email
     * 
     * Sent after successful account creation
     * 
     * @param email Recipient email address
     * @param username Username created
     * @param fullName User's full name
     * @throws RuntimeException if email sending fails
     */
    void sendRegistrationSuccessEmail(String email, String username, String fullName);
    
    /**
     * Send payment success email
     * 
     * Sent after successful payment via PayOS webhook
     * Uses Thymeleaf template rendering for HTML email
     * 
     * @param paymentData Payment email data containing customer and order information
     * @throws RuntimeException if email sending fails
     */
    void sendPaymentSuccessEmail(PaymentEmailData paymentData);
}
