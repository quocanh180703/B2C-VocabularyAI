package com.vocabularyai.auth.repository;

import com.vocabularyai.auth.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for OtpVerification entity
 * 
 * Handles database operations for OTP codes
 */
@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    /**
     * Find the most recent OTP for a given email and type
     * 
     * @param email   User email
     * @param otpType OTP type (REGISTER or FORGOT_PASSWORD)
     * @return Optional containing the OTP if found
     */
    Optional<OtpVerification> findFirstByEmailAndOtpTypeOrderByCreatedAtDesc(
            String email,
            OtpVerification.OtpType otpType);

    /**
     * Find OTP by email, OTP code, and type
     * 
     * Used during OTP verification
     * 
     * @param email   User email
     * @param otpCode The OTP code to verify
     * @param otpType OTP type
     * @return Optional containing the OTP if found
     */
    Optional<OtpVerification> findByEmailAndOtpCodeAndOtpType(
            String email,
            String otpCode,
            OtpVerification.OtpType otpType);

    /**
     * Check if an OTP exists for email and type
     * 
     * @param email   User email
     * @param otpType OTP type
     * @return true if OTP exists
     */
    boolean existsByEmailAndOtpType(String email, OtpVerification.OtpType otpType);

    /**
     * Delete expired OTPs
     * 
     * Cleans up old OTP records that have passed their expiration time
     * 
     * @param currentDateTime Current time to compare against
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification WHERE expiresAt < :currentDateTime")
    void deleteExpiredOtps(@Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Delete OTP for a given email and type
     * 
     * Used after successful OTP verification to invalidate the OTP
     * 
     * @param email   User email
     * @param otpType OTP type
     */
    @Modifying
    @Transactional
    void deleteByEmailAndOtpType(String email, OtpVerification.OtpType otpType);

    /**
     * Count OTPs for a given email and type that are not expired
     * 
     * @param email           User email
     * @param otpType         OTP type
     * @param currentDateTime Current time for comparison
     * @return Count of active OTPs
     */
    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.email = :email AND o.otpType = :otpType AND o.expiresAt > :currentDateTime")
    long countActiveOtpsByEmailAndType(
            @Param("email") String email,
            @Param("otpType") OtpVerification.OtpType otpType,
            @Param("currentDateTime") LocalDateTime currentDateTime);
}
