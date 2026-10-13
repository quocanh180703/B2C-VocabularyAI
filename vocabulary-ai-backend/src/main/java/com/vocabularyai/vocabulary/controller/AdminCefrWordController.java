package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.vocabulary.dto.CefrWordImportResult;
import com.vocabularyai.vocabulary.service.CefrWordImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminCefrWordController
 * 
 * REST API for admin-only CEFR word management operations
 * 
 * Endpoints:
 * - POST /api/admin/cefr-words/import/all - Import all CEFR levels (A1-C1)
 * - POST /api/admin/cefr-words/import/{level} - Import specific level
 * - GET /api/admin/cefr-words/import/statistics - Get import statistics
 * 
 * Security: ADMIN role required
 * Authentication: JWT Bearer token
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/cefr-words")
@RequiredArgsConstructor
@Tag(name = "Admin - CEFR Words", description = "Admin-only CEFR word management (import, statistics)")
@SecurityRequirement(name = "bearer-jwt")
public class AdminCefrWordController {

    private final CefrWordImportService cefrWordImportService;

    /**
     * POST /api/admin/cefr-words/import/all
     * 
     * Import all CEFR levels (A1-C1) from CSV files
     * 
     * Process:
     * 1. Load 5 CSV files (en-A1.csv, en-A2.csv, en-B1.csv, en-B2.csv, en-C1.csv)
     * 2. Parse and validate each row
     * 3. Batch insert (100 words per batch)
     * 4. Skip duplicates
     * 5. Return statistics
     * 
     * Expected result: ~4962 words imported
     * Duration: 30-60 seconds (depending on system)
     * 
     * @return Combined import result for all levels
     */
    @PostMapping("/import/all")
    @Secured("ROLE_ADMIN")  // Allow import regardless of authentication (for testing)
    @Operation(
            summary = "Import all CEFR word levels",
            description = "Import CEFR vocabulary from all 5 CSV files (A1-C1, ~4962 words total). " +
                    "This is a long-running operation that may take 30-60 seconds."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Import completed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CefrWordImportResult.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid JWT token"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - user is not admin"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
    )
    public ResponseEntity<CefrWordImportResult> importAllLevels() {
        log.info("ADMIN: Starting import of all CEFR levels (A1-C1)");

        try {
            CefrWordImportResult result = cefrWordImportService.importAllLevels();
            
            log.info("ADMIN: Import completed - Success: {}, Skip: {}, Error: {}",
                    result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            log.error("ADMIN: Error during import: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/admin/cefr-words/import/{level}
     * 
     * Import specific CEFR level
     * 
     * @param level CEFR level (A1, A2, B1, B2, C1)
     * @return Import result for the level
     */
    @PostMapping("/import/{level}")
    @Operation(
            summary = "Import specific CEFR level",
            description = "Import CEFR vocabulary for a specific level (A1, A2, B1, B2, or C1). " +
                    "Useful for partial imports or re-import a single level."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Import completed"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid CEFR level (must be A1, A2, B1, B2, or C1)"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - not admin"
    )
    public ResponseEntity<CefrWordImportResult> importLevel(
            @PathVariable String level
    ) {
        log.info("ADMIN: Starting import for CEFR level: {}", level);

        try {
            // Validate level
            if (!isValidLevel(level)) {
                log.warn("ADMIN: Invalid CEFR level: {}", level);
                return ResponseEntity.badRequest().build();
            }

            CefrWordImportResult result = cefrWordImportService.importLevel(level);
            
            log.info("ADMIN: Import for CEFR level {} completed - Success: {}, Skip: {}, Error: {}",
                    level, result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            log.error("ADMIN: Error importing CEFR level {}: {}", level, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/cefr-words/import/statistics
     * 
     * Get word import statistics (count by level)
     * 
     * @return Map of level -> word count
     */
    @GetMapping("/import/statistics")
    @Operation(
            summary = "Get CEFR word import statistics",
            description = "Get current CEFR word count statistics by CEFR level"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - not admin"
    )
    public ResponseEntity<Map<String, Long>> getStatistics() {
        log.info("ADMIN: Fetching CEFR word import statistics");

        try {
            Map<String, Long> stats = cefrWordImportService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("ADMIN: Error fetching CEFR statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/admin/cefr-words/upload
     * 
     * Upload and import CSV file
     * 
     * @param file CSV file to upload
     * @param level CEFR level for this file (optional - can be inferred from filename)
     * @return Import result
     */
    @PostMapping("/upload")
    @Secured("ROLE_ADMIN")
    @Operation(
            summary = "Upload and import CSV file",
            description = "Upload a CSV file and import CEFR words from it. " +
                    "File should have columns: word, pronunciation, meaning, example, part_of_speech"
    )
    @ApiResponse(
            responseCode = "201",
            description = "File uploaded and imported successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid file format"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid JWT token"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - user is not admin"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
    )
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "level", required = false) String level
    ) {
        log.info("ADMIN: Uploading CSV file: {}, level: {}", file.getOriginalFilename(), level);

        try {
            // Validate file
            if (file.isEmpty()) {
                log.warn("ADMIN: Uploaded file is empty");
                Map<String, Object> response = new HashMap<>();
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (!file.getContentType().contains("csv") && !file.getOriginalFilename().endsWith(".csv")) {
                log.warn("ADMIN: Invalid file type: {}", file.getContentType());
                Map<String, Object> response = new HashMap<>();
                response.put("error", "File must be CSV format");
                return ResponseEntity.badRequest().body(response);
            }

            // Determine CEFR level from level parameter or filename
            String inferredLevel = level;
            if (inferredLevel == null || inferredLevel.trim().isEmpty()) {
                inferredLevel = inferLevelFromFilename(file.getOriginalFilename());
            }

            if (inferredLevel == null) {
                log.warn("ADMIN: Could not determine CEFR level");
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Cannot determine CEFR level. Please provide level parameter (A1, A2, B1, B2, C1) or use filename like 'en-A1.csv'");
                return ResponseEntity.badRequest().body(response);
            }

            inferredLevel = inferredLevel.toUpperCase();

            if (!isValidLevel(inferredLevel)) {
                log.warn("ADMIN: Invalid CEFR level: {}", inferredLevel);
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Invalid CEFR level. Must be A1, A2, B1, B2, or C1");
                return ResponseEntity.badRequest().body(response);
            }

            // Parse CSV and import
            CefrWordImportResult result = importCsvFile(file, inferredLevel);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "File imported successfully");
            response.put("level", inferredLevel);
            response.put("filename", file.getOriginalFilename());
            response.put("successCount", result.getSuccessCount());
            response.put("skipCount", result.getSkipCount());
            response.put("errorCount", result.getErrorCount());

            log.info("ADMIN: File upload completed - Level: {}, Success: {}, Skip: {}, Error: {}",
                    inferredLevel, result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("ADMIN: Error uploading file: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Infer CEFR level from filename (e.g., "en-A1.csv" -> "A1")
     */
    private String inferLevelFromFilename(String filename) {
        if (filename == null) return null;
        
        String[] levels = {"A1", "A2", "B1", "B2", "C1"};
        for (String level : levels) {
            if (filename.toUpperCase().contains(level)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Parse CSV file and import words
     */
    private CefrWordImportResult importCsvFile(MultipartFile file, String level) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            // Read header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            String[] headers = headerLine.split(",");
            
            // Read data rows
            String line;
            int rowNum = 0;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length < 2) continue; // Skip incomplete rows
                
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                rows.add(row);
            }
        }

        // Use CefrWordImportService to import rows
        return cefrWordImportService.importFromRows(rows, level);
    }

    /**
     * Validate CEFR level
     */
    private boolean isValidLevel(String level) {
        if (level == null) {
            return false;
        }
        String normalized = level.toUpperCase();
        return normalized.equals("A1") || normalized.equals("A2") || 
               normalized.equals("B1") || normalized.equals("B2") || 
               normalized.equals("C1");
    }

}
