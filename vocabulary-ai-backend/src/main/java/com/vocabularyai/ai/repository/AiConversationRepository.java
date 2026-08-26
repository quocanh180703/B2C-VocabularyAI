package com.vocabularyai.ai.repository;

import com.vocabularyai.ai.entity.AiConversation;
import com.vocabularyai.ai.entity.AiConversationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AiConversation entity
 * 
 * Handles database operations for conversations between users and AI
 */
@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    
    /**
     * Get all conversations for a specific user, paginated
     * 
     * @param userId ID of the user
     * @param pageable Pagination info
     * @return Page of conversations
     */
    Page<AiConversation> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Get a specific conversation by ID and verify it belongs to the user
     * 
     * Used for authorization checks - ensure user owns the conversation
     * 
     * @param id Conversation ID
     * @param userId ID of the user
     * @return Optional containing conversation if found and belongs to user
     */
    Optional<AiConversation> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Get all conversations of a specific type for a user
     * 
     * @param userId ID of the user
     * @param type Conversation type (GENERAL, VOCABULARY, GRAMMAR, LEARNING)
     * @return List of conversations matching the type
     */
    List<AiConversation> findByUserIdAndType(Long userId, AiConversationType type);
    
    /**
     * Count total conversations for a user
     * 
     * @param userId ID of the user
     * @return Total number of conversations
     */
    long countByUserId(Long userId);
    
    /**
     * Get non-archived conversations for a user, sorted by recent first
     * 
     * @param userId ID of the user
     * @param pageable Pagination info
     * @return Page of active conversations
     */
    @Query("SELECT c FROM AiConversation c WHERE c.user.id = :userId AND c.isArchived = false ORDER BY c.updatedAt DESC")
    Page<AiConversation> findActiveConversationsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get archived conversations for a user
     * 
     * @param userId ID of the user
     * @param pageable Pagination info
     * @return Page of archived conversations
     */
    @Query("SELECT c FROM AiConversation c WHERE c.user.id = :userId AND c.isArchived = true ORDER BY c.updatedAt DESC")
    Page<AiConversation> findArchivedConversationsByUserId(@Param("userId") Long userId, Pageable pageable);
}
