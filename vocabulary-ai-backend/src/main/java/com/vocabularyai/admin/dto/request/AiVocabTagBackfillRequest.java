package com.vocabularyai.admin.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVocabTagBackfillRequest {

    /**
     * Scope:
     * - "IDS": Scoped to specific word UUIDs in wordIds
     * - "IMPORT": Scoped to an import batch by createdAfter and/or source
     * - "ALL": Scan all untagged words in database
     */
    @Builder.Default
    private String scope = "IDS";

    /**
     * List of word UUIDs (required when scope = "IDS")
     */
    private List<String> wordIds;

    /**
     * Lower bound for creation date (for scope = "IMPORT")
     */
    private LocalDateTime createdAfter;

    /**
     * Source dataset tag, e.g. "oxford_3000_5000" (for scope = "IMPORT")
     */
    private String source;

    @Min(value = 10, message = "Batch size must be at least 10")
    @Max(value = 200, message = "Batch size cannot exceed 200")
    @Builder.Default
    private Integer batchSize = 20;

    @DecimalMin(value = "0.1", message = "Confidence threshold must be at least 0.1")
    @DecimalMax(value = "1.0", message = "Confidence threshold cannot exceed 1.0")
    @Builder.Default
    private Double confidenceThreshold = 0.8;
}

