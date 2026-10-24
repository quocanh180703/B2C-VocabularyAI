package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.vocabulary.dto.WordResponse;
import com.vocabularyai.vocabulary.dto.WordSearchRequest;
import com.vocabularyai.vocabulary.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WordController
 * 
 * REST API for user vocabulary words (NOT CEFR dataset)
 * For CEFR dataset, use CefrWordController instead
 * 
 * Endpoints:
 * - GET /api/words/{id} - Get word by ID
 * - GET /api/words/search - Search words
 */
@Slf4j
@RestController
@RequestMapping({"/words", "/api/words"})
@Tag(name = "User Words", description = "User vocabulary word management")
public class WordController {

    private final WordService wordService;
    private final com.vocabularyai.payment.service.FeatureUsageService featureUsageService;
    private final com.vocabularyai.user.service.UserService userService;

    public WordController(WordService wordService,
                          com.vocabularyai.payment.service.FeatureUsageService featureUsageService,
                          com.vocabularyai.user.service.UserService userService) {
        this.wordService = wordService;
        this.featureUsageService = featureUsageService;
        this.userService = userService;
    }

    private Long getCurrentUserIdSafely() {
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            if (auth.getPrincipal() instanceof com.vocabularyai.user.entity.User u) {
                return u.getId();
            }
            return userService.getCurrentUserId();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * GET /api/words/{id}
     * 
     * Get user word by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user word by ID", description = "Retrieve a specific user vocabulary word by its ID")
    @ApiResponse(responseCode = "200", description = "Word found")
    @ApiResponse(responseCode = "404", description = "Word not found")
    public ResponseEntity<WordResponse> getWordById(
            @Parameter(description = "Word ID")
            @PathVariable Long id
    ) {
        log.info("GET /words/{} - Get user word by ID", id);

        WordResponse word = wordService.getWordById(id);
        if (word == null) {
            log.warn("User word not found: {}", id);
            return ResponseEntity.notFound().build();
        }

        if (word.getLevel() != null) {
            Long userId = getCurrentUserIdSafely();
            if (!featureUsageService.canAccessWord(userId, String.valueOf(id), word.getLevel())) {
                throw new com.vocabularyai.common.exception.UpgradeRequiredException(
                    "CEFR_B1_C1",
                    "Từ vựng cấp độ " + word.getLevel() + " dành riêng cho tài khoản Premium. Nâng cấp để mở khóa toàn bộ từ vựng."
                );
            }
        }

        return ResponseEntity.ok(word);
    }

    /**
     * GET /api/words/search
     * 
     * Search user words
     */
    @GetMapping("/search")
    @Operation(summary = "Search user words", description = "Search user vocabulary words by text or meaning")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<Page<WordResponse>> searchWords(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String query,

            @Parameter(description = "Page number")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /words/search - query: {}, page: {}, size: {}", query, page, size);

        WordSearchRequest request = WordSearchRequest.builder()
                .query(query)
                .page(page)
                .size(size)
                .build();

        Page<WordResponse> results = wordService.searchWords(request);
        return ResponseEntity.ok(results);
    }
}
