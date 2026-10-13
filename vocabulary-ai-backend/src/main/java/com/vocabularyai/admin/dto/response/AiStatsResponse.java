package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI Statistics Response DTO
 * 
 * Represents overall AI conversation statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStatsResponse {
    
    private Long totalConversations;
    
    private Long activeUsers;
    
    private Integer totalMessages;
    
    private Double avgMessagesPerConversation;
    
    private Map<String, Long> conversationTypeDistribution;
}
