package com.vocabularyai.ai.service.auth;

import com.vocabularyai.ai.entity.OtpVerification;
import com.vocabularyai.ai.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation of OtpService
 * 
 * Handles OTP generation, verification, and lifecycle management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpServiceImpl implements OtpService {
    
    private final OtpVerificationRepository otpVerificationRepository;
    
    @Value("${app.otp.length:6}")
    private int otpLength;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;
    
    private static final Random random = new Random();
    
    @Override
    public OtpVerification generateAndSaveOtp(String email, OtpVerification.OtpType otpType) {
        log.info("Generating OTP for email: {} with type: {}", email, otpType);
        
        // Delete any existing OTP for this email and type
        otpVerificationRepository.deleteByEmailAndOtpType(email, otpType);
        
        // Generate OTP code
        String otpCode = generateOtpCode();
        
        // Set expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        
        // Create OTP entity
        OtpVerification otp = OtpVerification.builder()
            .email(email)
            .otpCode(otpCode)
            .otpType(otpType)
            .attempts(0)
            .maxAttempts(maxAttempts)
            .expiresAt(expiresAt)
            .build();
        
        // Save to database
        otp = otpVerificationRepository.save(otp);
        
        log.info("OTP generated successfully for email: {}", email);
        return otp;
    }
    
    @Override
    public boolean verifyOtp(String email, String otpCode, OtpVerification.OtpType otpType) {
        log.info("Verifying OTP for email: {} with type: {}", email, otpType);
        
        Optional<OtpVerification> otpOptional = otpVerificationRepository.findByEmailAndOtpCodeAndOtpType(
            email, otpCode, otpType
        );
        
        if (otpOptional.isEmpty()) {
            log.warn("OTP not found for email: {} with type: {}", email, otpType);
            recordFailedAttempt(email, otpType);
            return false;
        }
        
        OtpVerification otp = otpOptional.get();
        
        // Check if OTP is expired
        if (otp.isExpired()) {
            log.warn("OTP expired for email: {}", email);
            recordFailedAttempt(email, otpType);
            return false;
        }
        
        // Check if max attempts exceeded
        if (otp.isMaxAttemptsExceeded()) {
            log.warn("Max OTP attempts exceeded for email: {}", email);
            return false;
        }
        
        // OTP is valid
        log.info("OTP verified successfully for email: {}", email);
        return true;
    }
    
    @Override
    public Optional<OtpVerification> getOtpByEmailAndType(String email, OtpVerification.OtpType otpType) {
        return otpVerificationRepository.findFirstByEmailAndOtpTypeOrderByCreatedAtDesc(email, otpType);
    }
    
    @Override
    public int getRemainingAttempts(String email, OtpVerification.OtpType otpType) {
        Optional<OtpVerification> otpOptional = getOtpByEmailAndType(email, otpType);
        
        if (otpOptional.isEmpty()) {
            return maxAttempts;
        }
        
        OtpVerification otp = otpOptional.get();
        
        if (otp.isExpired() || otp.isMaxAttemptsExceeded()) {
            return 0;
        }
        
        return otp.getMaxAttempts() - otp.getAttempts();
    }
    
    @Override
    public void invalidateOtp(String email, OtpVerification.OtpType otpType) {
        log.info("Invalidating OTP for email: {} with type: {}", email, otpType);
        otpVerificationRepository.deleteByEmailAndOtpType(email, otpType);
    }
    
    @Override
    public boolean hasActiveOtp(String email, OtpVerification.OtpType otpType) {
        long activeCount = otpVerificationRepository.countActiveOtpsByEmailAndType(
            email, otpType, LocalDateTime.now()
        );
        return activeCount > 0;
    }
    
    @Override
    public void cleanExpiredOtps() {
        log.info("Cleaning up expired OTPs");
        otpVerificationRepository.deleteExpiredOtps(LocalDateTime.now());
    }
    
    /**
     * Generate a random OTP code of specified length
     */
    private String generateOtpCode() {
        StringBuilder otp = new StringBuilder();
        int maxNum = (int) Math.pow(10, otpLength);
        int randomNum = random.nextInt(maxNum);
        
        // Pad with zeros if necessary
        otp.append(String.format("%0" + otpLength + "d", randomNum));
        
        return otp.toString();
    }
    
    /**
     * Record a failed OTP verification attempt
     */
    private void recordFailedAttempt(String email, OtpVerification.OtpType otpType) {
        Optional<OtpVerification> otpOptional = getOtpByEmailAndType(email, otpType);
        
        if (otpOptional.isPresent()) {
            OtpVerification otp = otpOptional.get();
            otp.setAttempts(otp.getAttempts() + 1);
            otp.setUpdatedAt(LocalDateTime.now());
            otpVerificationRepository.save(otp);
            
            log.warn("Failed OTP attempt recorded for email: {}. Attempts: {}/{}", 
                email, otp.getAttempts(), otp.getMaxAttempts());
        }
    }
}
