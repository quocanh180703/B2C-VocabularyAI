package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.request.SubmitSpeakingAttemptRequest;
import com.vocabularyai.speaking.dto.response.SpeakingAttemptResponse;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.mapper.SpeakingAttemptMapper;
import com.vocabularyai.speaking.repository.SpeakingAttemptRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.storage.AudioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * SpeakingAttemptService Implementation
 * 
 * Quản lý các lần ghi âm:
 * - Upload file audio
 * - Lưu transcript
 * - Lấy thông tin attempts
 * - Xóa attempts
 * 
 * CẢNH BÁO: MVP phase - Audio storage sẽ được implement sau
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpeakingAttemptServiceImpl implements SpeakingAttemptService {
    
    private final SpeakingAttemptRepository attemptRepository;
    private final SpeakingSessionRepository sessionRepository;
    private final SpeakingAttemptMapper attemptMapper;
    private final AudioStorageService audioStorageService;
    
    @Value("${speaking.audio.max-file-size:52428800}")
    private long maxFileSize;
    
    @Value("${speaking.audio.max-duration-seconds:600}")
    private long maxDurationSeconds;
    
    @Value("${speaking.audio.supported-formats:WAV,MP3,M4A,WEBM,OGG,FLAC}")
    private String supportedFormats;
    
    @Override
    public SpeakingAttemptResponse createAttempt(Long sessionId, Long userId, MultipartFile audioFile) {
        log.info("Tạo attempt mới: sessionId={}, userId={}", sessionId, userId);
        
        // Verify session ownership
        SpeakingSession session = getSessionAndVerifyOwnership(sessionId, userId);
        
        // Validate audio file
        validateAudioFile(audioFile);
        
        // Xác định số thứ tự attempt (auto increment)
        long attemptCount = attemptRepository.countBySessionId(sessionId);
        int attemptNumber = (int) (attemptCount + 1);
        
        // MVP: Tạm thời lưu URL là tên file (sẽ implement upload storage sau)
        String audioUrl = generateAudioUrl(sessionId, attemptNumber, audioFile.getOriginalFilename());
        
        // Tạo attempt entity
        SpeakingAttempt attempt = SpeakingAttempt.builder()
                .session(session)
                .audioUrl(audioUrl)
                .audioDurationSeconds(0) // TODO: Extract từ file metadata
                .attemptNumber(attemptNumber)
                .build();
        
        // Lưu vào database
        SpeakingAttempt saved = attemptRepository.save(attempt);
        log.info("Attempt được tạo thành công: attemptId={}, attemptNumber={}", saved.getId(), attemptNumber);
        
        return attemptMapper.toResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SpeakingAttemptResponse getAttemptDetail(Long attemptId, Long userId) {
        log.debug("Lấy chi tiết attempt: attemptId={}, userId={}", attemptId, userId);
        
        // Verify user ownership qua session
        SpeakingAttempt attempt = getAttemptAndVerifyOwnership(attemptId, userId);
        
        return attemptMapper.toResponse(attempt);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingAttemptResponse> listAttemptsBySession(Long sessionId, Long userId, Pageable pageable) {
        log.debug("Lấy danh sách attempts của phiên: sessionId={}, userId={}", sessionId, userId);
        
        // Verify session ownership
        getSessionAndVerifyOwnership(sessionId, userId);
        
        return attemptRepository.findBySessionId(sessionId, pageable)
                .map(attemptMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SpeakingAttemptResponse> listAttemptsBySessionOrdered(Long sessionId, Long userId) {
        log.debug("Lấy danh sách attempts theo thứ tự: sessionId={}, userId={}", sessionId, userId);
        
        // Verify session ownership
        getSessionAndVerifyOwnership(sessionId, userId);
        
        return attemptRepository.findBySessionIdOrderByAttemptNumber(sessionId)
                .stream()
                .map(attemptMapper::toResponse)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public SpeakingAttemptResponse getLatestAttempt(Long sessionId, Long userId) {
        log.debug("Lấy attempt mới nhất: sessionId={}, userId={}", sessionId, userId);
        
        // Verify session ownership
        getSessionAndVerifyOwnership(sessionId, userId);
        
        return attemptRepository.findLatestBySessionId(sessionId)
                .map(attemptMapper::toResponse)
                .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SpeakingAttemptResponse getAttemptByNumber(Long sessionId, Long userId, Integer attemptNumber) {
        log.debug("Lấy attempt theo số thứ tự: sessionId={}, attemptNumber={}, userId={}", sessionId, attemptNumber, userId);
        
        // Verify session ownership
        getSessionAndVerifyOwnership(sessionId, userId);
        
        return attemptRepository.findBySessionIdAndAttemptNumber(sessionId, attemptNumber)
                .map(attemptMapper::toResponse)
                .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingAttemptResponse> listHighConfidenceAttempts(Long sessionId, Long userId, Double minConfidence, Pageable pageable) {
        log.debug("Lấy attempts có độ tin cậy cao: sessionId={}, minConfidence={}, userId={}", sessionId, minConfidence, userId);
        
        // Verify session ownership
        getSessionAndVerifyOwnership(sessionId, userId);
        
        return attemptRepository.findBySessionIdAndHighConfidence(sessionId, minConfidence, pageable)
                .map(attemptMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<SpeakingAttemptResponse> listAttemptsWithFeedback(Long sessionId, Long userId, Pageable pageable) {
        log.debug("Lấy attempts có feedback: sessionId={}, userId={}", sessionId, userId);
        
        // Verify session ownership
        getSessionAndVerifyOwnership(sessionId, userId);
        
        return attemptRepository.findBySessionIdWithFeedback(sessionId, pageable)
                .map(attemptMapper::toResponse);
    }
    
    @Override
    public SpeakingAttemptResponse updateTranscript(Long attemptId, Long userId, String transcript, Double confidence) {
        log.info("Cập nhật transcript cho attempt: attemptId={}, userId={}", attemptId, userId);
        
        // Verify ownership
        SpeakingAttempt attempt = getAttemptAndVerifyOwnership(attemptId, userId);
        
        // Cập nhật transcript
        attempt.setTranscript(transcript);
        attempt.setTranscriptConfidence(confidence);
        
        // Lưu
        SpeakingAttempt updated = attemptRepository.save(attempt);
        log.info("Transcript được cập nhật: attemptId={}, confidence={}", attemptId, confidence);
        
        return attemptMapper.toResponse(updated);
    }
    
    @Override
    public void deleteAttempt(Long attemptId, Long userId) {
        log.info("Xóa attempt: attemptId={}, userId={}", attemptId, userId);
        
        // Verify ownership
        SpeakingAttempt attempt = getAttemptAndVerifyOwnership(attemptId, userId);
        
        // Xóa file audio từ storage (nếu có audioUrl)
        if (attempt.getAudioUrl() != null && !attempt.getAudioUrl().isEmpty()) {
            try {
                audioStorageService.deleteAudio(attempt.getAudioUrl());
                log.info("Audio file được xóa từ storage: attemptId={}, audioUrl={}", 
                        attemptId, attempt.getAudioUrl());
            } catch (Exception e) {
                log.warn("Không thể xóa audio file từ storage: attemptId={}, audioUrl={}, error: {}", 
                        attemptId, attempt.getAudioUrl(), e.getMessage());
                // Không throw exception, tiếp tục xóa attempt từ DB
            }
        }
        
        // Xóa attempt (cascade delete sẽ xóa feedback)
        attemptRepository.delete(attempt);
        log.info("Attempt đã xóa: attemptId={}", attemptId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countAttemptsBySession(Long sessionId) {
        return attemptRepository.countBySessionId(sessionId);
    }
    
    /**
     * Lấy attempt và xác minh user sở hữu (qua session)
     */
    private SpeakingAttempt getAttemptAndVerifyOwnership(Long attemptId, Long userId) {
        SpeakingAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt không tồn tại: " + attemptId));
        
        // Verify user owns the session
        Long sessionId = attempt.getSession().getId();
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            log.warn("Truy cập không được phép: attemptId={}, userId={}", attemptId, userId);
            throw new IllegalArgumentException("Bạn không có quyền truy cập attempt này");
        }
        
        return attempt;
    }
    
    /**
     * Lấy session và xác minh user sở hữu
     */
    private SpeakingSession getSessionAndVerifyOwnership(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> {
                    log.warn("Truy cập không được phép: sessionId={}, userId={}", sessionId, userId);
                    return new IllegalArgumentException("Phiên không tồn tại hoặc bạn không có quyền truy cập");
                });
    }
    
    /**
     * Validate audio file (size, format)
     */
    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File audio không được để trống");
        }
        
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File quá lớn. Tối đa: %d MB", maxFileSize / (1024 * 1024))
            );
        }
        
        // Check file format
        String filename = file.getOriginalFilename();
        if (filename == null || !isValidAudioFormat(filename)) {
            throw new IllegalArgumentException(
                    String.format("Định dạng file không được hỗ trợ. Hỗ trợ: %s", supportedFormats)
            );
        }
        
        log.debug("Audio file được validate: filename={}, size={}", filename, file.getSize());
    }
    
    /**
     * Kiểm tra định dạng file audio
     */
    private boolean isValidAudioFormat(String filename) {
        String[] formats = supportedFormats.split(",");
        String extension = getFileExtension(filename).toUpperCase();
        
        for (String format : formats) {
            if (extension.equals(format.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lấy extension từ filename
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * Tạo URL cho audio file (MVP: tạm thời dùng tên file)
     * 
     * TODO: Implement upload storage (local/Cloudinary/S3)
     */
    private String generateAudioUrl(Long sessionId, int attemptNumber, String originalFilename) {
        // Format: /storage/audio/session-{sessionId}/attempt-{attemptNumber}-{timestamp}.{ext}
        String ext = getFileExtension(originalFilename);
        long timestamp = System.currentTimeMillis();
        return String.format("/storage/audio/session-%d/attempt-%d-%d.%s", 
                sessionId, attemptNumber, timestamp, ext);
    }
}
