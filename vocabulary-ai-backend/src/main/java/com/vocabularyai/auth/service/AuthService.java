package com.vocabularyai.auth.service;

import com.vocabularyai.auth.config.JwtProperties;
import com.vocabularyai.auth.dto.*;
import com.vocabularyai.auth.entity.RefreshToken;
import com.vocabularyai.auth.repository.RefreshTokenRepository;
import com.vocabularyai.auth.security.JwtProvider;
import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.common.exception.UnauthorizedException;
import com.vocabularyai.user.dto.UserResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service
 * 
 * Business logic for authentication operations:
 * - Register
 * - Login
 * - Refresh Token
 * - Logout
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    
    /**
     * Login user with flexible emailOrUsername
     * 
     * Accepts both email and username for login
     * - If input is email: finds user by email, then uses username for authentication
     * - If input is username: uses directly for authentication
     */
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt for emailOrUsername: {}", request.getEmailOrUsername());
        
        // Determine if input is email or username
        String usernameForAuth = request.getEmailOrUsername();
        
        if (isEmail(request.getEmailOrUsername())) {
            // If email, find user by email and get username
            User user = userRepository.findByEmail(request.getEmailOrUsername())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", request.getEmailOrUsername());
                    return new UnauthorizedException("Email hoặc username không hợp lệ");
                });
            usernameForAuth = user.getUsername();
            log.debug("Email found, using username for authentication: {}", usernameForAuth);
        }
        
        // Authenticate using Spring Security with username
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                usernameForAuth,
                request.getPassword()
            )
        );
        
        User user = (User) authentication.getPrincipal();
        log.info("User logged in successfully: {}", user.getUsername());
        
        return generateAuthResponse(user);
    }
    
    /**
     * Check if input string is an email
     * Simple regex check for email format
     */
    private boolean isEmail(String input) {
        return input != null && input.contains("@");
    }
    
    /**
     * Refresh access token using refresh token
     * 
     * SECURITY:
     * - Validate refresh token exists and not revoked
     * - ✅ TASK 5: Validate user status (ACTIVE only)
     * - Prevent INACTIVE/BANNED users from refreshing tokens
     * - Generate new access token
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");
        
        // Validate refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> {
                log.warn("Invalid refresh token");
                return new UnauthorizedException("Invalid refresh token");
            });
        
        // Check if token is valid
        if (!refreshToken.isValid()) {
            log.warn("Refresh token is expired or revoked");
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }
        
        User user = refreshToken.getUser();
        
        // ✅ TASK 5: Validate user status before refreshing
        validateUserStatusForTokenRefresh(user);
        
        // Generate new access token
        String newAccessToken = jwtProvider.generateAccessToken(
            user.getUsername(),
            user.getId(),
            user.getRole().name()
        );
        
        log.info("Access token refreshed successfully for user: {} (status: {})", 
                 user.getUsername(), user.getStatus());
        
        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(request.getRefreshToken())
            .tokenType("Bearer")
            .expiresIn(jwtProperties.getAccessToken().getExpiration() / 1000)
            .user(UserResponse.fromEntity(user))
            .build();
    }
    
    /**
     * Validate user status for token refresh
     * 
     * Only ACTIVE users can refresh tokens
     * INACTIVE/BANNED users are denied
     * 
     * @param user User to validate
     * @throws UnauthorizedException If user is not ACTIVE
     */
    private void validateUserStatusForTokenRefresh(User user) {
        if (user.getStatus() == User.UserStatus.INACTIVE) {
            log.warn("Cannot refresh token for inactive user: {}", user.getUsername());
            throw new UnauthorizedException(
                "Your account is inactive (suspended). You cannot refresh your token."
            );
        }
        
        if (user.getStatus() == User.UserStatus.BANNED) {
            log.warn("Cannot refresh token for banned user: {}", user.getUsername());
            throw new UnauthorizedException(
                "Your account is banned. You cannot refresh your token."
            );
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            log.warn("Cannot refresh token for user with invalid status: {}", user.getStatus());
            throw new UnauthorizedException(
                "Your account has invalid status. Cannot refresh token."
            );
        }
    }
    
    /**
     * Logout user (revoke refresh token)
     */
    @Transactional
    public void logout(String refreshToken) {
        log.info("User logout");
        
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        
        log.info("User logged out successfully");
    }
    
    /**
     * Generate auth response with tokens
     */
    private AuthResponse generateAuthResponse(User user) {
        // Generate access token
        String accessToken = jwtProvider.generateAccessToken(
            user.getUsername(),
            user.getId(),
            user.getRole().name()
        );
        
        // Generate refresh token
        String refreshTokenString = jwtProvider.generateRefreshToken(
            user.getUsername(),
            user.getId()
        );
        
        // Save refresh token in database
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(refreshTokenString)
            .expiresAt(LocalDateTime.now().plusSeconds(
                jwtProperties.getRefreshToken().getExpiration() / 1000
            ))
            .revoked(false)
            .build();
        
        refreshTokenRepository.save(refreshToken);
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenString)
            .tokenType("Bearer")
            .expiresIn(jwtProperties.getAccessToken().getExpiration() / 1000)
            .user(UserResponse.fromEntity(user))
            .build();
    }
}
