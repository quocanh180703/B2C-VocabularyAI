package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.request.CreateSpeakingSessionRequest;
import com.vocabularyai.speaking.dto.response.SpeakingAttemptResponse;
import com.vocabularyai.speaking.dto.response.SpeakingFeedbackResponse;
import com.vocabularyai.speaking.dto.response.SpeakingSessionResponse;
import com.vocabularyai.speaking.dto.response.SpeakingSessionSummaryResponse;
import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import com.vocabularyai.speaking.mapper.SpeakingSessionMapper;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * SpeakingSessionService Implementation
 * 
 * Quản lý phiên luyện nói:
 * - Tạo phiên mới
 * - Lấy chi tiết phiên
 * - Cập nhật trạng thái phiên
 * - Tính điểm tổng thể từ attempts
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpeakingSessionServiceImpl implements SpeakingSessionService {
    
    private final SpeakingSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SpeakingSessionMapper sessionMapper;
    private final SpeakingAttemptService attemptService;
    private final SpeakingFeedbackService feedbackService;
    
    @Override
    public SpeakingSessionResponse createSession(Long userId, CreateSpeakingSessionRequest request) {
        log.info("Tạo phiên luyện nói mới cho user: {}", userId);
        
        // Xác minh user tồn tại
        User user = verifyUserExists(userId);
        
        // Tạo session entity
        SpeakingSession session = SpeakingSession.builder()
                .user(user)
                .topic(request.getTopic())
                .level(request.getLevel())
                .description(request.getDescription())
                .status(SpeakingStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        
        // Lưu vào database
        SpeakingSession savedSession = sessionRepository.save(session);
        log.info("Phiên luyện nói được tạo thành công: sessionId={}", savedSession.getId());
        
        // Chuyển đổi sang response DTO
        return sessionMapper.toResponse(savedSession);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SpeakingSessionSummaryResponse getSessionDetail(Long sessionId, Long userId) {
        log.info("Lấy chi tiết phiên: sessionId={}, userId={}", sessionId, userId);
        
        // Verify user ownership
        SpeakingSession session = getSessionAndVerifyOwnership(sessionId, userId);
        
        // Lấy attempts và feedback (fetch từ DB, không cache vì đây là read)
        // MapStruct sẽ tự động populate nested collections
        
        // Lấy feedback phiên (nếu có)
        SpeakingFeedbackResponse sessionFeedback = feedbackService
                .getSessionFeedback(sessionId, userId)
                .orElse(null);
        
        // Tạo summary response
        return SpeakingSessionSummaryResponse.builder()
                .id(session.getId())
                .topic(session.getTopic())
                .level(session.getLevel())
                .description(session.getDescription())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .overallScore(session.getOverallScore())
                .attemptCount(session.getAttemptCount())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .attempts(sessionMapper.toAttemptResponses(session.getAttempts()))
                .feedback(sessionFeedback)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingSessionResponse> listSessions(Long userId, Pageable pageable) {
        log.debug("Lấy danh sách phiên của user: userId={}", userId);
        verifyUserExists(userId);
        return sessionRepository.findByUserId(userId, pageable)
                .map(sessionMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingSessionResponse> listSessionsByStatus(Long userId, SpeakingStatus status, Pageable pageable) {
        log.debug("Lấy phiên theo trạng thái: userId={}, status={}", userId, status);
        verifyUserExists(userId);
        return sessionRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(sessionMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingSessionResponse> listSessionsByLevel(Long userId, SpeakingLevel level, Pageable pageable) {
        log.debug("Lấy phiên theo mức độ: userId={}, level={}", userId, level);
        verifyUserExists(userId);
        return sessionRepository.findByUserIdAndLevel(userId, level, pageable)
                .map(sessionMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SpeakingSessionResponse getLatestInProgressSession(Long userId) {
        log.debug("Lấy phiên gần nhất chưa hoàn thành của user: userId={}", userId);
        verifyUserExists(userId);
        
        return sessionRepository
                .findLatestByUserIdAndStatus(userId, SpeakingStatus.IN_PROGRESS)
                .map(sessionMapper::toResponse)
                .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingSessionResponse> listSessionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Lấy phiên trong khoảng thời gian: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        verifyUserExists(userId);
        return sessionRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate, pageable)
                .map(sessionMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingSessionResponse> listCompletedSessionsSortedByScore(Long userId, Pageable pageable) {
        log.debug("Lấy phiên hoàn thành sắp xếp theo điểm: userId={}", userId);
        verifyUserExists(userId);
        return sessionRepository.findCompletedSessionsByUserIdOrderByScore(userId, pageable)
                .map(sessionMapper::toResponse);
    }
    
    @Override
    public SpeakingSessionResponse completeSession(Long sessionId, Long userId) {
        log.info("Hoàn thành phiên: sessionId={}, userId={}", sessionId, userId);
        
        // Verify ownership
        SpeakingSession session = getSessionAndVerifyOwnership(sessionId, userId);
        
        // Hoàn thành phiên
        session.complete();
        
        // Cập nhật điểm tổng thể từ attempts
        updateSessionOverallScore(sessionId);
        
        // Lưu
        SpeakingSession updated = sessionRepository.save(session);
        log.info("Phiên đã hoàn thành: sessionId={}, overallScore={}", sessionId, updated.getOverallScore());
        
        return sessionMapper.toResponse(updated);
    }
    
    @Override
    public SpeakingSessionResponse cancelSession(Long sessionId, Long userId) {
        log.info("Hủy phiên: sessionId={}, userId={}", sessionId, userId);
        
        // Verify ownership
        SpeakingSession session = getSessionAndVerifyOwnership(sessionId, userId);
        
        // Hủy phiên
        session.cancel();
        
        // Lưu
        SpeakingSession updated = sessionRepository.save(session);
        log.info("Phiên đã hủy: sessionId={}", sessionId);
        
        return sessionMapper.toResponse(updated);
    }
    
    @Override
    public void updateSessionOverallScore(Long sessionId) {
        log.debug("Cập nhật điểm tổng thể phiên: sessionId={}", sessionId);
        
        SpeakingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Phiên không tồn tại: " + sessionId));
        
        // Tính điểm trung bình từ feedback
        Double avgScore = feedbackService.calculateAverageOverallScore(sessionId);
        
        if (avgScore != null && avgScore > 0) {
            session.setOverallScore(Math.round(avgScore * 100.0) / 100.0);
            sessionRepository.save(session);
            log.debug("Điểm phiên được cập nhật: sessionId={}, score={}", sessionId, session.getOverallScore());
        }
    }
    
    @Override
    public void deleteSession(Long sessionId, Long userId) {
        log.info("Xóa phiên: sessionId={}, userId={}", sessionId, userId);
        
        // Verify ownership
        SpeakingSession session = getSessionAndVerifyOwnership(sessionId, userId);
        
        // Xóa phiên (cascade delete sẽ xóa attempts và feedback)
        sessionRepository.delete(session);
        log.info("Phiên đã xóa: sessionId={}", sessionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countSessions(Long userId) {
        return sessionRepository.countByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countInProgressSessions(Long userId) {
        return sessionRepository.countByUserIdAndStatus(userId, SpeakingStatus.IN_PROGRESS);
    }
    
    /**
     * Lấy session và xác minh user sở hữu
     * 
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    private SpeakingSession getSessionAndVerifyOwnership(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> {
                    log.warn("Truy cập không được phép: sessionId={}, userId={}", sessionId, userId);
                    return new IllegalArgumentException("Phiên không tồn tại hoặc bạn không có quyền truy cập");
                });
    }
    
    /**
     * Xác minh user tồn tại
     * 
     * @throws IllegalArgumentException nếu user không tồn tại
     */
    private User verifyUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại: " + userId));
    }
}
