package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.response.SpeakingPracticeResponse;
import com.vocabularyai.speaking.dto.response.SpeakingPracticeResponse.*;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import com.vocabularyai.speaking.entity.SpeakingFeedback;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.repository.SpeakingAttemptRepository;
import com.vocabularyai.speaking.repository.SpeakingFeedbackRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.analysis.SpeakingAIAnalysisService;
import com.vocabularyai.speaking.service.analysis.dto.SpeakingAnalysisResult;
import com.vocabularyai.speaking.service.pronunciation.PronunciationAnalysisService;
import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult;
import com.vocabularyai.speaking.service.storage.AudioStorageService;
import com.vocabularyai.speaking.service.storage.AudioMetadata;
import com.vocabularyai.speaking.service.stt.SpeechToTextService;
import com.vocabularyai.speaking.service.stt.TranscriptResult;
import com.vocabularyai.speaking.config.SpeakingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SpeakingPracticeServiceImpl
 * 
 * Core Orchestration Service cho AI Speaking Practice
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpeakingPracticeServiceImpl implements SpeakingPracticeService {
    
    private final SpeakingSessionRepository sessionRepository;
    private final SpeakingAttemptRepository attemptRepository;
    private final SpeakingFeedbackRepository feedbackRepository;
    private final SpeakingFeedbackService feedbackService;
    private final AudioStorageService audioStorageService;
    private final SpeechToTextService speechToTextService;
    private final SpeakingAIAnalysisService aiAnalysisService;
    private final PronunciationAnalysisService pronunciationAnalysisService;
    private final SpeakingProperties speakingProperties;
    private final ObjectMapper objectMapper;
    
    @Override
    public SpeakingPracticeResponse submitSpeakingPractice(Long sessionId, Long userId, MultipartFile audioFile) {
        log.info("Starting speaking practice - sessionId: {}, userId: {}", sessionId, userId);
        
        try {
            // STEP 1: Verify session ownership
            SpeakingSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                    .orElseThrow(() -> new SpeakingPracticeException("Phiên không tồn tại hoặc bạn không có quyền truy cập"));
            
            // STEP 2: Validate audio file
            validateAudioFile(audioFile);
            
            // STEP 3: Create attempt with audio upload
            long attemptNumber = attemptRepository.countBySessionId(sessionId) + 1;
            AudioMetadata audioMetadata = audioStorageService.uploadAudio(
                    sessionId, Math.toIntExact(attemptNumber), audioFile);
            
            SpeakingAttempt attempt = SpeakingAttempt.builder()
                    .session(session)
                    .audioUrl(audioMetadata.getAudioUrl())
                    .audioDurationSeconds(audioMetadata.getDurationSeconds())
                    .attemptNumber(Math.toIntExact(attemptNumber))
                    .build();
            
            attempt = attemptRepository.save(attempt);
            log.debug("Created attempt: {}", attempt.getId());
            
            // STEP 4: Speech-to-Text - cần pass MultipartFile trực tiếp, không phải URL
            TranscriptResult transcriptResult = speechToTextService.transcribe(
                    audioFile, 
                    speakingProperties.getSpeechToText().getLanguageCode());
            
            attempt.setTranscript(transcriptResult.getTranscript());
            attempt.setTranscriptConfidence(transcriptResult.getConfidence());
            attemptRepository.save(attempt);
            log.debug("Updated attempt with transcript");
            
            // STEP 5: AI Analysis
            SpeakingAnalysisResult analysisResult = aiAnalysisService.analyzeSpeakingAttempt(
                    transcriptResult.getTranscript(),
                    session.getTopic(),
                    session.getLevel().toString(),
                    attempt.getId(),
                    sessionId,
                    userId);
            
            log.debug("AI analysis completed");
            
            // STEP 6: Pronunciation Analysis (optional)
            PronunciationAnalysisResult pronunciationResult = null;
            if (pronunciationAnalysisService.isProviderAvailable()) {
                try {
                    pronunciationResult = pronunciationAnalysisService.analyzePronunciation(
                            attempt.getAudioUrl(),
                            transcriptResult.getTranscript(),
                            session.getTopic(),
                            session.getLevel().toString(),
                            userId);
                } catch (Exception e) {
                    log.warn("Pronunciation analysis failed: {}", e.getMessage());
                }
            }
            
            // STEP 7: Update learning progress
            updateLearningProgress(userId, analysisResult);
            
            // STEP 8: Build response
            SpeakingPracticeResponse response = buildResponse(
                    session, attempt, transcriptResult, analysisResult, pronunciationResult, userId);
            
            log.info("Speaking practice completed - sessionId: {}, score: {}",
                    sessionId, analysisResult.getAnalysisScores().getOverall());
            
            return response;
            
        } catch (SpeakingPracticeException e) {
            log.error("Speaking practice failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new SpeakingPracticeException("Lỗi xử lý bài tập speaking: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public SpeakingPracticeResponse getPracticeSession(Long sessionId, Long userId) {
        log.debug("Getting practice session - sessionId: {}, userId: {}", sessionId, userId);
        
        SpeakingSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new SpeakingPracticeException("Phiên không tồn tại hoặc bạn không có quyền truy cập"));
        
        SpeakingAttempt latestAttempt = attemptRepository.findLatestBySessionId(sessionId)
                .orElse(null);
        
        if (latestAttempt == null || latestAttempt.getTranscript() == null) {
            return buildEmptyResponse(session);
        }
        
        SpeakingFeedback feedback = feedbackRepository.findByAttemptId(latestAttempt.getId())
                .orElse(null);
        
        return buildResponseFromSession(session, latestAttempt, feedback, userId);
    }
    
    private void validateAudioFile(MultipartFile audioFile) throws SpeakingPracticeException {
        if (audioFile.isEmpty()) {
            throw new SpeakingPracticeException("File audio trống");
        }
        
        long maxSize = speakingProperties.getAudio().getMaxFileSize();
        if (audioFile.getSize() > maxSize) {
            throw new SpeakingPracticeException(
                    String.format("Kích thước file vượt quá giới hạn: %d MB", maxSize / (1024 * 1024)));
        }
        
        String filename = audioFile.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new SpeakingPracticeException("Tên file không hợp lệ");
        }
        
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();
        String[] supportedFormats = speakingProperties.getAudio().getSupportedFormats().split(",");
        
        boolean formatValid = false;
        for (String format : supportedFormats) {
            if (format.trim().equalsIgnoreCase(ext)) {
                formatValid = true;
                break;
            }
        }
        
        if (!formatValid) {
            throw new SpeakingPracticeException("Format audio không hỗ trợ: " + ext);
        }
    }
    
    private void updateLearningProgress(Long userId, SpeakingAnalysisResult analysisResult) {
        try {
            log.debug("Updating learning progress - userId: {}, score: {}", 
                    userId, analysisResult.getAnalysisScores().getOverall());
        } catch (Exception e) {
            log.warn("Failed to update learning progress: {}", e.getMessage());
        }
    }
    
    private SpeakingPracticeResponse buildResponse(
            SpeakingSession session,
            SpeakingAttempt attempt,
            TranscriptResult transcriptResult,
            SpeakingAnalysisResult analysisResult,
            PronunciationAnalysisResult pronunciationResult,
            Long userId) {
        
        StatisticsInfo statistics = buildStatistics(session.getId(), userId);
        
        PronunciationInfo pronunciationInfo = null;
        if (pronunciationResult != null && pronunciationResult.getPronunciationScore() != null) {
            pronunciationInfo = PronunciationInfo.builder()
                    .pronunciationScore(pronunciationResult.getPronunciationScore())
                    .feedback(pronunciationResult.getFeedback())
                    .stressFeedback(pronunciationResult.getStressFeedback())
                    .intonationFeedback(pronunciationResult.getIntonationFeedback())
                    .provider(pronunciationResult.getProvider())
                    .build();
        }
        
        List<CorrectionInfo> corrections = analysisResult.getCorrections().stream()
                .map(c -> CorrectionInfo.builder()
                        .original(c.getOriginal())
                        .corrected(c.getCorrected())
                        .errorType(c.getErrorType())
                        .explanation(c.getExplanation())
                        .build())
                .collect(Collectors.toList());
        
        return SpeakingPracticeResponse.builder()
                .session(SessionInfo.builder()
                        .id(session.getId())
                        .topic(session.getTopic())
                        .level(session.getLevel())
                        .description(session.getDescription())
                        .status(session.getStatus())
                        .startedAt(session.getStartedAt())
                        .overallScore(session.getOverallScore())
                        .attemptCount(session.getAttemptCount())
                        .build())
                .attempt(AttemptInfo.builder()
                        .id(attempt.getId())
                        .attemptNumber(attempt.getAttemptNumber())
                        .audioUrl(attempt.getAudioUrl())
                        .audioDurationSeconds(attempt.getAudioDurationSeconds())
                        .transcript(transcriptResult.getTranscript())
                        .transcriptConfidence(transcriptResult.getConfidence())
                        .createdAt(attempt.getCreatedAt())
                        .build())
                .scores(ScoresInfo.builder()
                        .overallScore(analysisResult.getAnalysisScores().getOverall())
                        .grammarScore(analysisResult.getAnalysisScores().getGrammar())
                        .vocabularyScore(analysisResult.getAnalysisScores().getVocabulary())
                        .fluencyScore(analysisResult.getAnalysisScores().getFluency())
                        .contentScore(analysisResult.getAnalysisScores().getContent())
                        .pronunciationScore(analysisResult.getAnalysisScores().getPronunciation())
                        .build())
                .feedback(FeedbackInfo.builder()
                        .feedback(analysisResult.getFeedback())
                        .corrections(corrections)
                        .suggestions(analysisResult.getSuggestions())
                        .strengths(analysisResult.getStrengths())
                        .weaknesses(analysisResult.getWeaknesses())
                        .build())
                .pronunciation(pronunciationInfo)
                .statistics(statistics)
                .build();
    }
    
    private SpeakingPracticeResponse buildEmptyResponse(SpeakingSession session) {
        return SpeakingPracticeResponse.builder()
                .session(SessionInfo.builder()
                        .id(session.getId())
                        .topic(session.getTopic())
                        .level(session.getLevel())
                        .description(session.getDescription())
                        .status(session.getStatus())
                        .startedAt(session.getStartedAt())
                        .overallScore(session.getOverallScore())
                        .attemptCount(session.getAttemptCount())
                        .build())
                .statistics(StatisticsInfo.builder()
                        .totalAttempts(0)
                        .averageScore(0.0)
                        .bestScore(0.0)
                        .build())
                .build();
    }
    
    private SpeakingPracticeResponse buildResponseFromSession(
            SpeakingSession session,
            SpeakingAttempt attempt,
            SpeakingFeedback feedback,
            Long userId) {
        
        StatisticsInfo statistics = buildStatistics(session.getId(), userId);
        
        PronunciationInfo pronunciationInfo = null;
        if (feedback != null && feedback.getPronunciationScore() != null) {
            pronunciationInfo = PronunciationInfo.builder()
                    .pronunciationScore(feedback.getPronunciationScore())
                    .build();
        }
        
        List<CorrectionInfo> corrections = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        
        if (feedback != null) {
            if (feedback.getCorrections() != null) {
                try {
                    List<SpeakingAnalysisResult.Correction> parsed = 
                            objectMapper.readValue(feedback.getCorrections(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, 
                                    SpeakingAnalysisResult.Correction.class));
                    corrections = parsed.stream()
                            .map(c -> CorrectionInfo.builder()
                                    .original(c.getOriginal())
                                    .corrected(c.getCorrected())
                                    .errorType(c.getErrorType())
                                    .explanation(c.getExplanation())
                                    .build())
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    log.warn("Failed to parse corrections");
                }
            }
            
            if (feedback.getSuggestions() != null) {
                try {
                    suggestions = objectMapper.readValue(feedback.getSuggestions(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                } catch (Exception e) {
                    log.warn("Failed to parse suggestions");
                }
            }
            
            if (feedback.getStrengths() != null) {
                try {
                    strengths = objectMapper.readValue(feedback.getStrengths(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                } catch (Exception e) {
                    log.warn("Failed to parse strengths");
                }
            }
            
            if (feedback.getWeaknesses() != null) {
                try {
                    weaknesses = objectMapper.readValue(feedback.getWeaknesses(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                } catch (Exception e) {
                    log.warn("Failed to parse weaknesses");
                }
            }
        }
        
        return SpeakingPracticeResponse.builder()
                .session(SessionInfo.builder()
                        .id(session.getId())
                        .topic(session.getTopic())
                        .level(session.getLevel())
                        .description(session.getDescription())
                        .status(session.getStatus())
                        .startedAt(session.getStartedAt())
                        .overallScore(session.getOverallScore())
                        .attemptCount(session.getAttemptCount())
                        .build())
                .attempt(AttemptInfo.builder()
                        .id(attempt.getId())
                        .attemptNumber(attempt.getAttemptNumber())
                        .audioUrl(attempt.getAudioUrl())
                        .audioDurationSeconds(attempt.getAudioDurationSeconds())
                        .transcript(attempt.getTranscript())
                        .transcriptConfidence(attempt.getTranscriptConfidence())
                        .createdAt(attempt.getCreatedAt())
                        .build())
                .scores(feedback != null ? ScoresInfo.builder()
                        .overallScore(feedback.getOverallScore())
                        .grammarScore(feedback.getGrammarScore())
                        .vocabularyScore(feedback.getVocabularyScore())
                        .fluencyScore(feedback.getFluencyScore())
                        .contentScore(feedback.getContentScore())
                        .pronunciationScore(feedback.getPronunciationScore())
                        .build() : null)
                .feedback(feedback != null ? FeedbackInfo.builder()
                        .feedback(feedback.getFeedback())
                        .corrections(corrections)
                        .suggestions(suggestions)
                        .strengths(strengths)
                        .weaknesses(weaknesses)
                        .build() : null)
                .pronunciation(pronunciationInfo)
                .statistics(statistics)
                .build();
    }
    
    private StatisticsInfo buildStatistics(Long sessionId, Long userId) {
        try {
            long totalAttempts = feedbackService.countFeedbackBySession(sessionId);
            Double avgScore = feedbackService.calculateAverageOverallScore(sessionId);
            Double bestScore = feedbackService.getBestFeedbackInSession(sessionId, userId)
                    .map(f -> f.getOverallScore())
                    .orElse(0.0);
            
            Double avgGrammar = feedbackService.calculateAverageGrammarScore(sessionId);
            Double avgVocab = feedbackService.calculateAverageVocabularyScore(sessionId);
            Double avgFluency = feedbackService.calculateAverageFluencyScore(sessionId);
            Double avgContent = feedbackService.calculateAverageContentScore(sessionId);
            
            return StatisticsInfo.builder()
                    .totalAttempts(Math.toIntExact(totalAttempts))
                    .averageScore(avgScore != null ? avgScore : 0.0)
                    .bestScore(bestScore)
                    .averageGrammarScore(avgGrammar)
                    .averageVocabularyScore(avgVocab)
                    .averageFluencyScore(avgFluency)
                    .averageContentScore(avgContent)
                    .improvement(calculateImprovement(sessionId))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to calculate statistics: {}", e.getMessage());
            return StatisticsInfo.builder()
                    .totalAttempts(0)
                    .averageScore(0.0)
                    .bestScore(0.0)
                    .build();
        }
    }
    
    private String calculateImprovement(Long sessionId) {
        try {
            List<SpeakingFeedback> allFeedback = feedbackRepository.findAllAttemptFeedbackBySessionId(sessionId);
            
            if (allFeedback.size() < 2) {
                return "Chưa đủ dữ liệu để đánh giá";
            }
            
            SpeakingFeedback first = allFeedback.get(0);
            SpeakingFeedback last = allFeedback.get(allFeedback.size() - 1);
            
            if (first.getOverallScore() == null || last.getOverallScore() == null) {
                return "Chưa có điểm để so sánh";
            }
            
            double improvement = last.getOverallScore() - first.getOverallScore();
            if (improvement > 5) {
                return String.format("Tiến bộ: +%.1f điểm", improvement);
            } else if (improvement < -5) {
                return String.format("Giảm: %.1f điểm", improvement);
            } else {
                return "Ổn định";
            }
        } catch (Exception e) {
            log.warn("Failed to calculate improvement: {}", e.getMessage());
            return "Không thể tính toán";
        }
    }
}
