package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.response.SpeakingFeedbackResponse;
import com.vocabularyai.speaking.entity.SpeakingFeedback;
import com.vocabularyai.speaking.mapper.SpeakingFeedbackMapper;
import com.vocabularyai.speaking.repository.SpeakingFeedbackRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SpeakingFeedbackService Implementation
 * 
 * Quản lý feedback AI cho speaking attempts:
 * - Lấy feedback
 * - Tính toán điểm số trung bình
 * - Thống kê tiến độ học
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpeakingFeedbackServiceImpl implements SpeakingFeedbackService {
    
    private final SpeakingFeedbackRepository feedbackRepository;
    private final SpeakingSessionRepository sessionRepository;
    private final SpeakingFeedbackMapper feedbackMapper;
    
    @Override
    @Transactional(readOnly = true)
    public SpeakingFeedbackResponse getAttemptFeedback(Long attemptId, Long userId) {
        log.debug("Lấy feedback của attempt: attemptId={}, userId={}", attemptId, userId);
        
        SpeakingFeedback feedback = feedbackRepository.findByAttemptId(attemptId)
                .orElseThrow(() -> com.vocabularyai.common.exception.ResourceNotFoundException.of("SpeakingFeedback", "attemptId", attemptId));
        
        // ✅ TASK 9: Verify user ownership - throw ForbiddenException for 403
        Long sessionId = feedback.getAttempt().getSession().getId();
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            log.warn("IDOR prevention: Unauthorized access attempt: attemptId={}, userId={}", attemptId, userId);
            throw new com.vocabularyai.common.exception.ForbiddenException("You don't have permission to access this feedback");
        }
        
        return feedbackMapper.toResponse(feedback);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<SpeakingFeedbackResponse> getSessionFeedback(Long sessionId, Long userId) {
        log.debug("Lấy feedback của phiên: sessionId={}, userId={}", sessionId, userId);
        
        // ✅ TASK 9: Verify session ownership - throw ForbiddenException for 403
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            log.warn("IDOR prevention: Unauthorized access attempt: sessionId={}, userId={}", sessionId, userId);
            throw new com.vocabularyai.common.exception.ForbiddenException("You don't have permission to access this session feedback");
        }
        
        return feedbackRepository.findBySessionId(sessionId)
                .map(feedbackMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingFeedbackResponse> listFeedbackBySession(Long sessionId, Long userId, Pageable pageable) {
        log.debug("Lấy danh sách feedback của phiên: sessionId={}, userId={}", sessionId, userId);
        
        // ✅ TASK 9: Verify session ownership - throw ForbiddenException for 403
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            log.warn("IDOR prevention: Unauthorized access attempt: sessionId={}, userId={}", sessionId, userId);
            throw new com.vocabularyai.common.exception.ForbiddenException("You don't have permission to access this session");
        }
        
        return feedbackRepository.findAllAttemptFeedbackBySessionIdPaginated(sessionId, pageable)
                .map(feedbackMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingFeedbackResponse> listFeedbackBySessionSortedByScore(Long sessionId, Long userId, Pageable pageable) {
        log.debug("Lấy feedback sắp xếp theo điểm tổng: sessionId={}, userId={}", sessionId, userId);
        
        // ✅ TASK 9: Verify session ownership - throw ForbiddenException for 403
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            log.warn("IDOR prevention: Unauthorized access attempt: sessionId={}, userId={}", sessionId, userId);
            throw new com.vocabularyai.common.exception.ForbiddenException("You don't have permission to access this session");
        }
        
        return feedbackRepository.findAttemptFeedbackBySessionIdOrderByOverallScore(sessionId, pageable)
                .map(feedbackMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingFeedbackResponse> listFeedbackBySessionSortedByGrammar(Long sessionId, Long userId, Pageable pageable) {
        log.debug("Lấy feedback sắp xếp theo điểm ngữ pháp: sessionId={}, userId={}", sessionId, userId);
        
        // ✅ TASK 9: Verify session ownership - throw ForbiddenException for 403
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            log.warn("IDOR prevention: Unauthorized access attempt: sessionId={}, userId={}", sessionId, userId);
            throw new com.vocabularyai.common.exception.ForbiddenException("You don't have permission to access this session");
        }
        
        return feedbackRepository.findAttemptFeedbackBySessionIdOrderByGrammarScore(sessionId, pageable)
                .map(feedbackMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingFeedbackResponse> listFeedbackBySessionSortedByVocabulary(Long sessionId, Long userId, Pageable pageable) {
        log.debug("Lấy feedback sắp xếp theo điểm từ vựng: sessionId={}, userId={}", sessionId, userId);
        
        // ✅ TASK 9: Verify session ownership - throw ForbiddenException for 403
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            log.warn("IDOR prevention: Unauthorized access attempt: sessionId={}, userId={}", sessionId, userId);
            throw new com.vocabularyai.common.exception.ForbiddenException("You don't have permission to access this session");
        }
        
        return feedbackRepository.findAttemptFeedbackBySessionIdOrderByVocabularyScore(sessionId, pageable)
                .map(feedbackMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingFeedbackResponse> listFeedbackByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Lấy feedback trong khoảng thời gian: startDate={}, endDate={}", startDate, endDate);
        
        return feedbackRepository.findByCreatedAtBetween(startDate, endDate, pageable)
                .map(feedbackMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageOverallScore(Long sessionId) {
        log.debug("Tính điểm trung bình tổng thể: sessionId={}", sessionId);
        
        return feedbackRepository.calculateAverageScoreBySessionId(sessionId)
                .orElse(0.0);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageGrammarScore(Long sessionId) {
        log.debug("Tính điểm ngữ pháp trung bình: sessionId={}", sessionId);
        
        return feedbackRepository.calculateAverageGrammarScoreBySessionId(sessionId)
                .orElse(0.0);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageVocabularyScore(Long sessionId) {
        log.debug("Tính điểm từ vựng trung bình: sessionId={}", sessionId);
        
        return feedbackRepository.calculateAverageVocabularyScoreBySessionId(sessionId)
                .orElse(0.0);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageFluencyScore(Long sessionId) {
        log.debug("Tính điểm trôi chảy trung bình: sessionId={}", sessionId);
        
        return feedbackRepository.calculateAverageFluencyScoreBySessionId(sessionId)
                .orElse(0.0);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageContentScore(Long sessionId) {
        log.debug("Tính điểm nội dung trung bình: sessionId={}", sessionId);
        
        return feedbackRepository.calculateAverageContentScoreBySessionId(sessionId)
                .orElse(0.0);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<SpeakingFeedbackResponse> getBestFeedbackInSession(Long sessionId, Long userId) {
        log.debug("Lấy feedback tốt nhất: sessionId={}, userId={}", sessionId, userId);
        
        // ✅ TASK 9: Verify session ownership - throw ForbiddenException for 403
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            log.warn("IDOR prevention: Unauthorized access attempt: sessionId={}, userId={}", sessionId, userId);
            throw new com.vocabularyai.common.exception.ForbiddenException("You don't have permission to access this session");
        }
        
        // Lấy trang đầu tiên với 1 item (sắp xếp theo điểm cao nhất)
        Page<SpeakingFeedback> page = feedbackRepository.findAttemptFeedbackBySessionIdOrderByOverallScore(
                sessionId,
                org.springframework.data.domain.PageRequest.of(0, 1)
        );
        
        return page.stream()
                .findFirst()
                .map(feedbackMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countFeedbackBySession(Long sessionId) {
        return feedbackRepository.countByAttemptSessionId(sessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasAttemptFeedback(Long attemptId) {
        return feedbackRepository.existsByAttemptId(attemptId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasSessionFeedback(Long sessionId) {
        return feedbackRepository.existsBySessionId(sessionId);
    }
}
