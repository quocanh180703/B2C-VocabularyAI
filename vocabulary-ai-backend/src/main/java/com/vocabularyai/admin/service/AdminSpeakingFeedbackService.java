package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateSpeakingFeedbackRequest;
import com.vocabularyai.admin.dto.request.UpdateSpeakingFeedbackRequest;
import com.vocabularyai.admin.dto.response.SpeakingFeedbackDetailResponse;
import com.vocabularyai.admin.dto.response.SpeakingFeedbackListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminSpeakingFeedbackService {
    
    Page<SpeakingFeedbackListResponse> listFeedback(Pageable pageable);
    Page<SpeakingFeedbackListResponse> listBySessionId(Long sessionId, Pageable pageable);
    Page<SpeakingFeedbackListResponse> listByAttemptId(Long attemptId, Pageable pageable);
    Page<SpeakingFeedbackListResponse> searchFeedback(String searchTerm, Pageable pageable);
    
    SpeakingFeedbackDetailResponse getFeedbackDetail(Long feedbackId);
    
    SpeakingFeedbackDetailResponse createFeedback(CreateSpeakingFeedbackRequest request);
    SpeakingFeedbackDetailResponse updateFeedback(Long feedbackId, UpdateSpeakingFeedbackRequest request);
    void deleteFeedback(Long feedbackId);
}
