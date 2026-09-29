package com.vocabularyai.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vocabularyai.user.entity.User;

import java.time.LocalDateTime;

/**
 * PasswordResetToken Entity
 * 
 * Maps to 'password_reset_tokens' table in MySQL
 * Represents password reset tokens used in forgot password flow
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_password_reset_tokens_email", columnList = "email"),
    @Index(name = "idx_password_reset_tokens_token", columnList = "reset_token"),
    @Index(name = "idx_password_reset_tokens_expires_at", columnList = "expires_at"),
    @Index(name = "idx_password_reset_tokens_used", columnList = "used")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "createdAt"})
@ToString(exclude = {"user"})
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @Column(nullable = false, length = 255)
    private String email;
    
    @Column(name = "reset_token", nullable = false, length = 255, unique = true)
    private String resetToken;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Check if token has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if token is valid (not expired and not used)
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }
}
