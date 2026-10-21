package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.AiVocabApproveRequest;
import com.vocabularyai.admin.dto.request.AiVocabBatchGenerateRequest;
import com.vocabularyai.admin.dto.request.AiVocabGenerateRequest;
import com.vocabularyai.admin.dto.response.AiGeneratedWordDto;
import com.vocabularyai.admin.dto.response.AiMissingWordDto;
import com.vocabularyai.admin.dto.response.AiVocabAuditResponse;
import com.vocabularyai.admin.service.AdminAiVocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
