package com.vocabularyai.speaking.mapper;

import com.vocabularyai.speaking.dto.response.SpeakingFeedbackResponse;
import com.vocabularyai.speaking.entity.SpeakingFeedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct Mapper cho SpeakingFeedback
 * 
 * Ánh xạ giữa SpeakingFeedback entity và Response DTOs
 */
@Mapper(componentModel = "spring")
public interface SpeakingFeedbackMapper {
    
    /**
     * Ánh xạ SpeakingFeedback entity thành SpeakingFeedbackResponse DTO
     * 
     * @param feedback Entity
     * @return Response DTO
     */
    @Mapping(target = "attemptId", source = "attempt.id")
    @Mapping(target = "sessionId", source = "session.id")
    SpeakingFeedbackResponse toResponse(SpeakingFeedback feedback);
}
