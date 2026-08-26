package com.vocabularyai.user.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.user.dto.ChangePasswordRequest;
import com.vocabularyai.user.dto.UpdateUserRequest;
import com.vocabularyai.user.dto.UserResponse;
import com.vocabularyai.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
