package com.vocabularyai.auth.controller;

import com.vocabularyai.ai.entity.OtpVerification;
import com.vocabularyai.ai.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * DEBUG CONTROLLER - ONLY FOR DEVELOPMENT/TESTING
 * 
 * Provides helper endpoints for E2E testing without access to email/SMS
 * 
 * WARNING: Remove this controller in production!
 */
@RestController
@RequestMapping("/api/auth/debug")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.debug.enabled", havingValue = "true", matchIfMissing = true)
public class DebugAuthController {
    
    private final OtpVerificationRepository otpVerificationRepository;
    
    /**
     * Get latest OTP for testing
     * 
     * Usage: GET /api/auth/debug/get-latest-otp?email=test@example.com&type=REGISTER
     */
    @GetMapping("/get-latest-otp")
    public ResponseEntity<Map<String, Object>> getLatestOTP(
            @RequestParam String email,
            @RequestParam(defaultValue = "REGISTER") String type) {
        
        log.info("Debug: Getting latest OTP for email: {}, type: {}", email, type);
        
        try {
            OtpVerification.OtpType otpType = OtpVerification.OtpType.valueOf(type.toUpperCase());
            
            Optional<OtpVerification> otp = otpVerificationRepository
                    .findFirstByEmailAndOtpTypeOrderByCreatedAtDesc(email, otpType);
            
            Map<String, Object> response = new HashMap<>();
            
            if (otp.isPresent()) {
                OtpVerification otpData = otp.get();
                response.put("success", true);
                response.put("email", otpData.getEmail());
                response.put("otpCode", otpData.getOtpCode());
                response.put("otpType", otpData.getOtpType());
                response.put("expiresAt", otpData.getExpiresAt());
                response.put("isExpired", otpData.isExpired());
                response.put("attempts", otpData.getAttempts());
                response.put("maxAttempts", otpData.getMaxAttempts());
                
                log.info("OTP found: {}", otpData.getOtpCode());
            } else {
                response.put("success", false);
                response.put("message", "No OTP found for email: " + email);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid OTP type: " + type);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "DEBUG_ENDPOINT_ACTIVE");
        response.put("warning", "Remove this endpoint in production!");
        return ResponseEntity.ok(response);
    }
}
