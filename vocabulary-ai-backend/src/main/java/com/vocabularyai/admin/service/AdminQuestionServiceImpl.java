package com.vocabularyai.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.admin.dto.request.CreateQuestionRequest;
import com.vocabularyai.admin.dto.request.UpdateQuestionRequest;
import com.vocabularyai.admin.dto.response.QuestionDetailResponse;
import com.vocabularyai.admin.dto.response.QuestionListResponse;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.quiz.entity.Question;
import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.repository.QuestionRepository;
import com.vocabularyai.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQuestionServiceImpl implements AdminQuestionService {
    
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public Page<QuestionListResponse> listQuestions(Pageable pageable) {
        log.debug("Listing all questions with pagination: {}", pageable);
        return questionRepository.findAll(pageable).map(this::toListResponse);
    }
    
    @Override
    public QuestionDetailResponse getQuestionDetail(Long questionId) {
        log.debug("Getting question detail for ID: {}", questionId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));
        
        return toDetailResponse(question);
    }
    
    @Override
    @Transactional
    public QuestionDetailResponse createQuestion(CreateQuestionRequest request) {
        log.debug("Creating new question for quiz: {}", request.getQuizId());
        
        // Verify quiz exists
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + request.getQuizId()));
        
        // Validate correctAnswer index
        if (request.getCorrectAnswer() < 0 || request.getCorrectAnswer() >= request.getOptions().size()) {
            throw new IllegalArgumentException("Correct answer index must be between 0 and " + (request.getOptions().size() - 1));
        }
        
        // Convert options list to JSON
        String optionsJson;
        try {
            optionsJson = objectMapper.writeValueAsString(request.getOptions());
        } catch (JsonProcessingException e) {
            log.error("Error converting options to JSON", e);
            throw new RuntimeException("Error processing question data", e);
        }
        
        Question question = Question.builder()
                .quiz(quiz)
                .content(request.getContent())
                .options(optionsJson)
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .build();
        
        Question saved = questionRepository.save(question);
        log.info("Created new question with ID: {}", saved.getId());
        
        return toDetailResponse(saved);
    }
    
    @Override
    @Transactional
    public QuestionDetailResponse updateQuestion(Long questionId, UpdateQuestionRequest request) {
        log.debug("Updating question with ID: {}", questionId);
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));
        
        // Update content if provided
        if (request.getContent() != null && !request.getContent().isBlank()) {
            question.setContent(request.getContent());
        }
        
        // Update options if provided
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            Integer correctAnswerToUse = request.getCorrectAnswer() != null ? 
                    request.getCorrectAnswer() : question.getCorrectAnswer();
            
            if (correctAnswerToUse < 0 || correctAnswerToUse >= request.getOptions().size()) {
                throw new IllegalArgumentException("Correct answer index must be between 0 and " + (request.getOptions().size() - 1));
            }
            
            try {
                String optionsJson = objectMapper.writeValueAsString(request.getOptions());
                question.setOptions(optionsJson);
                
                if (request.getCorrectAnswer() != null) {
                    question.setCorrectAnswer(request.getCorrectAnswer());
                }
            } catch (JsonProcessingException e) {
                log.error("Error converting options to JSON", e);
                throw new RuntimeException("Error processing question data", e);
            }
        } else if (request.getCorrectAnswer() != null) {
            List<String> currentOptions = parseOptions(question.getOptions());
            if (request.getCorrectAnswer() < 0 || request.getCorrectAnswer() >= currentOptions.size()) {
                throw new IllegalArgumentException("Correct answer index must be between 0 and " + (currentOptions.size() - 1));
            }
            question.setCorrectAnswer(request.getCorrectAnswer());
        }
        
        // Update explanation if provided
        if (request.getExplanation() != null) {
            question.setExplanation(request.getExplanation());
        }
        
        Question updated = questionRepository.save(question);
        log.info("Updated question with ID: {}", updated.getId());
        
        return toDetailResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        log.debug("Deleting question with ID: {}", questionId);
        
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question not found: " + questionId);
        }
        
        questionRepository.deleteById(questionId);
        log.info("Deleted question with ID: {}", questionId);
    }
    
    // Helper methods
    
    private List<String> parseOptions(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Error parsing options JSON", e);
            return List.of();
        }
    }
    
    private QuestionDetailResponse toDetailResponse(Question question) {
        List<String> options = parseOptions(question.getOptions());
        String correctOptionText = question.getCorrectAnswer() >= 0 && 
                question.getCorrectAnswer() < options.size() ? 
                options.get(question.getCorrectAnswer()) : "";
        
        return QuestionDetailResponse.builder()
                .id(question.getId())
                .quizId(question.getQuiz().getId())
                .content(question.getContent())
                .options(options)
                .correctAnswer(question.getCorrectAnswer())
                .correctOptionText(correctOptionText)
                .explanation(question.getExplanation())
                .createdAt(question.getCreatedAt())
                .build();
    }
    
    private QuestionListResponse toListResponse(Question question) {
        List<String> options = parseOptions(question.getOptions());
        String contentPreview = question.getContent();
        if (contentPreview != null && contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 100) + "...";
        }
        
        return QuestionListResponse.builder()
                .id(question.getId())
                .quizId(question.getQuiz().getId())
                .contentPreview(contentPreview)
                .optionCount(options.size())
                .options(options)
                .correctAnswer(question.getCorrectAnswer())
                .createdAt(question.getCreatedAt())
                .build();
    }
}
