package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.*;
import com.vocabularyai.admin.dto.response.*;
import com.vocabularyai.admin.service.AdminAiVocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai-vocabulary")
@Tag(name = "Admin AI Vocabulary Studio", description = "AI-powered vocabulary content generation, audit, and approval workflow")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminAiVocabularyController {

    private final AdminAiVocabularyService adminAiVocabularyService;

    @GetMapping("/audit")
    @Operation(summary = "Audit vocabulary quality", description = "Get counts of words missing examples, meanings, definitions or pronunciations")
    public ResponseEntity<AiVocabAuditResponse> auditQuality() {
        log.info("GET /api/admin/ai-vocabulary/audit - Auditing vocabulary data quality");
        AiVocabAuditResponse res = adminAiVocabularyService.auditQuality();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/missing-data")
    @Operation(summary = "Get words needing enrichment", description = "Fetch sample words with missing attributes to enrich")
    public ResponseEntity<List<AiMissingWordDto>> getMissingDataWords(
            @Parameter(description = "Filter type: example, meaning, pronunciation, or definition")
            @RequestParam(defaultValue = "example") String type,
            @Parameter(description = "Limit of items (default 10, max 30)")
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /api/admin/ai-vocabulary/missing-data?type={}&limit={}", type, limit);
        List<AiMissingWordDto> words = adminAiVocabularyService.getWordsNeedingEnrichment(type, limit);
        return ResponseEntity.ok(words);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate vocabulary content with AI", description = "Generates IPA, POS, meaning, definition, and bilingual examples")
    public ResponseEntity<AiGeneratedWordDto> generateWord(
            @Valid @RequestBody AiVocabGenerateRequest request
    ) {
        log.info("POST /api/admin/ai-vocabulary/generate - Word: '{}'", request.getWord());
        AiGeneratedWordDto dto = adminAiVocabularyService.generateWordContent(request.getWord(), request.getLevelHint());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/batch-generate")
    @Operation(summary = "Batch generate vocabulary", description = "Generates dictionary entries for multiple words at once (max 10)")
    public ResponseEntity<List<AiGeneratedWordDto>> batchGenerate(
            @Valid @RequestBody AiVocabBatchGenerateRequest request
    ) {
        log.info("POST /api/admin/ai-vocabulary/batch-generate - Count: {}", request.getWords().size());
        List<AiGeneratedWordDto> dtos = adminAiVocabularyService.batchGenerateWords(request.getWords(), request.getLevelHint());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/approve")
    @Operation(summary = "Approve and save AI generated words", description = "Persists admin-verified words to the database")
    public ResponseEntity<Map<String, Object>> approveWords(
            @Valid @RequestBody AiVocabApproveRequest request
    ) {
        log.info("POST /api/admin/ai-vocabulary/approve - Approving {} words", request.getWords().size());
        int savedCount = adminAiVocabularyService.approveAndSaveWords(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("savedCount", savedCount);
        response.put("message", "Đã phê duyệt và lưu thành công " + savedCount + " từ vựng vào hệ thống.");

        return ResponseEntity.ok(response);
    }

    // ==================== Topic Classification & Backfill Endpoints ====================

    @PostMapping("/tag-classify")
    @Operation(summary = "Classify topics for words", description = "Uses AI to classify topics for up to 20 words based on entry definitions")
    public ResponseEntity<List<AiVocabTagClassifyItemDto>> tagClassify(
            @Valid @RequestBody AiVocabTagClassifyRequest request
    ) {
        log.info("POST /api/admin/ai-vocabulary/tag-classify - Count: {}", request.getLemmas().size());
        List<AiVocabTagClassifyItemDto> dtos = adminAiVocabularyService.classifyWordTopics(request.getLemmas());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/tag-approve")
    @Operation(summary = "Approve topics for words", description = "Saves topic categories to cefr_words and evicts search cache")
    public ResponseEntity<Map<String, Object>> tagApprove(
            @Valid @RequestBody AiVocabTagApproveRequest request
    ) {
        log.info("POST /api/admin/ai-vocabulary/tag-approve - Count: {}, overwrite: {}", request.getItems().size(), request.isOverwrite());
        int savedCount = adminAiVocabularyService.approveAndSaveTopics(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("savedCount", savedCount);
        response.put("message", "Đã cập nhật thành công chủ đề cho " + savedCount + " từ vựng.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/tag-backfill")
    @Operation(summary = "Start background backfill of topics", description = "Starts async job to classify and tag untagged words within given scope")
    public ResponseEntity<AiVocabTagBackfillStatusResponse> tagBackfill(
            @RequestBody(required = false) AiVocabTagBackfillRequest request
    ) {
        if (request == null) {
            return ResponseEntity.badRequest().body(AiVocabTagBackfillStatusResponse.builder()
                    .status("FAILED")
                    .message("Scope rỗng. Vui lòng chỉ định scope 'IDS' hoặc 'IMPORT'.")
                    .build());
        }

        String scope = request.getScope() != null ? request.getScope().trim().toUpperCase() : "IDS";
        request.setScope(scope);

        if ("IDS".equals(scope)) {
            if (request.getWordIds() == null || request.getWordIds().isEmpty()) {
                return ResponseEntity.badRequest().body(AiVocabTagBackfillStatusResponse.builder()
                        .status("FAILED")
                        .message("Vui lòng chọn ít nhất 1 từ vựng để gắn tag (scope IDS).")
                        .build());
            }
            if (request.getWordIds().size() > 500) {
                return ResponseEntity.badRequest().body(AiVocabTagBackfillStatusResponse.builder()
                        .status("FAILED")
                        .message("Số lượng từ gắn tag tối đa mỗi đợt là 500 từ.")
                        .build());
            }
        } else if ("IMPORT".equals(scope)) {
            boolean hasCreatedAfter = request.getCreatedAfter() != null;
            boolean hasSource = request.getSource() != null && !request.getSource().isBlank();
            if (!hasCreatedAfter && !hasSource) {
                return ResponseEntity.badRequest().body(AiVocabTagBackfillStatusResponse.builder()
                        .status("FAILED")
                        .message("Vui lòng chỉ định nguồn import (source) hoặc ngày tạo (createdAfter) cho scope IMPORT.")
                        .build());
            }
        } else if (!"ALL".equals(scope)) {
            return ResponseEntity.badRequest().body(AiVocabTagBackfillStatusResponse.builder()
                    .status("FAILED")
                    .message("Phạm vi không hợp lệ. Chỉ chấp nhận 'IDS', 'IMPORT' hoặc 'ALL'.")
                    .build());
        }

        log.info("POST /api/admin/ai-vocabulary/tag-backfill - scope={}, batchSize={}, threshold={}",
                request.getScope(), request.getBatchSize(), request.getConfidenceThreshold());
        AiVocabTagBackfillStatusResponse status = adminAiVocabularyService.startTopicBackfill(request);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/sources")
    @Operation(summary = "Get distinct dataset sources", description = "Returns list of all source tags available in cefr_words")
    public ResponseEntity<List<String>> getWordSources() {
        return ResponseEntity.ok(adminAiVocabularyService.getDistinctWordSources());
    }

    @GetMapping("/tag-backfill/{jobId}")
    @Operation(summary = "Get topic backfill status", description = "Returns progress of the background topic backfill job")
    public ResponseEntity<AiVocabTagBackfillStatusResponse> tagBackfillStatus(
            @PathVariable String jobId
    ) {
        AiVocabTagBackfillStatusResponse status = adminAiVocabularyService.getTopicBackfillStatus(jobId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/tag-backfill/cancel")
    @Operation(summary = "Cancel background backfill", description = "Stops the currently running background backfill job")
    public ResponseEntity<AiVocabTagBackfillStatusResponse> cancelTagBackfill() {
        log.info("POST /api/admin/ai-vocabulary/tag-backfill/cancel - Request to stop backfill job");
        AiVocabTagBackfillStatusResponse status = adminAiVocabularyService.stopTopicBackfill();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/tag-queue")
    @Operation(summary = "Get topic review queue", description = "Lists words where AI suggested topics with low confidence")
    public ResponseEntity<Page<TopicTagQueueDto>> getTagQueue(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/admin/ai-vocabulary/tag-queue?status={}&page={}&size={}", status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<TopicTagQueueDto> queue = adminAiVocabularyService.getTopicTagQueue(status, pageable);
        return ResponseEntity.ok(queue);
    }
}
