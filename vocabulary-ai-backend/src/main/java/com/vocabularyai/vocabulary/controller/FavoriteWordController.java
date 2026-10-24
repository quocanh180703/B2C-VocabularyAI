package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.user.service.UserService;
import com.vocabularyai.vocabulary.dto.FavoriteWordResponse;
import com.vocabularyai.vocabulary.service.FavoriteWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Favorite Word Controller
 * 
 * API endpoints for managing favorite words
 */
@Slf4j
@RestController
@RequestMapping("/words")
@Tag(name = "Favorite Words", description = "Favorite words management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteWordController {
    
    private final FavoriteWordService favoriteWordService;
    private final UserService userService;

    public FavoriteWordController(FavoriteWordService favoriteWordService, UserService userService) {
        this.favoriteWordService = favoriteWordService;
        this.userService = userService;
    }
    
    /**
     * Add word to favorites
     */
    @PostMapping("/{wordId}/favorite")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Add word to favorites",
        description = "Add a vocabulary word to current user's favorites"
    )
    public ResponseEntity<ApiResponse<FavoriteWordResponse>> addToFavorites(
            @PathVariable String wordId) {
        log.info("Adding word {} to favorites", wordId);
        
        var currentUser = userService.getCurrentUser();
        FavoriteWordResponse favoriteWord = favoriteWordService.addToFavorites(currentUser, wordId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Word added to favorites successfully", favoriteWord)
        );
    }
    
    /**
     * Remove word from favorites
     */
    @DeleteMapping("/{wordId}/favorite")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Remove word from favorites",
        description = "Remove a vocabulary word from current user's favorites"
    )
    public ResponseEntity<ApiResponse<?>> removeFromFavorites(
            @PathVariable String wordId) {
        log.info("Removing word {} from favorites", wordId);
        
        Long userId = userService.getCurrentUserId();
        favoriteWordService.removeFromFavorites(userId, wordId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Word removed from favorites successfully")
        );
    }
    
    /**
     * Get all favorite words for current user
     */
    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user's favorite words",
        description = "Retrieve all favorite words for the authenticated user"
    )
    public ResponseEntity<ApiResponse<Page<FavoriteWordResponse>>> getFavoriteWords(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting favorite words for current user");
        
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<FavoriteWordResponse> favoriteWords = favoriteWordService.getUserFavorites(userId, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Favorite words retrieved successfully", favoriteWords)
        );
    }
}
