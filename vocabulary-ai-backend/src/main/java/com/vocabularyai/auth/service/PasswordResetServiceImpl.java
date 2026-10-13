package com.vocabularyai.ai.service.auth;

import com.vocabularyai.ai.entity.PasswordResetToken;
import com.vocabularyai.ai.repository.PasswordResetTokenRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of PasswordResetService
 * 
 * Handles password reset token generation, validation, and lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    
    @Value("${app.password-reset.token-expiry-hours:1}")
    private int tokenExpiryHours;
    
    @Override
    public PasswordResetToken createResetToken(String email) {
        log.info("Creating reset token for email: {}", email);
        
        // Delete any existing unused tokens for this email
        passwordResetTokenRepository.deleteByEmail(email);
        
        // Generate unique token
        String resetToken = generateResetToken();
        
        // Set expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpiryHours);
        
        // Create token entity
        PasswordResetToken token = PasswordResetToken.builder()
            .email(email)
            .resetToken(resetToken)
            .expiresAt(expiresAt)
            .used(false)
            .build();
        
        // Save to database
        token = passwordResetTokenRepository.save(token);
        
        log.info("Reset token created successfully for email: {}", email);
        return token;
    }
    
    @Override
    public PasswordResetToken createResetToken(User user) {
        log.info("Creating reset token for user: {} (email: {})", user.getUsername(), user.getEmail());
        
        // Delete any existing unused tokens for this user
        passwordResetTokenRepository.deleteByEmail(user.getEmail());
        
        // Generate unique token
        String resetToken = generateResetToken();
        
        // Set expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpiryHours);
        
        // Create token entity
        PasswordResetToken token = PasswordResetToken.builder()
            .user(user)
            .email(user.getEmail())
            .resetToken(resetToken)
            .expiresAt(expiresAt)
            .used(false)
            .build();
        
        // Save to database
        token = passwordResetTokenRepository.save(token);
        
        log.info("Reset token created successfully for user: {}", user.getUsername());
        return token;
    }
    
    @Override
    public Optional<PasswordResetToken> getResetToken(String resetToken) {
        return passwordResetTokenRepository.findByResetToken(resetToken);
    }
    
    @Override
    public boolean isTokenValid(String resetToken) {
        Optional<PasswordResetToken> tokenOptional = getResetToken(resetToken);
        
        if (tokenOptional.isEmpty()) {
            log.warn("Reset token not found: {}", resetToken);
            return false;
        }
        
        PasswordResetToken token = tokenOptional.get();
        
        if (token.isExpired()) {
            log.warn("Reset token expired: {}", resetToken);
            return false;
        }
        
        if (token.getUsed()) {
            log.warn("Reset token already used: {}", resetToken);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void markTokenAsUsed(String resetToken) {
        log.info("Marking reset token as used: {}", resetToken);
        passwordResetTokenRepository.markTokenAsUsed(resetToken);
    }
    
    @Override
    public Optional<PasswordResetToken> getValidTokenByEmail(String email) {
        return passwordResetTokenRepository.findValidTokenByEmail(email, LocalDateTime.now());
    }
    
    @Override
    public boolean hasValidTokenForEmail(String email) {
        return passwordResetTokenRepository.existsValidTokenForEmail(email, LocalDateTime.now());
    }
    
    @Override
    public void deleteTokensByEmail(String email) {
        log.info("Deleting reset tokens for email: {}", email);
        passwordResetTokenRepository.deleteByEmail(email);
    }
    
    @Override
    public void cleanExpiredTokens() {
        log.info("Cleaning up expired password reset tokens");
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    /**
     * Generate a unique reset token using UUID
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}
