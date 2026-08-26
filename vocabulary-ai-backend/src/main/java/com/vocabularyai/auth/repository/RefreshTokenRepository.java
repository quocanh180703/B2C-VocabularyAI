package com.vocabularyai.auth.repository;

import com.vocabularyai.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for RefreshToken entity
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Delete all expired refresh tokens
     */
    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
    
    /**
     * Delete all refresh tokens for a user
     */
    void deleteByUserId(Long userId);
}
