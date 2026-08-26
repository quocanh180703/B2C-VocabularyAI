package com.vocabularyai.ai.mapper;

import com.vocabularyai.ai.dto.response.ConversationDetailResponse;
import com.vocabularyai.ai.dto.response.ConversationResponse;
import com.vocabularyai.ai.entity.AiConversation;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for AiConversation entity to DTOs
 */
@Mapper(componentModel = "spring")
public interface ConversationMapper {
    
    /**
     * Map AiConversation entity to ConversationResponse DTO
     */
    ConversationResponse toConversationResponse(AiConversation conversation);
    
    /**
     * Map AiConversation entity to ConversationDetailResponse DTO (includes message history)
     */
    ConversationDetailResponse toConversationDetailResponse(AiConversation conversation);
    
    /**
     * Map list of AiConversation entities to list of ConversationResponse DTOs
     */
    List<ConversationResponse> toConversationResponseList(List<AiConversation> conversations);
}
