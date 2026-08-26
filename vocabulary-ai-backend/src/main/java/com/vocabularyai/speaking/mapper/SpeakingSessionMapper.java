package com.vocabularyai.speaking.mapper;

import com.vocabularyai.speaking.dto.response.SpeakingAttemptResponse;
import com.vocabularyai.speaking.dto.response.SpeakingSessionResponse;
import com.vocabularyai.speaking.dto.response.SpeakingSessionSummaryResponse;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import com.vocabularyai.speaking.entity.SpeakingSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct Mapper cho SpeakingSession
 * 
 * Ánh xạ giữa SpeakingSession entity và Response DTOs
 */
@Mapper(componentModel = "spring", uses = SpeakingAttemptMapper.class)
public interface SpeakingSessionMapper {
    
    /**
     * Ánh xạ SpeakingSession entity thành SpeakingSessionResponse DTO
     * 
     * @param session Entity
     * @return Response DTO
     */
    @Mapping(target = "attemptCount", expression = "java(session.getAttemptCount())")
    SpeakingSessionResponse toResponse(SpeakingSession session);
    
    /**
     * Ánh xạ danh sách attempts thành danh sách response
     * 
     * @param attempts Danh sách attempts
     * @return Danh sách response
     */
    List<SpeakingAttemptResponse> toAttemptResponses(List<SpeakingAttempt> attempts);
}
