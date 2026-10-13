package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateLearningProgressRequest;
import com.vocabularyai.admin.dto.request.UpdateLearningProgressRequest;
import com.vocabularyai.admin.dto.response.LearningProgressDetailResponse;
import com.vocabularyai.admin.dto.response.LearningProgressListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminLearningProgressService {
    
    Page<LearningProgressListResponse> listProgress(Pageable pageable);
    LearningProgressDetailResponse getProgressDetail(Long progressId);
    LearningProgressDetailResponse createProgress(CreateLearningProgressRequest request);
    LearningProgressDetailResponse updateProgress(Long progressId, UpdateLearningProgressRequest request);
    void deleteProgress(Long progressId);
}
