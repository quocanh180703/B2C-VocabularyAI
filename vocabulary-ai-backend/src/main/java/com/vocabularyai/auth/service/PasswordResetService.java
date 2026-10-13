package com.vocabularyai.ai.service.auth;

import com.vocabularyai.ai.entity.PasswordResetToken;
import com.vocabularyai.user.entity.User;
import java.util.Optional;

/**
 * Service interface for password reset operations
 * 
 * Handles:
 * - Creating reset tokens
 * - Validating reset tokens
 * - Marking tokens as used
 * - Token expiry and cleanup
 */
public interface PasswordResetService {
    
    /**
     * Create a password reset token for a given email
     * 
     * Generates a unique token and sets expiration to 1 hour from now
     * 
     * @param email User email
     * @return Created PasswordResetToken entity
     */
    PasswordResetToken createResetToken(String email);
    
    /**
     * Create a password reset token for a given user
     * 
     * @param user User entity
     * @return Created PasswordResetToken entity
     */
    PasswordResetToken createResetToken(User user);
    
    /**
     * Get a password reset token by token string
     * 
     * @param resetToken The reset token string
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> getResetToken(String resetToken);
    
    /**
     * Check if a reset token is valid (not expired and not used)
     * 
     * @param resetToken The reset token string
     * @return true if token is valid
     */
    boolean isTokenValid(String resetToken);
    
    /**
     * Mark a reset token as used
     * 
     * Called after password is successfully reset
     * 
     * @param resetToken The reset token string
     */
    void markTokenAsUsed(String resetToken);
    
    /**
     * Get the most recent valid reset token for an email
     * 
     * @param email User email
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> getValidTokenByEmail(String email);
    
    /**
     * Check if an unused, non-expired token exists for an email
     * 
     * @param email User email
     * @return true if a valid token exists
     */
    boolean hasValidTokenForEmail(String email);
    
    /**
     * Delete all tokens for a given email
     * 
     * @param email User email
     */
    void deleteTokensByEmail(String email);
    
    /**
     * Clean up expired tokens from database
     * 
     * Should be called periodically to maintain database cleanliness
     * Can be triggered by a scheduled task
     */
    void cleanExpiredTokens();
}
