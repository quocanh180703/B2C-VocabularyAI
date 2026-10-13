package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.vocabulary.dto.CefrWordResponse;
import com.vocabularyai.vocabulary.dto.CefrWordSearchRequest;
import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.service.CefrWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CefrWordController
 * 
 * REST API for CEFR vocabulary words
 * 
 * Endpoints:
 * - GET /api/cefr-words - List all words (paginated, filterable)
 * - GET /api/cefr-words/{id} - Get word by ID
 * - GET /api/cefr-words/lemma/{lemma} - Get word by lemma
 * - GET /api/cefr-words/search - Search words
 * - GET /api/cefr-words/random - Get random word
 * - GET /api/cefr-words/statistics - Get word statistics
 * 
 * Authentication: Not required for these endpoints (public)
 */
@Slf4j
@RestController
@RequestMapping("/api/cefr-words")
@RequiredArgsConstructor
@Tag(name = "CEFR Words", description = "CEFR vocabulary word management and search")
public class CefrWordController {

    private final CefrWordService cefrWordService;

    /**
     * GET /api/cefr-words
     * 
     * Get all words with optional filtering by CEFR level
     * 
     * @param level Optional CEFR level filter (A1, A2, B1, B2, C1)
     * @param page Page number (0-indexed), default 0
     * @param size Page size, default 20
     * @return Page of words
     */
    @GetMapping
    @Operation(summary = "Get all CEFR words", description = "Retrieve paginated list of CEFR words, optionally filtered by CEFR level")
    @ApiResponse(responseCode = "200", description = "CEFR words retrieved successfully")
    public ResponseEntity<Page<CefrWordResponse>> getAllWords(
            @Parameter(description = "CEFR level filter (optional)", example = "B1")
            @RequestParam(required = false) CEFRLevel level,

            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /cefr-words - level: {}, page: {}, size: {}", level, page, size);

        if (level != null) {
            Page<CefrWordResponse> words = cefrWordService.getWordsByLevel(level, page, size);
            return ResponseEntity.ok(words);
        }

        // Return empty page for now (implement full list if needed)
        return ResponseEntity.ok(Page.empty());
    }

    /**
     * GET /api/cefr-words/{id}
     * 
     * Get word by UUID
     * 
     * @param id CefrWord UUID
     * @return Word details or 404
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get CEFR word by ID", description = "Retrieve a specific CEFR word by its UUID")
    @ApiResponse(responseCode = "200", description = "CEFR word found")
    @ApiResponse(responseCode = "404", description = "CEFR word not found")
    public ResponseEntity<CefrWordResponse> getWordById(
            @Parameter(description = "CEFR word UUID", example = "00092354-13f5-47aa-94c6-3312579db388")
            @PathVariable String id
    ) {
        log.info("GET /cefr-words/{} - Get CEFR word by ID", id);

        CefrWordResponse word = cefrWordService.getWordById(id);
        if (word == null) {
            log.warn("CEFR word not found: {}", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(word);
    }

    /**
     * GET /api/cefr-words/lemma/{lemma}
     * 
     * Get word by lemma (word text)
     * 
     * @param lemma Word text
     * @return Word details or 404
     */
    @GetMapping("/lemma/{lemma}")
    @Operation(summary = "Get CEFR word by lemma", description = "Retrieve a word by its text/lemma")
    @ApiResponse(responseCode = "200", description = "CEFR word found")
    @ApiResponse(responseCode = "404", description = "CEFR word not found")
    public ResponseEntity<CefrWordResponse> getWordByLemma(
            @Parameter(description = "Word text", example = "hello")
            @PathVariable String lemma
    ) {
        log.info("GET /cefr-words/lemma/{} - Get CEFR word by lemma", lemma);

        CefrWordResponse word = cefrWordService.getWordByLemma(lemma);
        if (word == null) {
            log.warn("CEFR word not found: {}", lemma);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(word);
    }

    /**
     * GET /api/cefr-words/search
     * 
     * Search words by lemma, definition, or other fields
     * 
     * @param query Search query
     * @param level Optional CEFR level filter
     * @param page Page number
     * @param size Page size
     * @return Page of matching words
     */
    @GetMapping("/search")
    @Operation(summary = "Search CEFR words", description = "Search words by lemma, definition, or other criteria")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<Page<CefrWordResponse>> searchWords(
            @Parameter(description = "Search query (lemma or definition)", example = "beautiful")
            @RequestParam(required = false) String query,

            @Parameter(description = "CEFR level filter (optional)", example = "B2")
            @RequestParam(required = false) CEFRLevel level,

            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /cefr-words/search - query: {}, level: {}, page: {}, size: {}", query, level, page, size);

        CefrWordSearchRequest request = CefrWordSearchRequest.builder()
                .query(query)
                .level(level)
                .page(page)
                .size(size)
                .build();

        Page<CefrWordResponse> results = cefrWordService.searchWords(request);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/cefr-words/autocomplete
     * 
     * Autocomplete: Find words starting with prefix
     * 
     * @param prefix Word prefix
     * @param limit Max results (default 10)
     * @return List of matching words
     */
    @GetMapping("/autocomplete")
    @Operation(summary = "Autocomplete CEFR words", description = "Find words starting with given prefix for autocomplete UI")
    @ApiResponse(responseCode = "200", description = "Autocomplete results")
    public ResponseEntity<Page<CefrWordResponse>> autocomplete(
            @Parameter(description = "Word prefix", example = "hel")
            @RequestParam String prefix,

            @Parameter(description = "Max results", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /cefr-words/autocomplete - prefix: {}, limit: {}", prefix, limit);

        if (prefix == null || prefix.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Page<CefrWordResponse> results = cefrWordService.autocomplete(prefix, limit);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/cefr-words/random
     * 
     * Get random word at specific CEFR level
     * 
     * @param level CEFR level (optional, default A1)
     * @return Random word or 404
     */
    @GetMapping("/random")
    @Operation(summary = "Get random CEFR word", description = "Retrieve a random word at specific CEFR level (useful for daily challenge)")
    @ApiResponse(responseCode = "200", description = "Random CEFR word found")
    @ApiResponse(responseCode = "404", description = "No words found at level")
    public ResponseEntity<CefrWordResponse> getRandomWord(
            @Parameter(description = "CEFR level", example = "B1")
            @RequestParam(defaultValue = "A1") CEFRLevel level
    ) {
        log.info("GET /cefr-words/random - level: {}", level);

        CefrWordResponse word = cefrWordService.getRandomWord(level);
        if (word == null) {
            log.warn("No CEFR words found at level: {}", level);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(word);
    }

    /**
     * GET /api/cefr-words/statistics
     * 
     * Get word statistics (count by level)
     * 
     * @return Statistics object
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get CEFR word statistics", description = "Get counts of CEFR words by CEFR level")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    public ResponseEntity<CefrWordService.CefrWordStatistics> getStatistics() {
        log.info("GET /cefr-words/statistics");

        CefrWordService.CefrWordStatistics stats = cefrWordService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
