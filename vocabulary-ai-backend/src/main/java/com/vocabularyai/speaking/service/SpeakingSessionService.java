package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.request.CreateSpeakingSessionRequest;
import com.vocabularyai.speaking.dto.response.SpeakingSessionResponse;
import com.vocabularyai.speaking.dto.response.SpeakingSessionSummaryResponse;
import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * SpeakingSessionService Interface
 * 
 * Quản lý các phiên luyện nói:
 * - Tạo phiên mới
 * - Lấy thông tin phiên
 * - Cập nhật trạng thái phiên
 * - Lấy danh sách phiên với bộ lọc
 * - Hoàn thành/hủy phiên
 * 
 * Tích hợp với:
 * - SpeakingSessionRepository (CRUD)
 * - User entity (verify user exists)
 * - CacheService (cache session data)
 */
public interface SpeakingSessionService {
    
    /**
     * Tạo một phiên luyện nói mới
     * 
     * Flow:
     * 1. Verify user exists
     * 2. Tạo SpeakingSession entity mới
     * 3. Lưu vào database
     * 4. Cache session info (TTL: 30 phút)
     * 5. Trả về response
     * 
     * @param userId ID của người dùng
     * @param request CreateSpeakingSessionRequest chứa topic, level, description
     * @return SpeakingSessionResponse của phiên vừa tạo
     * @throws IllegalArgumentException nếu user không tồn tại
     */
    SpeakingSessionResponse createSession(Long userId, CreateSpeakingSessionRequest request);
    
    /**
     * Lấy chi tiết một phiên luyện nói (bao gồm tất cả attempts)
     * 
     * Flow:
     * 1. Verify user ownership
     * 2. Lấy session từ cache hoặc DB
     * 3. Populate attempts và feedback (eager load)
     * 4. Cache result
     * 5. Trả về response
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng (verify ownership)
     * @return SpeakingSessionSummaryResponse chứa session + attempts + feedback
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    SpeakingSessionSummaryResponse getSessionDetail(Long sessionId, Long userId);
    
    /**
     * Lấy danh sách phiên của user (tất cả trạng thái)
     * 
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingSessionResponse> danh sách phiên
     */
    Page<SpeakingSessionResponse> listSessions(Long userId, Pageable pageable);
    
    /**
     * Lấy danh sách phiên theo trạng thái cụ thể
     * 
     * Dùng để lấy:
     * - Phiên đang diễn ra: IN_PROGRESS
     * - Phiên đã hoàn thành: COMPLETED
     * - Phiên đã hủy: CANCELLED
     * 
     * @param userId ID người dùng
     * @param status Trạng thái (IN_PROGRESS, COMPLETED, CANCELLED)
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingSessionResponse> danh sách phiên theo trạng thái
     */
    Page<SpeakingSessionResponse> listSessionsByStatus(Long userId, SpeakingStatus status, Pageable pageable);
    
    /**
     * Lấy danh sách phiên theo mức độ khó
     * 
     * Dùng để lọc phiên:
     * - Sơ cấp: A1-A2
     * - Trung cấp: B1-B2
     * - Nâng cao: C1-C2
     * 
     * @param userId ID người dùng
     * @param level Mức độ (A1, A2, B1, B2, C1, C2)
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingSessionResponse> danh sách phiên theo mức độ
     */
    Page<SpeakingSessionResponse> listSessionsByLevel(Long userId, SpeakingLevel level, Pageable pageable);
    
    /**
     * Lấy phiên gần nhất chưa hoàn thành
     * 
     * Dùng để "Tiếp tục phiên lần cuối"
     * 
     * @param userId ID người dùng
     * @return SpeakingSessionResponse của phiên gần nhất, hoặc null nếu không có
     */
    SpeakingSessionResponse getLatestInProgressSession(Long userId);
    
    /**
     * Lấy danh sách phiên trong khoảng thời gian
     * 
     * Dùng để thống kê phiên của tuần/tháng vừa qua
     * 
     * @param userId ID người dùng
     * @param startDate Thời gian bắt đầu
     * @param endDate Thời gian kết thúc
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingSessionResponse> phiên trong khoảng thời gian
     */
    Page<SpeakingSessionResponse> listSessionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Lấy danh sách phiên hoàn thành (sắp xếp theo điểm cao nhất)
     * 
     * Dùng để xem lịch sử học với kết quả tốt nhất
     * 
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<SpeakingSessionResponse> phiên hoàn thành sắp xếp theo điểm
     */
    Page<SpeakingSessionResponse> listCompletedSessionsSortedByScore(Long userId, Pageable pageable);
    
    /**
     * Hoàn thành một phiên luyện tập
     * 
     * Flow:
     * 1. Verify user ownership
     * 2. Cập nhật status = COMPLETED
     * 3. Tính điểm tổng thể từ attempts
     * 4. Lưu session
     * 5. Invalidate cache
     * 6. Trả về response
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @return SpeakingSessionResponse phiên đã cập nhật
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    SpeakingSessionResponse completeSession(Long sessionId, Long userId);
    
    /**
     * Hủy một phiên luyện tập
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @return SpeakingSessionResponse phiên đã hủy
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    SpeakingSessionResponse cancelSession(Long sessionId, Long userId);
    
    /**
     * Cập nhật điểm tổng thể của phiên từ attempts
     * 
     * Tính toán:
     * - Lấy tất cả attempts có feedback
     * - Lấy điểm overall của mỗi attempt
     * - Tính trung bình
     * - Cập nhật session.overallScore
     * 
     * @param sessionId ID phiên
     * @throws IllegalArgumentException nếu phiên không tồn tại
     */
    void updateSessionOverallScore(Long sessionId);
    
    /**
     * Xóa một phiên (xóa tất cả attempts và feedback)
     * 
     * CẢNH BÁO: Thao tác này không thể hoàn tác
     * 
     * @param sessionId ID phiên
     * @param userId ID người dùng
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    void deleteSession(Long sessionId, Long userId);
    
    /**
     * Đếm tổng số phiên của user
     * 
     * @param userId ID người dùng
     * @return Tổng số phiên
     */
    long countSessions(Long userId);
    
    /**
     * Đếm số phiên đang diễn ra của user
     * 
     * @param userId ID người dùng
     * @return Số phiên IN_PROGRESS
     */
    long countInProgressSessions(Long userId);
}
