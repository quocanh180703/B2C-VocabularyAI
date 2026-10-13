package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.response.AiConversationDetailResponse;
import com.vocabularyai.admin.dto.response.AiConversationListResponse;
import com.vocabularyai.admin.dto.response.AiStatsResponse;
import com.vocabularyai.admin.service.AdminAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin AI Controller
 * 
 * REST endpoints for AI conversation management
 * Base path: /api/admin/ai
 */
@RestController
@RequestMapping("/api/admin/ai")
@Tag(name = "Admin AI Management", description = "API for managing AI conversations")
@RequiredArgsConstructor
@Slf4j
public class AdminAiController {
    
    private final AdminAiService adminAiService;
    
    /**
     * Get paginated list of all AI conversations
     * 
     * @param pageable Pagination parameters
     * @return Page of AI conversations
     */
    @GetMapping
    @Operation(summary = "List AI conversations", description = "Get paginated list of all AI conversations with optional filtering")
    public ResponseEntity<Page<AiConversationListResponse>> listConversations(
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by conversation type")
            @RequestParam(required = false) String type,
            @Parameter(description = "Search term for title/summary")
            @RequestParam(required = false) String search) {
        
        log.info("GET /api/admin/ai - List AI conversations");
        
        Page<AiConversationListResponse> conversations;
        
        if (search != null && !search.trim().isEmpty()) {
            log.debug("Searching conversations with term: {}", search);
            conversations = adminAiService.searchConversations(search, pageable);
        } else if (userId != null) {
            log.debug("Filtering conversations by user: {}", userId);
            conversations = adminAiService.getConversationsByUser(userId, pageable);
        } else if (type != null && !type.trim().isEmpty()) {
            log.debug("Filtering conversations by type: {}", type);
            conversations = adminAiService.getConversationsByType(type, pageable);
        } else {
            log.debug("Listing all conversations");
            conversations = adminAiService.listConversations(pageable);
        }
        
        return ResponseEntity.ok(conversations);
    }
    
    /**
     * Get AI conversation detail
     * 
     * @param conversationId Conversation ID
     * @return Conversation detail
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "Get conversation detail", description = "Get complete information of an AI conversation")
    public ResponseEntity<AiConversationDetailResponse> getConversationDetail(
            @Parameter(description = "Conversation ID")
            @PathVariable Long conversationId) {
        
        log.info("GET /api/admin/ai/{} - Get conversation detail", conversationId);
        
        AiConversationDetailResponse detail = adminAiService.getConversationDetail(conversationId);
        
        if (detail == null) {
            log.warn("Conversation not found: {}", conversationId);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(detail);
    }
    
    /**
     * Get AI statistics
     * 
     * @return AI statistics
     */
    @GetMapping("/stats/summary")
    @Operation(summary = "Get AI statistics", description = "Get overall statistics about AI conversations")
    public ResponseEntity<AiStatsResponse> getStats() {
        
        log.info("GET /api/admin/ai/stats/summary - Get statistics");
        
        AiStatsResponse stats = adminAiService.getStats();
        return ResponseEntity.ok(stats);
    }
}
