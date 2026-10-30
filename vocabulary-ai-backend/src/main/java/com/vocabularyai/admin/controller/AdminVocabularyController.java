package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.CreateVocabularyRequest;
import com.vocabularyai.admin.dto.request.UpdateVocabularyRequest;
import com.vocabularyai.admin.dto.response.VocabularyListResponse;
import com.vocabularyai.admin.dto.response.VocabularyDetailResponse;
import com.vocabularyai.admin.dto.response.CefrStatsResponse;
import com.vocabularyai.admin.dto.response.CategoryListResponse;
import com.vocabularyai.admin.service.AdminVocabularyService;
import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * Admin Vocabulary Controller
 * 
 * Endpoints for admin vocabulary management
 * All endpoints require ADMIN role authorization
 * 
 * Endpoints:
 * - GET /api/admin/vocabulary - List all vocabulary words (paginated)
 * - GET /api/admin/vocabulary?cefr=B1 - Filter by CEFR level
 * - GET /api/admin/vocabulary?search=beautiful - Search vocabulary
 * - GET /api/admin/vocabulary?pos=ADJECTIVE&cefr=B1 - Filter by POS and level
 * - GET /api/admin/vocabulary/{wordId} - Get word detail
 * - GET /api/admin/vocabulary/cefr-stats - Get CEFR distribution statistics
 * - GET /api/admin/vocabulary/categories - List all categories
 * - POST /api/admin/vocabulary/bulk-upload - Bulk upload words (CSV)
 */
@RestController
@RequestMapping("/admin/vocabulary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Vocabulary", description = "Admin vocabulary management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVocabularyController {
    
    private final AdminVocabularyService vocabularyService;
    
    /**
     * Get paginated list of all vocabulary words
     * 
     * Supports filtering and searching:
     * - Pagination
     * - Sorting by any field
     * - Filter by CEFR level
     * - Filter by part of speech
     * - Search by lemma or definition
     * 
     * Endpoint: GET /api/admin/vocabulary
     * Query Parameters:
     * - page: Page number (0-based, default: 0)
     * - size: Page size (default: 20)
     * - sort: Sort field and direction (e.g., "word,asc")
     * - cefr: Filter by CEFR level (A1, A2, B1, B2, C1, optional)
     * - pos: Filter by part of speech (NOUN, VERB, ADJECTIVE, ADVERB, etc., optional)
     * - search: Search by word or definition (optional)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Vocabulary list retrieved successfully",
     *   "data": {
     *     "content": [
     *       {
     *         "id": "word_123",
     *         "word": "beautiful",
     *         "pronunciation": "/ˈbjuːtɪfəl/",
     *         "partOfSpeech": "ADJECTIVE",
     *         "cefr": "A1",
     *         "meaning": "pleasing to look at",
     *         "source": "OXFORD",
     *         "createdAt": "2024-01-15T10:30:00"
     *       }
     *     ],
     *     "totalElements": 4962,
     *     "totalPages": 249,
     *     "currentPage": 0,
     *     "pageSize": 20
     *   }
     * }
     */
    @GetMapping
    @Operation(
        summary = "Get vocabulary list",
        description = "Retrieve paginated list of vocabulary words with optional filtering and search (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Vocabulary list retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        )
    })
    public ResponseEntity<ApiResponse<Page<VocabularyListResponse>>> getVocabulary(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field and direction (e.g., 'word,asc')", example = "word,asc")
            @RequestParam(required = false, defaultValue = "word,asc") String sort,
            
            @Parameter(description = "Filter by CEFR level (A1, A2, B1, B2, C1)", example = "B1")
            @RequestParam(required = false) String cefr,
            
            @Parameter(description = "Filter by part of speech (NOUN, VERB, ADJECTIVE, etc.)", example = "ADJECTIVE")
            @RequestParam(required = false) String pos,
            
            @Parameter(description = "Search by word or definition", example = "beautiful")
            @RequestParam(required = false) String search) {
        
        try {
            log.info("GET /api/admin/vocabulary - Fetching vocabulary list (page={}, size={}, cefr={}, pos={}, search={})",
                page, size, cefr, pos, search);
            
            Pageable pageable = PaginationUtils.createPageable(page, size, sort);
            
            Page<VocabularyListResponse> result = vocabularyService.getVocabularyFiltered(pos, cefr, search, pageable);
            log.debug("Vocabulary retrieved: {} results", result.getTotalElements());
            
            return ResponseEntity.ok(
                ApiResponse.success("Vocabulary list retrieved successfully", result)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving vocabulary list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve vocabulary: " + e.getMessage()));
        }
    }
    
    /**
     * Get complete word information
     * 
     * Returns:
     * - Word details (pronunciation, definitions)
     * - CEFR level and part of speech
     * - Related information
     * 
     * Endpoint: GET /api/admin/vocabulary/{wordId}
     * Path Parameters:
     * - wordId: Word ID
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Word detail retrieved successfully",
     *   "data": {
     *     "id": "word_123",
     *     "word": "beautiful",
     *     "pronunciation": "/ˈbjuːtɪfəl/",
     *     "partOfSpeech": "ADJECTIVE",
     *     "cefr": "A1",
     *     "definition": "pleasing to look at; attractive",
     *     "source": "OXFORD",
     *     "language": "en",
     *     "usageCount": 5432,
     *     "createdAt": "2024-01-15T10:30:00",
     *     "updatedAt": "2024-03-20T15:45:00"
     *   }
     * }
     */
    @GetMapping("/{wordId}")
    @Operation(
        summary = "Get word detail",
        description = "Retrieve complete word information including pronunciation, definition, and metadata (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Word detail retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Word not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching word"
        )
    })
    public ResponseEntity<ApiResponse<VocabularyDetailResponse>> getWordDetail(
            @Parameter(description = "Word ID", example = "word_123")
            @PathVariable String wordId) {
        
        try {
            log.info("GET /api/admin/vocabulary/{} - Retrieving word detail", wordId);
            
            VocabularyDetailResponse word = vocabularyService.getVocabularyDetail(wordId);
            
            log.debug("Word detail retrieved: id={}, word={}", word.getId(), word.getWord());
            
            return ResponseEntity.ok(
                ApiResponse.success("Word detail retrieved successfully", word)
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("Word not found: {}", wordId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Word not found"));
        } catch (Exception e) {
            log.error("Error retrieving word detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve word detail: " + e.getMessage()));
        }
    }
    
    /**
     * Get CEFR level distribution statistics
     * 
     * Returns:
     * - Count of words at each CEFR level (A1, A2, B1, B2, C1)
     * - Total word count
     * 
     * Endpoint: GET /api/admin/vocabulary/stats/cefr
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "CEFR statistics retrieved successfully",
     *   "data": {
     *     "a1Count": 850,
     *     "a2Count": 1200,
     *     "b1Count": 1100,
     *     "b2Count": 650,
     *     "c1Count": 162,
     *     "totalCount": 4962
     *   }
     * }
     */
    @GetMapping("/stats/cefr")
    @Operation(
        summary = "Get CEFR statistics",
        description = "Retrieve distribution of vocabulary words by CEFR level (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "CEFR statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching statistics"
        )
    })
    public ResponseEntity<ApiResponse<CefrStatsResponse>> getCefrStats() {
        try {
            log.info("GET /api/admin/vocabulary/stats/cefr - Retrieving CEFR statistics");
            
            CefrStatsResponse stats = vocabularyService.getCefrStatistics();
            
            log.debug("CEFR statistics retrieved: total={}", stats.getTotalCount());
            
            return ResponseEntity.ok(
                ApiResponse.success("CEFR statistics retrieved successfully", stats)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving CEFR statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve CEFR statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get list of vocabulary categories
     * 
     * Returns all available categories/topics for vocabulary
     * 
     * Endpoint: GET /api/admin/vocabulary/categories
     * Query Parameters:
     * - page: Page number (0-based, default: 0)
     * - size: Page size (default: 50)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Categories retrieved successfully",
     *   "data": {
     *     "content": [
     *       {
     *         "categoryId": "cat_1",
     *         "categoryName": "Animals",
     *         "wordCount": 250,
     *         "createdAt": "2024-01-15T10:30:00"
     *       }
     *     ],
     *     "totalElements": 45,
     *     "totalPages": 1,
     *     "currentPage": 0,
     *     "pageSize": 50
     *   }
     * }
     */
    @GetMapping("/categories")
    @Operation(
        summary = "Get vocabulary categories",
        description = "Retrieve list of vocabulary categories (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching categories"
        )
    })
    public ResponseEntity<ApiResponse<Page<CategoryListResponse>>> getCategories(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size", example = "50")
            @RequestParam(required = false, defaultValue = "50") Integer size) {
        
        try {
            log.info("GET /api/admin/vocabulary/categories - Fetching categories (page={}, size={})",
                page, size);
            
            Pageable pageable = PaginationUtils.createPageable(page, size, "categoryId,asc");
            
            Page<CategoryListResponse> result = vocabularyService.getCategories(pageable);
            
            log.debug("Categories fetched: {} total", result.getTotalElements());
            
            return ResponseEntity.ok(
                ApiResponse.success("Categories retrieved successfully", result)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve categories: " + e.getMessage()));
        }
    }
    
    /**
     * Create new vocabulary entry
     * 
     * Endpoint: POST /api/admin/vocabulary
     * Request Body:
     * {
     *   "word": "beautiful",
     *   "meaning": "đẹp",
     *   "partOfSpeech": "ADJECTIVE",
     *   "level": "A1",
     *   "pronunciation": "/ˈbjuːtɪfəl/",
     *   "definition": "pleasing to look at; attractive",
     *   "example": "This is a beautiful day.",
     *   "audioUrl": "https://example.com/audio/beautiful.mp3",
     *   "imageUrl": "https://example.com/images/beautiful.jpg"
     * }
     * 
     * Response (201 CREATED):
     * {
     *   "success": true,
     *   "message": "Vocabulary created successfully",
     *   "data": {
     *     "id": "word_456",
     *     "word": "beautiful",
     *     "meaning": "đẹp",
     *     "partOfSpeech": "ADJECTIVE",
     *     "cefr": "A1",
     *     "pronunciation": "/ˈbjuːtɪfəl/",
     *     "createdAt": "2024-10-03T15:30:00"
     *   }
     * }
     */
    @PostMapping
    @Operation(
        summary = "Create vocabulary entry",
        description = "Create a new vocabulary entry (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Vocabulary created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation failed"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        )
    })
    public ResponseEntity<ApiResponse<VocabularyDetailResponse>> createVocabulary(
            @Valid @RequestBody CreateVocabularyRequest request) {
        
        try {
            log.info("POST /api/admin/vocabulary - Creating vocabulary: {}", request.getWord());
            
            VocabularyDetailResponse created = vocabularyService.createVocabulary(request);
            
            log.debug("Vocabulary created: id={}, word={}", created.getId(), created.getWord());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vocabulary created successfully", created));
            
        } catch (IllegalArgumentException e) {
            log.warn("Vocabulary creation validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating vocabulary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create vocabulary: " + e.getMessage()));
        }
    }
    
    /**
     * Update existing vocabulary entry
     * 
     * Endpoint: PUT /api/admin/vocabulary/{wordId}
     * Request Body (partial update):
     * {
     *   "meaning": "rất đẹp",
     *   "definition": "extremely pleasing to look at"
     * }
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Vocabulary updated successfully",
     *   "data": {
     *     "id": "word_456",
     *     "word": "beautiful",
     *     "meaning": "rất đẹp",
     *     "partOfSpeech": "ADJECTIVE",
     *     "cefr": "A1",
     *     "definition": "extremely pleasing to look at",
     *     "updatedAt": "2024-10-03T16:45:00"
     *   }
     * }
     */
    @PutMapping("/{wordId}")
    @Operation(
        summary = "Update vocabulary entry",
        description = "Update an existing vocabulary entry (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Vocabulary updated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation failed"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Vocabulary not found"
        )
    })
    public ResponseEntity<ApiResponse<VocabularyDetailResponse>> updateVocabulary(
            @Parameter(description = "Word ID", example = "word_456")
            @PathVariable String wordId,
            
            @Valid @RequestBody UpdateVocabularyRequest request) {
        
        try {
            log.info("PUT /api/admin/vocabulary/{} - Updating vocabulary", wordId);
            
            VocabularyDetailResponse updated = vocabularyService.updateVocabulary(wordId, request);
            
            log.debug("Vocabulary updated: id={}, word={}", updated.getId(), updated.getWord());
            
            return ResponseEntity.ok(
                ApiResponse.success("Vocabulary updated successfully", updated)
            );
            
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                log.warn("Vocabulary not found: {}", wordId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Vocabulary not found"));
            }
            log.warn("Vocabulary update validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating vocabulary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update vocabulary: " + e.getMessage()));
        }
    }
    
    /**
     * Delete vocabulary entry (soft delete)
     * 
     * Endpoint: DELETE /api/admin/vocabulary/{wordId}
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Vocabulary deleted successfully",
     *   "data": null
     * }
     */
    @DeleteMapping("/{wordId}")
    @Operation(
        summary = "Delete vocabulary entry",
        description = "Delete a vocabulary entry (soft delete - marks as deleted, admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Vocabulary deleted successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Vocabulary not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteVocabulary(
            @Parameter(description = "Word ID", example = "word_456")
            @PathVariable String wordId) {
        
        try {
            log.info("DELETE /api/admin/vocabulary/{} - Deleting vocabulary", wordId);
            
            vocabularyService.deleteVocabulary(wordId);
            
            log.debug("Vocabulary deleted: id={}", wordId);
            
            return ResponseEntity.ok(
                ApiResponse.success("Vocabulary deleted successfully", null)
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("Vocabulary not found: {}", wordId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Vocabulary not found"));
        } catch (Exception e) {
            log.error("Error deleting vocabulary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete vocabulary: " + e.getMessage()));
        }
    }
}

