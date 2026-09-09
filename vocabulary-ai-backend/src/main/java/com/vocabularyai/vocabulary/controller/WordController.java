package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.vocabulary.dto.*;
import com.vocabularyai.vocabulary.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Word Controller
 * 
 * API endpoints for word/vocabulary management
 */
@Slf4j
@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
@Tag(name = "Words", description = "Vocabulary word management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class WordController {
    
    private final WordService wordService;
    
    /**
     * Get all words
     */
    @GetMapping
    @Operation(summary = "Get all words", description = "Retrieve all vocabulary words with pagination")
    public ResponseEntity<ApiResponse<Page<WordResponse>>> getAllWords(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting all words");
        
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<WordResponse> words = wordService.getAllWords(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Words retrieved successfully", words)
        );
    }
    
    /**
     * Get word by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get word by ID", description = "Retrieve a specific vocabulary word")
    public ResponseEntity<ApiResponse<WordResponse>> getWordById(@PathVariable Long id) {
        log.info("Getting word with id: {}", id);
        
        WordResponse word = wordService.getWordById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Word retrieved successfully", word)
        );
    }
    
    /**
     * Create new word (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new word",
        description = "Create a new vocabulary word (Admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<WordResponse>> createWord(
            @Valid @RequestBody CreateWordRequest request) {
        log.info("Creating new word: {}", request.getWord());
        
        WordResponse word = wordService.createWord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Word created successfully", word)
        );
    }
    
    /**
     * Update word (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update word",
        description = "Update an existing vocabulary word (Admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<WordResponse>> updateWord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWordRequest request) {
        log.info("Updating word with id: {}", id);
        
        WordResponse word = wordService.updateWord(id, request);
        return ResponseEntity.ok(
            ApiResponse.success("Word updated successfully", word)
        );
    }
    
    /**
     * Delete word (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete word",
        description = "Delete a vocabulary word (Admin only)"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<?>> deleteWord(@PathVariable Long id) {
        log.info("Deleting word with id: {}", id);
        
        wordService.deleteWord(id);
        return ResponseEntity.ok(
            ApiResponse.success("Word deleted successfully")
        );
    }
    
    /**
     * Get words by category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(
        summary = "Get words by category",
        description = "Retrieve all words in a specific category"
    )
    public ResponseEntity<ApiResponse<Page<WordResponse>>> getWordsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting words for category: {}", categoryId);
        
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<WordResponse> words = wordService.getWordsByCategory(categoryId, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Words retrieved successfully", words)
        );
    }
    
    /**
     * Search words
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search words",
        description = "Search vocabulary words by keyword, category, and level"
    )
    public ResponseEntity<ApiResponse<Page<WordResponse>>> searchWords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Searching words with keyword: {}, categoryId: {}, level: {}", keyword, categoryId, level);
        
        WordSearchRequest request = WordSearchRequest.builder()
            .keyword(keyword)
            .categoryId(categoryId)
            .level(level != null ? WordResponse_mapper.stringToWordLevel(level) : null)
            .build();
        
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<WordResponse> words = wordService.searchWords(request, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Words retrieved successfully", words)
        );
    }
}

// Helper class for enum conversion
class WordResponse_mapper {
    public static com.vocabularyai.vocabulary.entity.Word.WordLevel stringToWordLevel(String level) {
        try {
            return com.vocabularyai.vocabulary.entity.Word.WordLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
