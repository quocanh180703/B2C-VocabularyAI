package com.vocabularyai.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.vocabularyai.auth.dto.response.AuthResponse;
import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.UnauthorizedException;
import com.vocabularyai.user.entity.AuthProvider;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for Google Identity Services authentication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Value("${google.client-id:}")
    private String googleClientId;

    /**
     * Authenticate or register user using Google ID token
     *
     * @param idTokenString JWT Google ID token sent from frontend
     * @return AuthResponse with JWT access and refresh tokens
     */
    @Transactional
    public AuthResponse loginWithGoogle(String idTokenString) {
        log.info("Processing Google login request");

        if (googleClientId == null || googleClientId.isBlank()) {
            log.error("Google Client ID is not configured in backend");
            throw new BadRequestException("Đăng nhập Google chưa được cấu hình");
        }

        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(idTokenString);
        } catch (Exception e) {
            log.warn("Failed to verify Google ID token: {}", e.getMessage());
            throw new UnauthorizedException("Token Google không hợp lệ hoặc đã hết hạn");
        }

        if (idToken == null) {
            log.warn("Google ID token verification returned null (invalid signature, expired, or wrong audience)");
            throw new UnauthorizedException("Token Google không hợp lệ hoặc đã hết hạn");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        Boolean emailVerified = payload.getEmailVerified();
        if (!Boolean.TRUE.equals(emailVerified)) {
            log.warn("Google account email is not verified: {}", payload.getEmail());
            throw new UnauthorizedException("Email tài khoản Google chưa được xác minh");
        }

        String sub = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        if (picture != null && picture.length() > 500) {
            picture = picture.substring(0, 500);
        }

        final String finalPicture = picture;

        User user = userRepository.findByProviderId(sub)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            log.info("Linking Google account (sub: {}) to existing user: {}", sub, existingUser.getUsername());
                            existingUser.setProviderId(sub);
                            return userRepository.save(existingUser);
                        })
                        .orElseGet(() -> {
                            log.info("Creating new user for Google login: {}", email);
                            String username = generateUniqueUsername(email);
                            String fullName = (name != null && !name.isBlank()) ? name : username;

                            User newUser = User.builder()
                                    .username(username)
                                    .email(email)
                                    .fullName(fullName)
                                    .avatarUrl(finalPicture)
                                    .role(User.UserRole.STUDENT)
                                    .status(User.UserStatus.ACTIVE)
                                    .provider(AuthProvider.GOOGLE)
                                    .providerId(sub)
                                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                    .emailRemindersEnabled(true)
                                    .build();

                            return userRepository.save(newUser);
                        }));

        // Validate user status (ACTIVE only)
        authService.validateUserStatus(user);

        log.info("User {} successfully authenticated with Google", user.getUsername());
        return authService.generateAuthResponse(user);
    }

    /**
     * Generate unique username from email prefix
     */
    public String generateUniqueUsername(String email) {
        String base = "user";
        if (email != null && email.contains("@")) {
            String prefix = email.substring(0, email.indexOf("@")).toLowerCase();
            String cleaned = prefix.replaceAll("[^a-z0-9_]", "");
            if (!cleaned.isBlank()) {
                base = cleaned;
            }
        }
        if (base.length() > 50) {
            base = base.substring(0, 50);
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }
}

