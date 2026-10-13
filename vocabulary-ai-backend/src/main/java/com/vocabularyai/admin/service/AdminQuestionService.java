package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateQuestionRequest;
import com.vocabularyai.admin.dto.request.UpdateQuestionRequest;
import com.vocabularyai.admin.dto.response.QuestionDetailResponse;
import com.vocabularyai.admin.dto.response.QuestionListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminQuestionService {
    
    Page<QuestionListResponse> listQuestions(Pageable pageable);
    QuestionDetailResponse getQuestionDetail(Long questionId);
    QuestionDetailResponse createQuestion(CreateQuestionRequest request);
    QuestionDetailResponse updateQuestion(Long questionId, UpdateQuestionRequest request);
    void deleteQuestion(Long questionId);
}
