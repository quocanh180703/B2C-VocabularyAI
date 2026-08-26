package com.vocabularyai.speaking.mapper;

import com.vocabularyai.speaking.dto.response.SpeakingAttemptResponse;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct Mapper cho SpeakingAttempt
 * 
 * Ánh xạ giữa SpeakingAttempt entity và Response DTOs
 */
@Mapper(componentModel = "spring", uses = SpeakingFeedbackMapper.class)
public interface SpeakingAttemptMapper {
    
    /**
     * Ánh xạ SpeakingAttempt entity thành SpeakingAttemptResponse DTO
     * 
     * @param attempt Entity
     * @return Response DTO
     */
    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "feedback", source = "feedback")
    SpeakingAttemptResponse toResponse(SpeakingAttempt attempt);
}
