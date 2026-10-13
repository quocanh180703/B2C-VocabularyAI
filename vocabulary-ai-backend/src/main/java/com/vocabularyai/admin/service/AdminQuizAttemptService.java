package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateQuizAttemptRequest;
import com.vocabularyai.admin.dto.request.UpdateQuizAttemptRequest;
import com.vocabularyai.admin.dto.response.QuizAttemptDetailResponse;
import com.vocabularyai.admin.dto.response.QuizAttemptListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminQuizAttemptService {
    
    Page<QuizAttemptListResponse> listAttempts(Pageable pageable);
    QuizAttemptDetailResponse getAttemptDetail(Long attemptId);
    QuizAttemptDetailResponse createAttempt(CreateQuizAttemptRequest request);
    QuizAttemptDetailResponse updateAttempt(Long attemptId, UpdateQuizAttemptRequest request);
    void deleteAttempt(Long attemptId);
}
