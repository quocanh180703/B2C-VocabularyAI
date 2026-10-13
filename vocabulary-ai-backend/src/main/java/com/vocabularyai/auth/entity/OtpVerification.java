package com.vocabularyai.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OtpVerification Entity
 * 
 * Maps to 'otp_verifications' table in MySQL
 * Represents OTP codes sent to users for registration and password reset
 */
@Entity
@Table(name = "otp_verifications", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_email_type", columnList = "email, otp_type"),
    @Index(name = "idx_otp_expires_at", columnList = "expires_at"),
    @Index(name = "idx_otp_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
@ToString
public class OtpVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String email;
    
    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;
    
    @Column(name = "otp_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpType otpType;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;
    
    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 5;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * OTP Type Enum
     */
    public enum OtpType {
        REGISTER,
        FORGOT_PASSWORD
    }
    
    /**
     * Check if OTP has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if max attempts exceeded
     */
    public boolean isMaxAttemptsExceeded() {
        return attempts >= maxAttempts;
    }
    
    /**
     * Check if OTP is valid (not expired and not max attempts exceeded)
     */
    public boolean isValid() {
        return !isExpired() && !isMaxAttemptsExceeded();
    }
}
