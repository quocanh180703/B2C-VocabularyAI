package com.vocabularyai.document.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.document.dto.DocumentAnalysisResponse;
import com.vocabularyai.document.dto.DocumentChatRequest;
import com.vocabularyai.document.dto.DocumentChatResponse;
import com.vocabularyai.document.dto.DocumentResponse;
import com.vocabularyai.document.dto.GenerateQuizFromDocumentRequest;
import com.vocabularyai.document.service.DocumentExerciseAiService;
import com.vocabularyai.document.service.DocumentService;
import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "User Documents & AI Exercises", description = "Endpoints for document upload and AI exercise generation")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentExerciseAiService documentExerciseAiService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload document", description = "Upload a document (.txt, .md, .pdf, .docx, max 10MB) and extract text")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        User user = userService.getCurrentUser();
        log.info("User {} uploading document: {}", user.getId(), file.getOriginalFilename());
        DocumentResponse response = documentService.uploadDocument(user, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải tài liệu lên thành công", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user documents", description = "Retrieve list of documents uploaded by current user")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getUserDocuments(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<DocumentResponse> response = documentService.getUserDocuments(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài liệu thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get document detail", description = "Get details of an uploaded document")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        DocumentResponse response = documentService.getDocument(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài liệu thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete document", description = "Delete an uploaded document and its physical file")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        documentService.deleteDocument(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài liệu thành công", null));
    }

    @PostMapping("/{id}/analyze")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Analyze document", description = "AI analyzes document, summarizes and suggests level and topics (no quota consumed)")
    public ResponseEntity<ApiResponse<DocumentAnalysisResponse>> analyzeDocument(
            @PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        log.info("User {} analyzing document id={}", userId, id);
        DocumentAnalysisResponse response = documentExerciseAiService.analyzeDocument(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Phân tích tài liệu thành công", response));
    }

    @PostMapping("/{id}/generate-chat")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Chat with document", description = "AI conversational refinement for exercise creation (no quota consumed)")
    public ResponseEntity<ApiResponse<DocumentChatResponse>> chatWithDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentChatRequest request) {
        Long userId = userService.getCurrentUserId();
        log.info("User {} chatting with document id={}", userId, id);
        DocumentChatResponse response = documentExerciseAiService.chatWithDocument(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Trợ lý AI đã phản hồi", response));
    }

    @PostMapping("/{id}/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate quiz from document", description = "Generate verified multiple choice quiz from document (consumes 1 EXERCISE_GENERATION quota)")
    public ResponseEntity<ApiResponse<QuizResponse>> generateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody GenerateQuizFromDocumentRequest request) {
        Long userId = userService.getCurrentUserId();
        log.info("User {} generating quiz from document id={}", userId, id);
        QuizResponse response = documentExerciseAiService.generateQuiz(id, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bài tập trắc nghiệm từ tài liệu thành công", response));
    }
}
