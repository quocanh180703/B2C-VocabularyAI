package com.vocabularyai.vocabulary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CefrWordImportResult DTO
 * 
 * Response object for CEFR word import operations
 * Contains statistics and error details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CefrWordImportResult {

    /**
     * CEFR level imported (null for bulk import)
     */
    private String level;

    /**
     * Start time of import
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * End time of import
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * Duration in seconds
     */
    private long durationSeconds;

    /**
     * Total words processed (rows in CSV)
     */
    private int totalWords;

    /**
     * Successfully imported words
     */
    private int successCount;

    /**
     * Skipped words (duplicates, validation errors)
     */
    private int skipCount;

    /**
     * Words with errors
     */
    private int errorCount;

    /**
     * Error messages (if any)
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * Add error message
     */
    public void addError(String message) {
        this.errors.add(message);
    }

    /**
     * Success rate percentage
     */
    public double getSuccessRate() {
        if (totalWords == 0) {
            return 0;
        }
        return (double) successCount / totalWords * 100;
    }

    /**
     * Skip rate percentage
     */
    public double getSkipRate() {
        if (totalWords == 0) {
            return 0;
        }
        return (double) skipCount / totalWords * 100;
    }

    /**
     * Check if import was successful
     */
    public boolean isSuccess() {
        return errorCount == 0 && successCount > 0;
    }

    /**
     * Get summary message
     */
    public String getSummary() {
        return String.format(
                "Imported %d/%d CEFR words (%.1f%% success, %.1f%% skipped, %d errors)",
                successCount, totalWords, getSuccessRate(), getSkipRate(), errorCount
        );
    }
}
