package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateSpeakingSessionRequest;
import com.vocabularyai.admin.dto.request.UpdateSpeakingSessionRequest;
import com.vocabularyai.admin.dto.response.SpeakingSessionDetailResponse;
import com.vocabularyai.admin.dto.response.SpeakingSessionListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminSpeakingSessionService {
    
    Page<SpeakingSessionListResponse> listSessions(Pageable pageable);
    Page<SpeakingSessionListResponse> listByLevel(String level, Pageable pageable);
    Page<SpeakingSessionListResponse> listByStatus(String status, Pageable pageable);
    Page<SpeakingSessionListResponse> searchSessions(String searchTerm, Pageable pageable);
    
    SpeakingSessionDetailResponse getSessionDetail(Long sessionId);
    
    SpeakingSessionDetailResponse createSession(CreateSpeakingSessionRequest request);
    SpeakingSessionDetailResponse updateSession(Long sessionId, UpdateSpeakingSessionRequest request);
    void deleteSession(Long sessionId);
}
