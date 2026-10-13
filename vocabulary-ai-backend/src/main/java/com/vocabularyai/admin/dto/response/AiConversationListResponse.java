package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI Conversation List Response DTO
 * 
 * Represents a single AI conversation in list view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConversationListResponse {
    
    private Long id;
    
    private Long userId;
    
    private String userEmail;
    
    private String conversationType;
    
    private String title;
    
    private Integer messageCount;
    
    private Boolean isArchived;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
