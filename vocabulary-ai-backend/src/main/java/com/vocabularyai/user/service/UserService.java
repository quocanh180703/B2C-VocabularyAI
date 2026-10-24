package com.vocabularyai.user.service;

import com.vocabularyai.ai.service.cache.CacheInvalidationService;
import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.user.dto.UpdateUserRequest;
import com.vocabularyai.user.dto.UserResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import com.vocabularyai.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * User Service
 * 
 * Business logic for user management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheInvalidationService cacheInvalidationService;
    private final CloudinaryService cloudinaryService;
    
    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("User", "id", userId));
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> ResourceNotFoundException.of("User", "username", username));
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> ResourceNotFoundException.of("User", "email", email));
    }
    
    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        
        String username = authentication.getName();
        return getUserByUsername(username);
    }
    
    /**
     * Get current user ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    /**
     * Get current user response
     */
    public UserResponse getCurrentUserResponse() {
        User user = getCurrentUser();
        return UserResponse.fromEntity(user);
    }
    
    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateUserProfile(Long userId, UpdateUserRequest request) {
        log.info("Updating user profile for userId: {}", userId);
        
        User user = getUserById(userId);
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        if (request.getEmailRemindersEnabled() != null) {
            user.setEmailRemindersEnabled(request.getEmailRemindersEnabled());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for userId: {}", userId);
        
        return UserResponse.fromEntity(updatedUser);
    }
    
    /**
     * Update user preferences (e.g., email reminders toggle)
     */
    @Transactional
    public UserResponse updatePreferences(Long userId, Boolean emailRemindersEnabled) {
        log.info("Updating preferences for userId: {}, emailRemindersEnabled: {}", userId, emailRemindersEnabled);
        User user = getUserById(userId);
        if (emailRemindersEnabled != null) {
            user.setEmailRemindersEnabled(emailRemindersEnabled);
        }
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }
    
    /**
     * Upload user avatar to Cloudinary and update profile
     */
    @Transactional
    public UserResponse uploadAvatar(Long userId, MultipartFile file) {
        log.info("Processing avatar upload for userId: {}", userId);
        User user = getUserById(userId);
        
        String oldAvatarUrl = user.getAvatarUrl();
        String newAvatarUrl = cloudinaryService.uploadAvatar(userId, file);
        
        user.setAvatarUrl(newAvatarUrl);
        User updatedUser = userRepository.save(user);
        log.info("Avatar updated successfully for userId: {}, url: {}", userId, newAvatarUrl);
        
        // Clean up previous Cloudinary avatar if applicable
        if (oldAvatarUrl != null && oldAvatarUrl.contains("cloudinary.com") && oldAvatarUrl.contains("vocabulary-ai/avatars")) {
            try {
                cloudinaryService.deleteImageByUrl(oldAvatarUrl);
            } catch (Exception e) {
                log.warn("Could not delete old avatar for userId {}: {}", userId, e.getMessage());
            }
        }
        
        return UserResponse.fromEntity(updatedUser);
    }
    
    /**
     * Delete user avatar and revert to default
     */
    @Transactional
    public UserResponse deleteAvatar(Long userId) {
        log.info("Processing avatar deletion for userId: {}", userId);
        User user = getUserById(userId);
        String oldAvatarUrl = user.getAvatarUrl();
        
        user.setAvatarUrl(null);
        User updatedUser = userRepository.save(user);
        log.info("Avatar removed successfully for userId: {}", userId);
        
        if (oldAvatarUrl != null && oldAvatarUrl.contains("cloudinary.com") && oldAvatarUrl.contains("vocabulary-ai/avatars")) {
            try {
                cloudinaryService.deleteImageByUrl(oldAvatarUrl);
            } catch (Exception e) {
                log.warn("Could not delete old avatar for userId {}: {}", userId, e.getMessage());
            }
        }
        
        return UserResponse.fromEntity(updatedUser);
    }
    
    /**
     * Save user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for userId: {}", userId);
        
        User user = getUserById(userId);
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Invalid old password for userId: {}", userId);
            throw new BadRequestException("Old password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for userId: {}", userId);
    }
    
    /**
     * TASK 11: Update user status and invalidate caches
     * 
     * Updates user status (ACTIVE, INACTIVE, BANNED)
     * When status changes to INACTIVE or BANNED, invalidates all user-specific caches
     * 
     * Security flow:
     * 1. User status updated in database
     * 2. All user caches invalidated (AI analysis, recommendations, etc.)
     * 3. Future requests:
     *    - Authentication fails (JwtAuthenticationFilter checks status)
     *    - Or if somehow bypassed, service layer checks status again
     *    - Cache miss (deleted) forces recomputation from database
     * 
     * @param userId User ID to update
     * @param newStatus New status (ACTIVE, INACTIVE, BANNED)
     */
    @Transactional
    public void updateUserStatus(Long userId, User.UserStatus newStatus) {
        log.info("[TASK 11] Updating user status for userId: {} to status: {}", userId, newStatus);
        
        User user = getUserById(userId);
        User.UserStatus oldStatus = user.getStatus();
        
        // Update status
        user.setStatus(newStatus);
        userRepository.save(user);
        
        log.info("[TASK 11] User status updated successfully for userId: {} ({} → {})", 
                 userId, oldStatus, newStatus);
        
        // TASK 11: If status changed to INACTIVE or BANNED, invalidate all caches
        if ((newStatus == User.UserStatus.INACTIVE || newStatus == User.UserStatus.BANNED) &&
            oldStatus != newStatus) {
            
            log.info("[TASK 11] Invalidating caches due to status change for userId: {}", userId);
            cacheInvalidationService.invalidateAllUserCaches(userId);
        }
    }
}
