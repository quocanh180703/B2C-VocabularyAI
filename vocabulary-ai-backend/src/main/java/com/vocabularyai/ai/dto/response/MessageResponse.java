package com.vocabularyai.ai.dto.response;

import com.vocabularyai.ai.entity.AiMessage;
import com.vocabularyai.ai.entity.AiMessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a single message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    
    private Long id;
    private AiMessageRole role;
    private String content;
    private LocalDateTime createdAt;
    private Integer tokensUsed;
    private Long responseTimeMs;
    private String metadata;
    
    /**
     * Convert from AiMessage entity to DTO
     */
    public static MessageResponse fromEntity(AiMessage entity) {
        return MessageResponse.builder()
            .id(entity.getId())
            .role(entity.getRole())
            .content(entity.getContent())
            .createdAt(entity.getCreatedAt())
            .tokensUsed(entity.getTokensUsed())
            .responseTimeMs(entity.getResponseTimeMs())
            .metadata(entity.getMetadata())
            .build();
    }
}
