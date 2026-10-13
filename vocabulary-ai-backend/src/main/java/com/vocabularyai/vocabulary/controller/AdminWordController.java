package com.vocabularyai.vocabulary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminWordController
 * 
 * REST API for admin-only user vocabulary word management
 * For CEFR word import admin operations, use AdminCefrWordController instead
 * 
 * Security: ADMIN role required
 * Authentication: JWT Bearer token
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/words")
@RequiredArgsConstructor
@Tag(name = "Admin - User Words", description = "Admin-only user vocabulary word management")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWordController {

}
