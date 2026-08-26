package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.request.SubmitSpeakingAttemptRequest;
import com.vocabularyai.speaking.dto.response.SpeakingAttemptResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * SpeakingAttemptService Interface
 * 
 * Quản lý các lần ghi âm:
 * - Upload file audio
 * - Lưu transcript (từ Speech-to-Text)
 * - Lấy chi tiết attempt
 * - Lấy danh sách attempts của phiên
 * - Gọi AI để phân tích và cho feedback
 * 
 * Tích hợp với:
 * - SpeakingAttemptRepository (CRUD)
 * - SpeakingFeedbackService (AI analysis)
 * - AudioStorageService (lưu file audio)
 * - SpeechToTextService (convert audio -> text)
 */
public interface SpeakingAttemptService {
    
    /**
     * Tạo một lần ghi âm mới
     * 
     * Flow:
     * 1. Verify session ownership
     * 2. Validate audio file (size, format, duration)
     * 3. Xác định số thứ tự attempt (tăng dần)
     * 4. Upload file audio (local/Cloudinary/S3)
     * 5. Lấy audio metadata (duration)
     * 6. Tạo SpeakingAttempt entity
     * 7. Lưu vào database
     * 8. Trả về response (chưa có transcript/feedback)
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng (verify ownership)
     * @param audioFile File audio được upload (multipart/form-data)
     * @return SpeakingAttemptResponse của attempt vừa tạo (audioUrl, attempts number)
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     * @throws IllegalArgumentException nếu file không hợp lệ (size, format, duration)
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu upload storage lỗi
     */
    SpeakingAttemptResponse createAttempt(Long sessionId, Long userId, MultipartFile audioFile);
    
    /**
     * Lấy chi tiết một lần ghi âm (kèm transcript và feedback)
     * 
     * @param attemptId ID attempt
     * @param userId ID người dùng (verify ownership qua session)
     * @return SpeakingAttemptResponse với transcript và feedback
     * @throws IllegalArgumentException nếu attempt không tồn tại hoặc user không sở hữu
     */
    SpeakingAttemptResponse getAttemptDetail(Long attemptId, Long userId);
    
    /**
     * Lấy danh sách tất cả attempts của một phiên
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng (verify ownership)
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingAttemptResponse> danh sách attempts
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Page<SpeakingAttemptResponse> listAttemptsBySession(Long sessionId, Long userId, Pageable pageable);
    
    /**
     * Lấy danh sách attempts theo thứ tự (không phân trang)
     * 
     * Dùng để lấy toàn bộ attempts khi cần tính toán thống kê
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @return List<SpeakingAttemptResponse> sắp xếp theo attempt number
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    List<SpeakingAttemptResponse> listAttemptsBySessionOrdered(Long sessionId, Long userId);
    
    /**
     * Lấy lần ghi âm mới nhất của phiên
     * 
     * Dùng để xem attempt gần đây nhất
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @return SpeakingAttemptResponse attempt mới nhất, hoặc null nếu không có
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    SpeakingAttemptResponse getLatestAttempt(Long sessionId, Long userId);
    
    /**
     * Lấy lần ghi âm theo số thứ tự
     * 
     * Ví dụ: Attempt 1, Attempt 2, v.v.
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @param attemptNumber Số thứ tự (1, 2, 3, ...)
     * @return SpeakingAttemptResponse nếu tìm thấy, hoặc null
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    SpeakingAttemptResponse getAttemptByNumber(Long sessionId, Long userId, Integer attemptNumber);
    
    /**
     * Lấy danh sách attempts có độ tin cậy cao
     * 
     * Dùng để lọc attempts với transcript chất lượng tốt
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @param minConfidence Độ tin cậy tối thiểu (0-100)
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingAttemptResponse> attempts có độ tin cậy cao
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Page<SpeakingAttemptResponse> listHighConfidenceAttempts(Long sessionId, Long userId, Double minConfidence, Pageable pageable);
    
    /**
     * Lấy danh sách attempts đã có feedback
     * 
     * Dùng để lấy attempts đã được AI phân tích
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingAttemptResponse> attempts có feedback
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Page<SpeakingAttemptResponse> listAttemptsWithFeedback(Long sessionId, Long userId, Pageable pageable);
    
    /**
     * Ghi nhận transcript cho một lần ghi âm
     * 
     * Được gọi sau khi Speech-to-Text service hoàn thành
     * 
     * Flow:
     * 1. Verify user ownership
     * 2. Cập nhật transcript + confidence
     * 3. Lưu database
     * 4. Invalidate cache
     * 5. Trả về response
     * 
     * @param attemptId ID attempt
     * @param userId ID người dùng
     * @param transcript Text từ Speech-to-Text
     * @param confidence Độ tin cậy (0-100)
     * @return SpeakingAttemptResponse đã cập nhật
     * @throws IllegalArgumentException nếu attempt không tồn tại hoặc user không sở hữu
     */
    SpeakingAttemptResponse updateTranscript(Long attemptId, Long userId, String transcript, Double confidence);
    
    /**
     * Xóa một lần ghi âm (xóa file audio + feedback)
     * 
     * CẢNH BÁO: Thao tác này không thể hoàn tác
     * 
     * Flow:
     * 1. Verify user ownership
     * 2. Xóa file audio từ storage
     * 3. Xóa attempt + feedback từ database (cascade)
     * 4. Invalidate cache
     * 
     * @param attemptId ID attempt
     * @param userId ID người dùng
     * @throws IllegalArgumentException nếu attempt không tồn tại hoặc user không sở hữu
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu xóa file storage lỗi
     */
    void deleteAttempt(Long attemptId, Long userId);
    
    /**
     * Đếm số attempts của một phiên
     * 
     * @param sessionId ID phiên
     * @return Số attempts
     */
    long countAttemptsBySession(Long sessionId);
}
