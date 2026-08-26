package com.vocabularyai.ai.repository;

import com.vocabularyai.ai.entity.AiMessage;
import com.vocabularyai.ai.entity.AiMessageRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for AiMessage entity
 * 
 * Handles database operations for messages within conversations
 */
@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    
    /**
     * Get all messages in a conversation, ordered chronologically (ascending)
     * 
     * Used to build conversation history for context
     * 
     * @param conversationId ID of the conversation
     * @return List of messages in chronological order
     */
    List<AiMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    
    /**
     * Get messages in a conversation paginated, ordered newest first
     * 
     * Used for pagination display in UI
     * 
     * @param conversationId ID of the conversation
     * @param pageable Pagination info
     * @return Page of messages in descending order
     */
    Page<AiMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    
    /**
     * Count total messages in a conversation
     * 
     * @param conversationId ID of the conversation
     * @return Total number of messages
     */
    int countByConversationId(Long conversationId);
    
    /**
     * Delete all messages in a conversation
     * 
     * @param conversationId ID of the conversation
     */
    @Modifying
    @Transactional
    void deleteByConversationId(Long conversationId);
    
    /**
     * Get messages of a specific role (USER or ASSISTANT) in a conversation
     * 
     * Used to analyze user vs AI messages separately
     * 
     * @param conversationId ID of the conversation
     * @param role Message role (USER or ASSISTANT)
     * @param pageable Pagination info
     * @return Page of messages with specified role
     */
    Page<AiMessage> findByConversationIdAndRoleOrderByCreatedAtDesc(
        Long conversationId,
        AiMessageRole role,
        Pageable pageable
    );
    
    /**
     * Get last N messages of a conversation (for context window)
     * 
     * Used by AI service to build context from recent messages
     * 
     * @param conversationId ID of the conversation
     * @param limit Number of recent messages to retrieve
     * @return List of recent messages
     */
    @Query(value = "SELECT * FROM ai_messages WHERE conversation_id = :conversationId ORDER BY created_at DESC LIMIT :limit", 
           nativeQuery = true)
    List<AiMessage> findRecentMessages(@Param("conversationId") Long conversationId, @Param("limit") int limit);
    
    /**
     * Count messages by role in a conversation
     * 
     * @param conversationId ID of the conversation
     * @param role Message role
     * @return Count of messages with specified role
     */
    long countByConversationIdAndRole(Long conversationId, AiMessageRole role);
}
