package com.vocabularyai.ai.controller;

import com.vocabularyai.ai.dto.request.CreateConversationRequest;
import com.vocabularyai.ai.dto.request.SendMessageRequest;
import com.vocabularyai.ai.dto.response.ConversationDetailResponse;
import com.vocabularyai.ai.dto.response.ConversationResponse;
import com.vocabularyai.ai.dto.response.MessageResponse;
import com.vocabularyai.ai.service.chat.ChatService;
import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * ChatController
 * 
 * REST API endpoints cho quản lý trò chuyện AI:
 * - Tạo conversation mới
 * - Gửi message → nhận phản hồi
 * - Lấy danh sách conversations
 * - Lấy chi tiết conversation
 * - Archive/unarchive conversations
 * - Update conversation title
 * - Xóa conversations
 * 
 * Base path: /api/v1/ai/conversations
 * Authentication: Required (JWT)
 * 
 * Endpoints:
 * - POST / - Tạo conversation
 * - GET / - Lấy danh sách (active)
 * - GET /archived - Lấy danh sách (archived)
 * - GET /{conversationId} - Lấy chi tiết
 * - POST /{conversationId}/messages - Gửi message
 * - PUT /{conversationId}/title - Cập nhật title
 * - PUT /{conversationId}/archive - Archive
 * - PUT /{conversationId}/unarchive - Unarchive
 * - DELETE /{conversationId} - Xóa
 */
@RestController
@RequestMapping("/api/v1/ai/conversations")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {
    
    private final ChatService chatService;
    private final UserService userService;
    
    // ==================== Conversation Management ====================
    
    /**
     * POST /api/v1/ai/conversations
     * 
     * Tạo conversation mới
     * 
     * Request:
     * {
     *   "type": "GENERAL|VOCABULARY|GRAMMAR|LEARNING",
     *   "title": "Chat Title"
     * }
     * 
     * Response: 201 Created
     * {
     *   "success": true,
     *   "data": {
     *     "id": 123,
     *     "title": "Chat Title",
     *     "type": "GENERAL",
     *     "createdAt": "2024-01-01T10:00:00",
     *     "updatedAt": "2024-01-01T10:00:00",
     *     "messageCount": 0,
     *     "isArchived": false
     *   }
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @Valid @RequestBody CreateConversationRequest request) {
        log.info("Creating conversation: type={}", request.getType());
        
        User user = userService.getCurrentUser();
        ConversationResponse response = chatService.createConversation(user.getId(), request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Conversation created successfully", response));
    }
    
    /**
     * GET /api/v1/ai/conversations
     * 
     * Lấy danh sách conversations (active - chưa archive)
     * 
     * Query params:
     * - page: 0-based page number (default: 0)
     * - size: page size (default: 10)
     * - sort: sort field (default: updatedAt,desc)
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "content": [
     *       {
     *         "id": 123,
     *         "title": "...",
     *         "type": "GENERAL",
     *         ...
     *       }
     *     ],
     *     "pageable": {...},
     *     "totalElements": 50,
     *     "totalPages": 5
     *   }
     * }
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> listActiveConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        log.info("Listing active conversations: page={}, size={}", page, size);
        
        User user = userService.getCurrentUser();
        
        // Parse sort parameter
        Sort.Order sortOrder = sort.contains("asc")
            ? Sort.Order.asc(sort.replace(",asc", ""))
            : Sort.Order.desc(sort.replace(",desc", ""));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder));
        
        Page<ConversationResponse> result = chatService.listActiveConversations(user.getId(), pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Active conversations retrieved", result));
    }
    
    /**
     * GET /api/v1/ai/conversations/archived
     * 
     * Lấy danh sách conversations đã archive
     * 
     * Query params:
     * - page: 0-based page number (default: 0)
     * - size: page size (default: 10)
     * 
     * Response: 200 OK
     */
    @GetMapping("/archived")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> listArchivedConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Listing archived conversations: page={}, size={}", page, size);
        
        User user = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        
        Page<ConversationResponse> result = chatService.listArchivedConversations(user.getId(), pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Archived conversations retrieved", result));
    }
    
    /**
     * GET /api/v1/ai/conversations/{conversationId}
     * 
     * Lấy chi tiết conversation (bao gồm tất cả messages)
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "id": 123,
     *     "title": "...",
     *     "type": "GENERAL",
     *     "messages": [
     *       {
     *         "id": 1,
     *         "role": "USER|ASSISTANT",
     *         "content": "...",
     *         "createdAt": "...",
     *         "tokensUsed": 50,
     *         "responseTimeMs": 1200
     *       }
     *     ],
     *     ...
     *   }
     * }
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationDetailResponse>> getConversationDetail(
            @PathVariable Long conversationId) {
        log.info("Getting conversation detail: {}", conversationId);
        
        User user = userService.getCurrentUser();
        ConversationDetailResponse response = chatService.getConversationDetail(conversationId, user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Conversation retrieved", response));
    }
    
    // ==================== Messaging ====================
    
    /**
     * POST /api/v1/ai/conversations/{conversationId}/messages
     * 
     * Gửi message và nhận phản hồi từ AI
     * 
     * Request:
     * {
     *   "content": "Hello AI",
     *   "context": "optional context about the word/topic"
     * }
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "data": {
     *     "id": 2,
     *     "role": "ASSISTANT",
     *     "content": "Hello! How can I help you learn?",
     *     "createdAt": "2024-01-01T10:05:00",
     *     "tokensUsed": 80,
     *     "responseTimeMs": 1500
     *   },
     *   "message": "Message sent successfully"
     * }
     */
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        log.info("Sending message to conversation: {}, content length: {}", 
            conversationId, request.getContent().length());
        
        User user = userService.getCurrentUser();
        MessageResponse response = chatService.sendMessage(conversationId, user.getId(), request);
        
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", response));
    }
    
    // ==================== Conversation Metadata ====================
    
    /**
     * PUT /api/v1/ai/conversations/{conversationId}/title
     * 
     * Cập nhật tiêu đề conversation
     * 
     * Request:
     * {
     *   "title": "New Title"
     * }
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Conversation title updated"
     * }
     */
    @PutMapping("/{conversationId}/title")
    public ResponseEntity<ApiResponse<Void>> updateConversationTitle(
            @PathVariable Long conversationId,
            @Valid @RequestBody UpdateTitleRequest request) {
        log.info("Updating conversation title: {}, new title: {}", conversationId, request.getTitle());
        
        User user = userService.getCurrentUser();
        chatService.updateConversationTitle(conversationId, user.getId(), request.getTitle());
        
        return ResponseEntity.ok(ApiResponse.success("Conversation title updated", null));
    }
    
    /**
     * PUT /api/v1/ai/conversations/{conversationId}/archive
     * 
     * Archive một conversation
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Conversation archived"
     * }
     */
    @PutMapping("/{conversationId}/archive")
    public ResponseEntity<ApiResponse<Void>> archiveConversation(
            @PathVariable Long conversationId) {
        log.info("Archiving conversation: {}", conversationId);
        
        User user = userService.getCurrentUser();
        chatService.archiveConversation(conversationId, user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Conversation archived", null));
    }
    
    /**
     * PUT /api/v1/ai/conversations/{conversationId}/unarchive
     * 
     * Unarchive một conversation
     * 
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Conversation unarchived"
     * }
     */
    @PutMapping("/{conversationId}/unarchive")
    public ResponseEntity<ApiResponse<Void>> unarchiveConversation(
            @PathVariable Long conversationId) {
        log.info("Unarchiving conversation: {}", conversationId);
        
        User user = userService.getCurrentUser();
        chatService.unarchiveConversation(conversationId, user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Conversation unarchived", null));
    }
    
    /**
     * DELETE /api/v1/ai/conversations/{conversationId}
     * 
     * Xóa một conversation (và tất cả messages)
     * 
     * Response: 204 No Content
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long conversationId) {
        log.info("Deleting conversation: {}", conversationId);
        
        User user = userService.getCurrentUser();
        chatService.deleteConversation(conversationId, user.getId());
        
        return ResponseEntity.noContent().build();
    }
    
    // ==================== Helper DTOs ====================
    
    /**
     * Request DTO for updating conversation title
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateTitleRequest {
        @jakarta.validation.constraints.NotBlank(message = "Title cannot be blank")
        @jakarta.validation.constraints.Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        private String title;
    }
}
