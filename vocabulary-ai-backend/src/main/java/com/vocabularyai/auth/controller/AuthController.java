package com.vocabularyai.auth.controller;

import com.vocabularyai.auth.dto.*;
import com.vocabularyai.auth.service.AuthService;
import com.vocabularyai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * API endpoints for authentication operations:
 * - Register
 * - Login
 * - Refresh Token
 * - Logout
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * User Registration
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Create a new user account"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User registered successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request or user already exists"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Register request for username: {}", request.getUsername());
        
        AuthResponse authResponse = authService.register(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("User registered successfully", authResponse)
        );
    }
    
    /**
     * User Login
     */
    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user and get JWT tokens"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        
        AuthResponse authResponse = authService.login(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Login successful", authResponse)
        );
    }
    
    /**
     * Refresh Access Token
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Get a new access token using refresh token"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Access token refreshed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshAccessToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request");
        
        AuthResponse authResponse = authService.refreshToken(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Access token refreshed successfully", authResponse)
        );
    }
    
    /**
     * User Logout
     */
    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Revoke refresh token and logout user"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful"
        )
    })
    public ResponseEntity<ApiResponse<?>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request");
        
        authService.logout(request.getRefreshToken());
        
        return ResponseEntity.ok(
            ApiResponse.success("Logout successful")
        );
    }
}
