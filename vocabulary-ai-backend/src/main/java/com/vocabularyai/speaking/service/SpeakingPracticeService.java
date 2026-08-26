package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.response.SpeakingPracticeResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * SpeakingPracticeService Interface
 * 
 * Core Orchestration Service cho AI Speaking Practice
 * 
 * Chịu trách nhiệm orchestrate toàn bộ flow:
 * 1. Audio Upload → Validate
 * 2. AudioStorageService → Lưu audio, nhận audioUrl
 * 3. SpeechToTextService → Audio → Transcript
 * 4. SpeakingAIAnalysisService → Phân tích AI (grammar, vocabulary, fluency, content)
 * 5. PronunciationAnalysisService → Phân tích pronunciation (nếu provider hỗ trợ)
 * 6. Combine Feedback → Lưu SpeakingFeedback entity
 * 7. Update Learning Progress → Cập nhật LearningProgress
 * 8. Trả về SpeakingPracticeResponse
 * 
 * Không bao gồm:
 * - Tạo session (do SpeakingSessionService)
 * - Lấy attempts/feedback history (do SpeakingAttemptService/SpeakingFeedbackService)
 * 
 * Tích hợp với:
 * - SpeakingAttemptService
 * - AudioStorageService
 * - SpeechToTextService
 * - SpeakingAIAnalysisService
 * - PronunciationAnalysisService
 * - SpeakingFeedbackService
 * - SpeakingProgressService
 */
public interface SpeakingPracticeService {
    
    /**
     * Xử lý bài tập speaking: upload audio, transcribe, analyze, feedback
     * 
     * Complete flow:
     * 1. Verify session ownership
     * 2. Validate audio file
     * 3. Create SpeakingAttempt (audioUrl được lưu)
     * 4. Upload audio to storage (local/Cloudinary/S3)
     * 5. Call SpeechToTextService → Transcript
     * 6. Update attempt with transcript
     * 7. Call SpeakingAIAnalysisService → AI scores + feedback
     * 8. Call PronunciationAnalysisService → Pronunciation scores (optional)
     * 9. Combine results → Save SpeakingFeedback
     * 10. Update LearningProgress
     * 11. Return SpeakingPracticeResponse
     * 
     * Error handling:
     * - Validate audio: size, format, MIME type
     * - Handle STT errors: invalid audio, timeout, provider errors
     * - Handle AI analysis errors: Gemini timeout, invalid response
     * - Transaction rollback nếu bất kỳ bước nào thất bại
     * 
     * @param sessionId ID phiên luyện tập
     * @param userId ID người dùng
     * @param audioFile Audio file từ client (multipart/form-data)
     * @return SpeakingPracticeResponse chứa attempt info, transcript, feedback, scores
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     * @throws IllegalArgumentException nếu audio file không hợp lệ
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu audio storage/STT/AI lỗi
     * @throws com.vocabularyai.common.exception.RateLimitExceededException nếu user vượt quá rate limit
     */
    SpeakingPracticeResponse submitSpeakingPractice(Long sessionId, Long userId, MultipartFile audioFile);
    
    /**
     * Lấy thông tin practice session hiện tại
     * 
     * Bao gồm:
     * - Phiên info (topic, level, status)
     * - Danh sách attempts của phiên
     * - Feedback của attempt gần nhất
     * - Điểm tổng thể, average scores
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @return SpeakingPracticeResponse với thông tin phiên
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    SpeakingPracticeResponse getPracticeSession(Long sessionId, Long userId);
}
