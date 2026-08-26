package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.response.SpeakingFeedbackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SpeakingFeedbackService Interface
 * 
 * Quản lý feedback AI cho speaking attempts:
 * - Lấy feedback cho attempt/session
 * - Lấy danh sách feedback với bộ lọc
 * - Tính toán điểm số trung bình
 * - Thống kê tiến độ học
 * 
 * Tích hợp với:
 * - SpeakingFeedbackRepository (CRUD)
 * - SpeakingSessionRepository (verify ownership)
 * - GeminiAIClientService (tạo prompt analysis)
 * - PromptEngineeringService (xây dựng prompt cho speaking)
 */
public interface SpeakingFeedbackService {
    
    /**
     * Lấy feedback của một lần ghi âm
     * 
     * @param attemptId ID attempt
     * @param userId ID người dùng (verify ownership)
     * @return SpeakingFeedbackResponse feedback chi tiết
     * @throws IllegalArgumentException nếu attempt không tồn tại hoặc user không sở hữu
     */
    SpeakingFeedbackResponse getAttemptFeedback(Long attemptId, Long userId);
    
    /**
     * Lấy feedback của một phiên
     * 
     * Dùng để xem feedback tổng hợp của cả phiên
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng (verify ownership)
     * @return Optional<SpeakingFeedbackResponse> feedback phiên nếu tồn tại
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Optional<SpeakingFeedbackResponse> getSessionFeedback(Long sessionId, Long userId);
    
    /**
     * Lấy danh sách feedback của tất cả attempts trong phiên
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingFeedbackResponse> danh sách feedback
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Page<SpeakingFeedbackResponse> listFeedbackBySession(Long sessionId, Long userId, Pageable pageable);
    
    /**
     * Lấy feedback sắp xếp theo điểm tổng thể (cao nhất trước)
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingFeedbackResponse> feedback sắp xếp theo điểm
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Page<SpeakingFeedbackResponse> listFeedbackBySessionSortedByScore(Long sessionId, Long userId, Pageable pageable);
    
    /**
     * Lấy feedback sắp xếp theo điểm ngữ pháp (cao nhất trước)
     * 
     * Dùng để xem attempts có ngữ pháp tốt nhất
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingFeedbackResponse> feedback sắp xếp theo grammar
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Page<SpeakingFeedbackResponse> listFeedbackBySessionSortedByGrammar(Long sessionId, Long userId, Pageable pageable);
    
    /**
     * Lấy feedback sắp xếp theo điểm từ vựng (cao nhất trước)
     * 
     * Dùng để xem attempts có từ vựng phong phú nhất
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingFeedbackResponse> feedback sắp xếp theo vocabulary
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Page<SpeakingFeedbackResponse> listFeedbackBySessionSortedByVocabulary(Long sessionId, Long userId, Pageable pageable);
    
    /**
     * Lấy danh sách feedback được tạo trong khoảng thời gian
     * 
     * Dùng để thống kê theo ngày/tuần/tháng
     * 
     * @param startDate Thời gian bắt đầu
     * @param endDate Thời gian kết thúc
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingFeedbackResponse> feedback trong khoảng thời gian
     */
    Page<SpeakingFeedbackResponse> listFeedbackByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Tính điểm trung bình tổng thể của phiên
     * 
     * @param sessionId ID phiên
     * @return Điểm trung bình, hoặc 0.0 nếu không có feedback
     * @throws IllegalArgumentException nếu phiên không tồn tại
     */
    Double calculateAverageOverallScore(Long sessionId);
    
    /**
     * Tính điểm ngữ pháp trung bình của phiên
     * 
     * @param sessionId ID phiên
     * @return Điểm grammar trung bình, hoặc 0.0 nếu không có feedback
     * @throws IllegalArgumentException nếu phiên không tồn tại
     */
    Double calculateAverageGrammarScore(Long sessionId);
    
    /**
     * Tính điểm từ vựng trung bình của phiên
     * 
     * @param sessionId ID phiên
     * @return Điểm vocabulary trung bình, hoặc 0.0 nếu không có feedback
     * @throws IllegalArgumentException nếu phiên không tồn tại
     */
    Double calculateAverageVocabularyScore(Long sessionId);
    
    /**
     * Tính điểm trôi chảy trung bình của phiên
     * 
     * @param sessionId ID phiên
     * @return Điểm fluency trung bình, hoặc 0.0 nếu không có feedback
     * @throws IllegalArgumentException nếu phiên không tồn tại
     */
    Double calculateAverageFluencyScore(Long sessionId);
    
    /**
     * Tính điểm nội dung trung bình của phiên
     * 
     * @param sessionId ID phiên
     * @return Điểm content trung bình, hoặc 0.0 nếu không có feedback
     * @throws IllegalArgumentException nếu phiên không tồn tại
     */
    Double calculateAverageContentScore(Long sessionId);
    
    /**
     * Lấy feedback tốt nhất (điểm cao nhất) của phiên
     * 
     * Dùng để xem attempt tốt nhất trong phiên
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @return Optional<SpeakingFeedbackResponse> feedback tốt nhất
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    Optional<SpeakingFeedbackResponse> getBestFeedbackInSession(Long sessionId, Long userId);
    
    /**
     * Đếm số feedback của phiên
     * 
     * @param sessionId ID phiên
     * @return Số feedback
     */
    long countFeedbackBySession(Long sessionId);
    
    /**
     * Kiểm tra xem attempt có feedback chưa
     * 
     * @param attemptId ID attempt
     * @return true nếu có feedback, false nếu chưa
     */
    boolean hasAttemptFeedback(Long attemptId);
    
    /**
     * Kiểm tra xem session có feedback chưa
     * 
     * @param sessionId ID phiên
     * @return true nếu có feedback, false nếu chưa
     */
    boolean hasSessionFeedback(Long sessionId);
}
