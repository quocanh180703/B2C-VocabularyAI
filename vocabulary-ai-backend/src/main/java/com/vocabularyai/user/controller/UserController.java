package com.vocabularyai.user.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.user.dto.ChangePasswordRequest;
import com.vocabularyai.user.dto.UpdateUserRequest;
import com.vocabularyai.user.dto.UserResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.vocabularyai.common.exception.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * User Controller
 * 
 * API endpoints for user management
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user profile",
        description = "Get the profile of the currently authenticated user"
    )
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("Getting current user profile");
        UserResponse userResponse = userService.getCurrentUserResponse();
        return ResponseEntity.ok(
            ApiResponse.success("User profile retrieved successfully", userResponse)
        );
    }
    
    /**
     * Update current user profile
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update current user profile",
        description = "Update the profile of the currently authenticated user"
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating current user profile");
        Long userId = userService.getCurrentUserId();
        UserResponse userResponse = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(
            ApiResponse.success("User profile updated successfully", userResponse)
        );
    }
    
    /**
     * Upload current user avatar to Cloudinary
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Upload user avatar",
        description = "Upload an avatar image to Cloudinary and update user profile"
    )
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        log.info("Uploading avatar for current user");
        MultipartFile uploadFile = avatar != null ? avatar : file;
        if (uploadFile == null || uploadFile.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn tệp ảnh để tải lên.");
        }
        Long userId = userService.getCurrentUserId();
        UserResponse userResponse = userService.uploadAvatar(userId, uploadFile);
        return ResponseEntity.ok(
            ApiResponse.success("Ảnh đại diện đã được cập nhật thành công!", userResponse)
        );
    }
    
    /**
     * Delete user avatar and revert to default
     */
    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete user avatar",
        description = "Delete user avatar and reset to default"
    )
    public ResponseEntity<ApiResponse<UserResponse>> deleteAvatar() {
        log.info("Deleting avatar for current user");
        Long userId = userService.getCurrentUserId();
        UserResponse userResponse = userService.deleteAvatar(userId);
        return ResponseEntity.ok(
            ApiResponse.success("Đã xóa ảnh đại diện thành công!", userResponse)
        );
    }
    
    /**
     * Update user password
     */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update user password",
        description = "Update the password of the currently authenticated user"
    )
    public ResponseEntity<ApiResponse<?>> updatePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Updating user password");
        Long userId = userService.getCurrentUserId();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(
            ApiResponse.success("Password updated successfully")
        );
    }
    
    /**
     * TASK 11: Update user status (ADMIN only)
     * 
     * Admin endpoint to manage user account status
     * - ACTIVE: Normal user
     * - INACTIVE: Account suspended
     * - BANNED: Account banned
     * 
     * When status changes to INACTIVE or BANNED, all user-specific caches are invalidated
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user status",
        description = "Update user account status (ADMIN only). Updates status and invalidates user caches."
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam User.UserStatus status) {
        log.info("Updating user status for userId: {} to status: {} by admin", userId, status);
        
        userService.updateUserStatus(userId, status);
        UserResponse userResponse = UserResponse.fromEntity(userService.getUserById(userId));
        
        return ResponseEntity.ok(
            ApiResponse.success("User status updated successfully", userResponse)
        );
    }
}
