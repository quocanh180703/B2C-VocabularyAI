package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI Conversation Detail Response DTO
 * 
 * Represents complete information of an AI conversation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConversationDetailResponse {
    
    private Long id;
    
    private Long userId;
    
    private String userEmail;
    
    private String userFirstName;
    
    private String userLastName;
    
    private String conversationType;
    
    private String title;
    
    private String summary;
    
    private Integer messageCount;
    
    private Boolean isArchived;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
