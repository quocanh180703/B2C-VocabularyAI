package com.vocabularyai.ai.dto.response;

import com.vocabularyai.ai.entity.AiConversation;
import com.vocabularyai.ai.entity.AiConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for conversation summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    
    private Long id;
    private String title;
    private AiConversationType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer messageCount;
    private Boolean isArchived;
    
    /**
     * Convert from AiConversation entity to DTO
     */
    public static ConversationResponse fromEntity(AiConversation entity) {
        return ConversationResponse.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .type(entity.getType())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .messageCount(entity.getMessageCount())
            .isArchived(entity.getIsArchived())
            .build();
    }
}
