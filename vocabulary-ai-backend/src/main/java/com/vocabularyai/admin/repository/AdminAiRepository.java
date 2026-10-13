package com.vocabularyai.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vocabularyai.admin.entity.AdminQueryResult;

import java.util.List;

/**
 * Admin AI Repository
 * 
 * Provides queries for AI conversation management (admin system-wide view)
 * Supports listing, searching, and filtering AI conversations
 */
@Repository
public interface AdminAiRepository extends JpaRepository<AdminQueryResult, Long> {
    
    // ==================== Conversation List ====================
    
    /**
     * Get paginated list of all AI conversations
     * 
     * @param pageable Pagination and sorting
     * @return Page of conversations
     */
    @Query("SELECT new map(" +
           "ac.id as id, " +
           "u.id as userId, " +
           "u.email as userEmail, " +
           "ac.type as conversationType, " +
           "ac.title as title, " +
           "0 as messageCount, " +
           "ac.isArchived as isArchived, " +
           "ac.createdAt as createdAt, " +
           "ac.updatedAt as updatedAt) " +
           "FROM AiConversation ac " +
           "LEFT JOIN ac.user u " +
           "ORDER BY ac.createdAt DESC")
    Page<?> getConversationList(Pageable pageable);
    
    /**
     * Get AI conversations for specific user
     * 
     * @param userId User ID
     * @param pageable Pagination
     * @return Page of user's conversations
     */
    @Query("SELECT new map(" +
           "ac.id as id, " +
           "u.id as userId, " +
           "u.email as userEmail, " +
           "ac.type as conversationType, " +
           "ac.title as title, " +
           "0 as messageCount, " +
           "ac.isArchived as isArchived, " +
           "ac.createdAt as createdAt, " +
           "ac.updatedAt as updatedAt) " +
           "FROM AiConversation ac " +
           "LEFT JOIN ac.user u " +
           "WHERE u.id = :userId " +
           "ORDER BY ac.createdAt DESC")
    Page<?> getConversationsByUser(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get AI conversations by type (GRAMMAR, HINT, VOCABULARY, etc.)
     * 
     * @param conversationType Conversation type
     * @param pageable Pagination
     * @return Page of conversations of this type
     */
    @Query("SELECT new map(" +
           "ac.id as id, " +
           "u.id as userId, " +
           "u.email as userEmail, " +
           "ac.type as conversationType, " +
           "ac.title as title, " +
           "0 as messageCount, " +
           "ac.isArchived as isArchived, " +
           "ac.createdAt as createdAt, " +
           "ac.updatedAt as updatedAt) " +
           "FROM AiConversation ac " +
           "LEFT JOIN ac.user u " +
           "WHERE ac.type = :conversationType " +
           "ORDER BY ac.createdAt DESC")
    Page<?> getConversationsByType(@Param("conversationType") String conversationType, Pageable pageable);
    
    /**
     * Search conversations by title or summary
     * 
     * @param searchTerm Search query (case-insensitive)
     * @param pageable Pagination
     * @return Page of matching conversations
     */
    @Query("SELECT new map(" +
           "ac.id as id, " +
           "u.id as userId, " +
           "u.email as userEmail, " +
           "ac.type as conversationType, " +
           "ac.title as title, " +
           "0 as messageCount, " +
           "ac.isArchived as isArchived, " +
           "ac.createdAt as createdAt, " +
           "ac.updatedAt as updatedAt) " +
           "FROM AiConversation ac " +
           "LEFT JOIN ac.user u " +
           "WHERE LOWER(ac.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ac.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY ac.createdAt DESC")
    Page<?> searchConversations(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // ==================== Conversation Detail ====================
    
    /**
     * Get complete conversation information
     * 
     * @param conversationId Conversation ID
     * @return Map with conversation details
     */
    @Query("SELECT new map(" +
           "ac.id as id, " +
           "u.id as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "ac.type as conversationType, " +
           "ac.title as title, " +
           "ac.summary as summary, " +
           "0 as messageCount, " +
           "ac.isArchived as isArchived, " +
           "ac.createdAt as createdAt, " +
           "ac.updatedAt as updatedAt) " +
           "FROM AiConversation ac " +
           "LEFT JOIN ac.user u " +
           "WHERE ac.id = :conversationId")
    List<?> getConversationDetail(@Param("conversationId") Long conversationId);
    
    // ==================== Statistics ====================
    
    /**
     * Get AI conversation statistics
     * 
     * @return Map with various statistics
     */
    @Query("SELECT new map(" +
           "COUNT(DISTINCT ac.id) as totalConversations, " +
           "COUNT(DISTINCT ac.user.id) as activeUsers, " +
           "0 as totalMessages, " +
           "0.0 as avgMessagesPerConversation) " +
           "FROM AiConversation ac")
    List<?> getConversationStats();
    
    /**
     * Get conversation count by type
     * 
     * @return Map with counts per conversation type
     */
    @Query("SELECT new map(" +
           "ac.type as conversationType, " +
           "COUNT(ac) as count) " +
           "FROM AiConversation ac " +
           "GROUP BY ac.type")
    List<?> getConversationCountByType();
}
