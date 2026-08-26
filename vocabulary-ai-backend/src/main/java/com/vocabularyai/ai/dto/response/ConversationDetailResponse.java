package com.vocabularyai.ai.dto.response;

import com.vocabularyai.ai.entity.AiConversation;
import com.vocabularyai.ai.entity.AiConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for conversation with full message history
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDetailResponse {
    
    private Long id;
    private String title;
    private AiConversationType type;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isArchived;
    private List<MessageResponse> messages;
    
    /**
     * Convert from AiConversation entity to detail DTO
     */
    public static ConversationDetailResponse fromEntity(AiConversation entity) {
        List<MessageResponse> messageResponses = entity.getMessages() != null
            ? entity.getMessages().stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList())
            : new ArrayList<>();
        
        return ConversationDetailResponse.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .type(entity.getType())
            .summary(entity.getSummary())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .isArchived(entity.getIsArchived())
            .messages(messageResponses)
            .build();
    }
}
