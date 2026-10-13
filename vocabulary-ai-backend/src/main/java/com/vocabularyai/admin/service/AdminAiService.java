package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.AiConversationDetailResponse;
import com.vocabularyai.admin.dto.response.AiConversationListResponse;
import com.vocabularyai.admin.dto.response.AiStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin AI Service Interface
 * 
 * Defines operations for AI conversation management from admin perspective
 */
public interface AdminAiService {
    
    /**
     * Get paginated list of all AI conversations
     * 
     * @param pageable Pagination parameters
     * @return Page of conversations
     */
    Page<AiConversationListResponse> listConversations(Pageable pageable);
    
    /**
     * Get AI conversations for specific user
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of user's conversations
     */
    Page<AiConversationListResponse> getConversationsByUser(Long userId, Pageable pageable);
    
    /**
     * Get AI conversations by type
     * 
     * @param conversationType Conversation type (GRAMMAR, HINT, VOCABULARY, etc.)
     * @param pageable Pagination parameters
     * @return Page of conversations of this type
     */
    Page<AiConversationListResponse> getConversationsByType(String conversationType, Pageable pageable);
    
    /**
     * Search AI conversations
     * 
     * @param searchTerm Search query
     * @param pageable Pagination parameters
     * @return Page of matching conversations
     */
    Page<AiConversationListResponse> searchConversations(String searchTerm, Pageable pageable);
    
    /**
     * Get conversation detail
     * 
     * @param conversationId Conversation ID
     * @return Conversation detail
     */
    AiConversationDetailResponse getConversationDetail(Long conversationId);
    
    /**
     * Get AI statistics
     * 
     * @return AI statistics
     */
    AiStatsResponse getStats();
}
