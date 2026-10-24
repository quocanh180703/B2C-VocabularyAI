package com.vocabularyai.auth.repository;

import com.vocabularyai.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for PasswordResetToken entity
 * 
 * Handles database operations for password reset tokens
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find a password reset token by token string
     * 
     * @param resetToken The reset token
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByResetToken(String resetToken);

    /**
     * Find the most recent valid (not used, not expired) token for an email
     * 
     * @param email           User email
     * @param currentDateTime Current time for expiration check
     * @return Optional containing the valid token if found
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.used = false AND t.expiresAt > :currentDateTime ORDER BY t.createdAt DESC LIMIT 1")
    Optional<PasswordResetToken> findValidTokenByEmail(
            @Param("email") String email,
            @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Check if an unused, non-expired token exists for an email
     * 
     * @param email           User email
     * @param currentDateTime Current time for expiration check
     * @return true if a valid token exists
     */
    @Query("SELECT COUNT(t) > 0 FROM PasswordResetToken t WHERE t.email = :email AND t.used = false AND t.expiresAt > :currentDateTime")
    boolean existsValidTokenForEmail(
            @Param("email") String email,
            @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Mark a token as used
     * 
     * @param resetToken The reset token to mark
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken SET used = true WHERE resetToken = :resetToken")
    void markTokenAsUsed(@Param("resetToken") String resetToken);

    /**
     * Delete expired tokens
     * 
     * Cleans up old password reset tokens that have passed their expiration time
     * 
     * @param currentDateTime Current time to compare against
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken WHERE expiresAt < :currentDateTime")
    void deleteExpiredTokens(@Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Delete all tokens for a given email
     * 
     * @param email User email
     */
    @Modifying
    @Transactional
    void deleteByEmail(String email);

    /**
     * Count active (unused, non-expired) reset tokens for an email
     * 
     * @param email           User email
     * @param currentDateTime Current time for comparison
     * @return Count of active tokens
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.email = :email AND t.used = false AND t.expiresAt > :currentDateTime")
    long countActiveTokensByEmail(
            @Param("email") String email,
            @Param("currentDateTime") LocalDateTime currentDateTime);
}
