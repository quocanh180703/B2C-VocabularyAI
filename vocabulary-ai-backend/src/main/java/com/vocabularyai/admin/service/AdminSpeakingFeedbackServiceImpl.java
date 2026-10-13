package com.vocabularyai.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.admin.dto.request.CreateSpeakingFeedbackRequest;
import com.vocabularyai.admin.dto.request.UpdateSpeakingFeedbackRequest;
import com.vocabularyai.admin.dto.response.SpeakingFeedbackDetailResponse;
import com.vocabularyai.admin.dto.response.SpeakingFeedbackListResponse;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import com.vocabularyai.speaking.entity.SpeakingFeedback;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.repository.SpeakingAttemptRepository;
import com.vocabularyai.speaking.repository.SpeakingFeedbackRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSpeakingFeedbackServiceImpl implements AdminSpeakingFeedbackService {
    
    private final SpeakingFeedbackRepository feedbackRepository;
    private final SpeakingSessionRepository sessionRepository;
    private final SpeakingAttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public Page<SpeakingFeedbackListResponse> listFeedback(Pageable pageable) {
        log.debug("Listing all speaking feedback with pagination: {}", pageable);
        return feedbackRepository.findAll(pageable).map(this::toListResponse);
    }
    
    @Override
    public Page<SpeakingFeedbackListResponse> listBySessionId(Long sessionId, Pageable pageable) {
        log.debug("Listing feedback by session ID: {} with pagination: {}", sessionId, pageable);
        // Verify session exists
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        
        return feedbackRepository.findAllAttemptFeedbackBySessionIdPaginated(sessionId, pageable)
                .map(this::toListResponse);
    }
    
    @Override
    public Page<SpeakingFeedbackListResponse> listByAttemptId(Long attemptId, Pageable pageable) {
        log.debug("Listing feedback by attempt ID: {}", attemptId);
        // Verify attempt exists
        attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));
        
        // Since there's at most one feedback per attempt, return single page
        return feedbackRepository.findByAttemptId(attemptId)
                .map(fb -> Page.empty(pageable).map(p -> toListResponse(fb)))
                .orElse(Page.empty(pageable));
    }
    
    @Override
    public Page<SpeakingFeedbackListResponse> searchFeedback(String searchTerm, Pageable pageable) {
        log.debug("Searching feedback by term: {} with pagination: {}", searchTerm, pageable);
        // Simple search in feedback text - get all then filter
        List<SpeakingFeedback> all = feedbackRepository.findAll();
        List<SpeakingFeedbackListResponse> filtered = all.stream()
                .filter(f -> f.getFeedback() != null && f.getFeedback().toLowerCase().contains(searchTerm.toLowerCase()))
                .map(this::toListResponse)
                .collect(Collectors.toList());
        
        int total = filtered.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);
        List<SpeakingFeedbackListResponse> pageContent = filtered.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, total);
    }
    
    @Override
    public SpeakingFeedbackDetailResponse getFeedbackDetail(Long feedbackId) {
        log.debug("Getting feedback detail for ID: {}", feedbackId);
        SpeakingFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found: " + feedbackId));
        
        return toDetailResponse(feedback);
    }
    
    @Override
    @Transactional
    public SpeakingFeedbackDetailResponse createFeedback(CreateSpeakingFeedbackRequest request) {
        log.debug("Creating new speaking feedback");
        
        // Validate that either sessionId or attemptId is provided
        if ((request.getSessionId() == null || request.getSessionId() <= 0) &&
                (request.getAttemptId() == null || request.getAttemptId() <= 0)) {
            throw new IllegalArgumentException("Either sessionId or attemptId must be provided");
        }
        
        // Validate that not both are provided
        if ((request.getSessionId() != null && request.getSessionId() > 0) &&
                (request.getAttemptId() != null && request.getAttemptId() > 0)) {
            throw new IllegalArgumentException("Cannot provide both sessionId and attemptId");
        }
        
        SpeakingFeedback feedback = SpeakingFeedback.builder()
                .overallScore(request.getOverallScore())
                .grammarScore(request.getGrammarScore())
                .vocabularyScore(request.getVocabularyScore())
                .fluencyScore(request.getFluencyScore())
                .contentScore(request.getContentScore())
                .pronunciationScore(request.getPronunciationScore())
                .feedback(request.getFeedback())
                .build();
        
        // Set session or attempt
        if (request.getSessionId() != null && request.getSessionId() > 0) {
            SpeakingSession session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + request.getSessionId()));
            feedback.setSession(session);
        } else if (request.getAttemptId() != null && request.getAttemptId() > 0) {
            SpeakingAttempt attempt = attemptRepository.findById(request.getAttemptId())
                    .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + request.getAttemptId()));
            feedback.setAttempt(attempt);
        }
        
        // Convert JSON fields
        try {
            if (request.getCorrections() != null && !request.getCorrections().isEmpty()) {
                feedback.setCorrections(objectMapper.writeValueAsString(request.getCorrections()));
            }
            if (request.getSuggestions() != null && !request.getSuggestions().isEmpty()) {
                feedback.setSuggestions(objectMapper.writeValueAsString(request.getSuggestions()));
            }
            if (request.getStrengths() != null && !request.getStrengths().isEmpty()) {
                feedback.setStrengths(objectMapper.writeValueAsString(request.getStrengths()));
            }
            if (request.getWeaknesses() != null && !request.getWeaknesses().isEmpty()) {
                feedback.setWeaknesses(objectMapper.writeValueAsString(request.getWeaknesses()));
            }
        } catch (JsonProcessingException e) {
            log.error("Error converting request objects to JSON", e);
            throw new RuntimeException("Error processing feedback data", e);
        }
        
        SpeakingFeedback saved = feedbackRepository.save(feedback);
        log.info("Created new speaking feedback with ID: {}", saved.getId());
        
        return toDetailResponse(saved);
    }
    
    @Override
    @Transactional
    public SpeakingFeedbackDetailResponse updateFeedback(Long feedbackId, UpdateSpeakingFeedbackRequest request) {
        log.debug("Updating speaking feedback with ID: {}", feedbackId);
        
        SpeakingFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found: " + feedbackId));
        
        // Update scores
        if (request.getOverallScore() != null) {
            feedback.setOverallScore(request.getOverallScore());
        }
        if (request.getGrammarScore() != null) {
            feedback.setGrammarScore(request.getGrammarScore());
        }
        if (request.getVocabularyScore() != null) {
            feedback.setVocabularyScore(request.getVocabularyScore());
        }
        if (request.getFluencyScore() != null) {
            feedback.setFluencyScore(request.getFluencyScore());
        }
        if (request.getContentScore() != null) {
            feedback.setContentScore(request.getContentScore());
        }
        if (request.getPronunciationScore() != null) {
            feedback.setPronunciationScore(request.getPronunciationScore());
        }
        
        // Update feedback text
        if (request.getFeedback() != null) {
            feedback.setFeedback(request.getFeedback());
        }
        
        // Update JSON fields
        try {
            if (request.getCorrections() != null) {
                feedback.setCorrections(request.getCorrections().isEmpty() ? 
                        null : objectMapper.writeValueAsString(request.getCorrections()));
            }
            if (request.getSuggestions() != null) {
                feedback.setSuggestions(request.getSuggestions().isEmpty() ? 
                        null : objectMapper.writeValueAsString(request.getSuggestions()));
            }
            if (request.getStrengths() != null) {
                feedback.setStrengths(request.getStrengths().isEmpty() ? 
                        null : objectMapper.writeValueAsString(request.getStrengths()));
            }
            if (request.getWeaknesses() != null) {
                feedback.setWeaknesses(request.getWeaknesses().isEmpty() ? 
                        null : objectMapper.writeValueAsString(request.getWeaknesses()));
            }
        } catch (JsonProcessingException e) {
            log.error("Error converting request objects to JSON", e);
            throw new RuntimeException("Error processing feedback data", e);
        }
        
        SpeakingFeedback updated = feedbackRepository.save(feedback);
        log.info("Updated speaking feedback with ID: {}", updated.getId());
        
        return toDetailResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        log.debug("Deleting speaking feedback with ID: {}", feedbackId);
        
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new ResourceNotFoundException("Feedback not found: " + feedbackId);
        }
        
        feedbackRepository.deleteById(feedbackId);
        log.info("Deleted speaking feedback with ID: {}", feedbackId);
    }
    
    // Helper methods for conversion
    
    private SpeakingFeedbackDetailResponse toDetailResponse(SpeakingFeedback feedback) {
        try {
            List<SpeakingFeedbackDetailResponse.CorrectionItem> corrections = null;
            if (feedback.getCorrections() != null) {
                corrections = objectMapper.readValue(feedback.getCorrections(),
                        new TypeReference<List<SpeakingFeedbackDetailResponse.CorrectionItem>>() {});
            }
            
            List<SpeakingFeedbackDetailResponse.SuggestionItem> suggestions = null;
            if (feedback.getSuggestions() != null) {
                suggestions = objectMapper.readValue(feedback.getSuggestions(),
                        new TypeReference<List<SpeakingFeedbackDetailResponse.SuggestionItem>>() {});
            }
            
            List<SpeakingFeedbackDetailResponse.StrengthItem> strengths = null;
            if (feedback.getStrengths() != null) {
                strengths = objectMapper.readValue(feedback.getStrengths(),
                        new TypeReference<List<SpeakingFeedbackDetailResponse.StrengthItem>>() {});
            }
            
            List<SpeakingFeedbackDetailResponse.WeaknessItem> weaknesses = null;
            if (feedback.getWeaknesses() != null) {
                weaknesses = objectMapper.readValue(feedback.getWeaknesses(),
                        new TypeReference<List<SpeakingFeedbackDetailResponse.WeaknessItem>>() {});
            }
            
            return SpeakingFeedbackDetailResponse.builder()
                    .id(feedback.getId())
                    .sessionId(feedback.getSession() != null ? feedback.getSession().getId() : null)
                    .attemptId(feedback.getAttempt() != null ? feedback.getAttempt().getId() : null)
                    .overallScore(feedback.getOverallScore())
                    .grammarScore(feedback.getGrammarScore())
                    .vocabularyScore(feedback.getVocabularyScore())
                    .fluencyScore(feedback.getFluencyScore())
                    .contentScore(feedback.getContentScore())
                    .pronunciationScore(feedback.getPronunciationScore())
                    .feedback(feedback.getFeedback())
                    .corrections(corrections)
                    .suggestions(suggestions)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .createdAt(feedback.getCreatedAt())
                    .updatedAt(feedback.getUpdatedAt())
                    .build();
        } catch (JsonProcessingException e) {
            log.warn("Error parsing JSON fields for feedback ID: {}", feedback.getId(), e);
            return SpeakingFeedbackDetailResponse.builder()
                    .id(feedback.getId())
                    .sessionId(feedback.getSession() != null ? feedback.getSession().getId() : null)
                    .attemptId(feedback.getAttempt() != null ? feedback.getAttempt().getId() : null)
                    .overallScore(feedback.getOverallScore())
                    .grammarScore(feedback.getGrammarScore())
                    .vocabularyScore(feedback.getVocabularyScore())
                    .fluencyScore(feedback.getFluencyScore())
                    .contentScore(feedback.getContentScore())
                    .pronunciationScore(feedback.getPronunciationScore())
                    .feedback(feedback.getFeedback())
                    .createdAt(feedback.getCreatedAt())
                    .updatedAt(feedback.getUpdatedAt())
                    .build();
        }
    }
    
    private SpeakingFeedbackListResponse toListResponse(SpeakingFeedback feedback) {
        String feedbackSummary = feedback.getFeedback();
        if (feedbackSummary != null && feedbackSummary.length() > 100) {
            feedbackSummary = feedbackSummary.substring(0, 100) + "...";
        }
        
        String feedbackType = feedback.isAttemptFeedback() ? "ATTEMPT" : "SESSION";
        
        return SpeakingFeedbackListResponse.builder()
                .id(feedback.getId())
                .sessionId(feedback.getSession() != null ? feedback.getSession().getId() : null)
                .attemptId(feedback.getAttempt() != null ? feedback.getAttempt().getId() : null)
                .overallScore(feedback.getOverallScore())
                .grammarScore(feedback.getGrammarScore())
                .vocabularyScore(feedback.getVocabularyScore())
                .fluencyScore(feedback.getFluencyScore())
                .contentScore(feedback.getContentScore())
                .pronunciationScore(feedback.getPronunciationScore())
                .feedbackSummary(feedbackSummary)
                .feedbackType(feedbackType)
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
