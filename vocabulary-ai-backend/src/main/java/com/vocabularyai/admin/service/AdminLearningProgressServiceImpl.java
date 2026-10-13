package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateLearningProgressRequest;
import com.vocabularyai.admin.dto.request.UpdateLearningProgressRequest;
import com.vocabularyai.admin.dto.response.LearningProgressDetailResponse;
import com.vocabularyai.admin.dto.response.LearningProgressListResponse;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.entity.LearningProgress.LearningStatus;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLearningProgressServiceImpl implements AdminLearningProgressService {
    
    private final LearningProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    
    @Override
    public Page<LearningProgressListResponse> listProgress(Pageable pageable) {
        log.debug("Listing all learning progress with pagination: {}", pageable);
        return progressRepository.findAll(pageable).map(this::toListResponse);
    }
    
    @Override
    public LearningProgressDetailResponse getProgressDetail(Long progressId) {
        log.debug("Getting learning progress detail for ID: {}", progressId);
        LearningProgress progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning progress not found: " + progressId));
        
        return toDetailResponse(progress);
    }
    
    @Override
    @Transactional
    public LearningProgressDetailResponse createProgress(CreateLearningProgressRequest request) {
        log.debug("Creating new learning progress for user: {} word: {}", request.getUserId(), request.getWordId());
        
        // Verify user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
        
        // Verify word exists
        Word word = wordRepository.findById(request.getWordId())
                .orElseThrow(() -> new ResourceNotFoundException("Word not found: " + request.getWordId()));
        
        // Check for duplicate progress
        progressRepository.findByUserIdAndWordId(request.getUserId(), request.getWordId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Learning progress already exists for this user and word");
                });
        
        LearningProgress progress = LearningProgress.builder()
                .user(user)
                .word(word)
                .status(LearningStatus.valueOf(request.getStatus().toUpperCase()))
                .correctCount(request.getCorrectCount() != null ? request.getCorrectCount() : 0)
                .wrongCount(request.getWrongCount() != null ? request.getWrongCount() : 0)
                .reviewCount(request.getReviewCount() != null ? request.getReviewCount() : 0)
                .lastReviewedAt(request.getLastReviewedAt())
                .nextReviewAt(request.getNextReviewAt())
                .build();
        
        LearningProgress saved = progressRepository.save(progress);
        log.info("Created new learning progress with ID: {}", saved.getId());
        
        return toDetailResponse(saved);
    }
    
    @Override
    @Transactional
    public LearningProgressDetailResponse updateProgress(Long progressId, UpdateLearningProgressRequest request) {
        log.debug("Updating learning progress with ID: {}", progressId);
        
        LearningProgress progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning progress not found: " + progressId));
        
        // Update status
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                progress.setStatus(LearningStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + request.getStatus());
            }
        }
        
        // Update counts
        if (request.getCorrectCount() != null) {
            progress.setCorrectCount(request.getCorrectCount());
        }
        if (request.getWrongCount() != null) {
            progress.setWrongCount(request.getWrongCount());
        }
        if (request.getReviewCount() != null) {
            progress.setReviewCount(request.getReviewCount());
        }
        
        // Update timestamps
        if (request.getLastReviewedAt() != null) {
            progress.setLastReviewedAt(request.getLastReviewedAt());
        }
        if (request.getNextReviewAt() != null) {
            progress.setNextReviewAt(request.getNextReviewAt());
        }
        
        LearningProgress updated = progressRepository.save(progress);
        log.info("Updated learning progress with ID: {}", updated.getId());
        
        return toDetailResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteProgress(Long progressId) {
        log.debug("Deleting learning progress with ID: {}", progressId);
        
        if (!progressRepository.existsById(progressId)) {
            throw new ResourceNotFoundException("Learning progress not found: " + progressId);
        }
        
        progressRepository.deleteById(progressId);
        log.info("Deleted learning progress with ID: {}", progressId);
    }
    
    // Helper methods
    
    private LearningProgressDetailResponse toDetailResponse(LearningProgress progress) {
        boolean needsReview = progress.needsReview();
        
        return LearningProgressDetailResponse.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .wordId(progress.getWord().getId())
                .wordText(progress.getWord().getWord())
                .status(progress.getStatus().name())
                .correctCount(progress.getCorrectCount())
                .wrongCount(progress.getWrongCount())
                .reviewCount(progress.getReviewCount())
                .accuracy(Math.round(progress.getAccuracy() * 100.0) / 100.0)
                .lastReviewedAt(progress.getLastReviewedAt())
                .nextReviewAt(progress.getNextReviewAt())
                .needsReview(needsReview)
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
    
    private LearningProgressListResponse toListResponse(LearningProgress progress) {
        boolean needsReview = progress.needsReview();
        
        return LearningProgressListResponse.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .wordId(progress.getWord().getId())
                .wordText(progress.getWord().getWord())
                .status(progress.getStatus().name())
                .correctCount(progress.getCorrectCount())
                .wrongCount(progress.getWrongCount())
                .reviewCount(progress.getReviewCount())
                .accuracy(Math.round(progress.getAccuracy() * 100.0) / 100.0)
                .needsReview(needsReview)
                .nextReviewAt(progress.getNextReviewAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}
