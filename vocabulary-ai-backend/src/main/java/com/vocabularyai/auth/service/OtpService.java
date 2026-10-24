package com.vocabularyai.auth.service;

import com.vocabularyai.auth.entity.OtpVerification;
import java.util.Optional;

/**
 * Service interface for OTP operations
 * 
 * Handles:
 * - OTP generation and saving
 * - OTP verification
 * - OTP expiry and cleanup
 * - Attempt tracking
 */
public interface OtpService {
    
    /**
     * Generate and save an OTP for a given email and type
     * 
     * Generates a 6-digit random OTP code, sets expiration to current time + 5 minutes,
     * and persists to database
     * 
     * @param email User email
     * @param otpType OTP type (REGISTER or FORGOT_PASSWORD)
     * @return Generated OtpVerification entity
     */
    OtpVerification generateAndSaveOtp(String email, OtpVerification.OtpType otpType);
    
    /**
     * Verify an OTP code against the stored OTP
     * 
     * Checks:
     * - OTP code matches
     * - OTP has not expired
     * - Max attempts not exceeded
     * 
     * Increments attempt counter on failed attempts
     * 
     * @param email User email
     * @param otpCode OTP code to verify
     * @param otpType OTP type
     * @return true if OTP is valid, false otherwise
     */
    boolean verifyOtp(String email, String otpCode, OtpVerification.OtpType otpType);
    
    /**
     * Get the most recent OTP for a given email and type
     * 
     * @param email User email
     * @param otpType OTP type
     * @return Optional containing the OTP if found
     */
    Optional<OtpVerification> getOtpByEmailAndType(String email, OtpVerification.OtpType otpType);
    
    /**
     * Get remaining attempts for an OTP
     * 
     * Returns the number of attempts remaining before max attempts is reached
     * 
     * @param email User email
     * @param otpType OTP type
     * @return Remaining attempts (0 if max attempts exceeded)
     */
    int getRemainingAttempts(String email, OtpVerification.OtpType otpType);
    
    /**
     * Invalidate (delete) an OTP for a given email and type
     * 
     * Called after successful OTP verification to ensure OTP cannot be reused
     * 
     * @param email User email
     * @param otpType OTP type
     */
    void invalidateOtp(String email, OtpVerification.OtpType otpType);
    
    /**
     * Check if an OTP exists and is not expired for a given email and type
     * 
     * @param email User email
     * @param otpType OTP type
     * @return true if active OTP exists
     */
    boolean hasActiveOtp(String email, OtpVerification.OtpType otpType);
    
    /**
     * Clean up expired OTPs from database
     * 
     * Should be called periodically to maintain database cleanliness
     * Can be triggered by a scheduled task
     */
    void cleanExpiredOtps();
}
