package com.vocabularyai.ai.mapper;

import com.vocabularyai.ai.dto.response.MessageResponse;
import com.vocabularyai.ai.entity.AiMessage;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for AiMessage entity to MessageResponse DTO
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {
    
    /**
     * Map AiMessage entity to MessageResponse DTO
     */
    MessageResponse toMessageResponse(AiMessage message);
    
    /**
     * Map list of AiMessage entities to list of MessageResponse DTOs
     */
    List<MessageResponse> toMessageResponseList(List<AiMessage> messages);
}
