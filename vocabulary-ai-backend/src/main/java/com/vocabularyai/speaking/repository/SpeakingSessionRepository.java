package com.vocabularyai.speaking.repository;

import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SpeakingSession entity
 * 
 * Xử lý các thao tác cơ sở dữ liệu cho phiên luyện nói
 * - Lấy phiên của người dùng cụ thể
 * - Lọc theo trạng thái, mức độ
 * - Hỗ trợ phân trang
 */
@Repository
public interface SpeakingSessionRepository extends JpaRepository<SpeakingSession, Long> {
    
    /**
     * Lấy tất cả phiên của một người dùng (có phân trang)
     * 
     * Dùng để hiển thị danh sách phiên của user trên giao diện
     * 
     * @param userId ID của người dùng
     * @param pageable Thông tin phân trang
     * @return Trang chứa danh sách phiên
     */
    Page<SpeakingSession> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Lấy phiên cụ thể của người dùng (xác minh quyền truy cập)
     * 
     * Dùng để kiểm tra xem phiên có thuộc về user hay không
     * QUAN TRỌNG: Luôn sử dụng khi lấy phiên để verify user ownership
     * 
     * @param id ID của phiên
     * @param userId ID của người dùng
     * @return Optional chứa phiên nếu tìm thấy và thuộc về user
     */
    Optional<SpeakingSession> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Lấy các phiên chưa hoàn thành của người dùng
     * 
     * Dùng để tiếp tục phiên chưa xong, hoặc lấy phiên hiện tại
     * 
     * @param userId ID của người dùng
     * @param status Trạng thái (IN_PROGRESS, COMPLETED, CANCELLED)
     * @param pageable Thông tin phân trang
     * @return Trang chứa phiên có trạng thái cụ thể
     */
    Page<SpeakingSession> findByUserIdAndStatus(Long userId, SpeakingStatus status, Pageable pageable);
    
    /**
     * Lấy các phiên có mức độ cụ thể của người dùng
     * 
     * Dùng để lọc phiên theo mức độ (A1-C2)
     * 
     * @param userId ID của người dùng
     * @param level Mức độ khó
     * @param pageable Thông tin phân trang
     * @return Trang chứa phiên có mức độ cụ thể
     */
    Page<SpeakingSession> findByUserIdAndLevel(Long userId, SpeakingLevel level, Pageable pageable);
    
    /**
     * Lấy phiên gần đây nhất của người dùng (chưa hoàn thành)
     * 
     * Dùng để tiếp tục phiên lần cuối, hoặc kiểm tra có phiên đang chạy không
     * 
     * @param userId ID của người dùng
     * @param status Trạng thái (thường là IN_PROGRESS)
     * @return Optional chứa phiên gần nhất
     */
    @Query("SELECT s FROM SpeakingSession s WHERE s.user.id = :userId AND s.status = :status ORDER BY s.createdAt DESC LIMIT 1")
    Optional<SpeakingSession> findLatestByUserIdAndStatus(@Param("userId") Long userId, @Param("status") SpeakingStatus status);
    
    /**
     * Lấy danh sách phiên được tạo trong một khoảng thời gian
     * 
     * Dùng để thống kê, lấy phiên của tuần/tháng vừa qua
     * 
     * @param userId ID của người dùng
     * @param startDate Thời gian bắt đầu
     * @param endDate Thời gian kết thúc
     * @param pageable Thông tin phân trang
     * @return Trang chứa phiên trong khoảng thời gian cụ thể
     */
    @Query("SELECT s FROM SpeakingSession s WHERE s.user.id = :userId AND s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    Page<SpeakingSession> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    /**
     * Lấy các phiên hoàn thành của người dùng (có điểm tổng thể)
     * 
     * Dùng để thống kê tiến độ, xem lịch sử học
     * 
     * @param userId ID của người dùng
     * @param pageable Thông tin phân trang
     * @return Trang chứa phiên đã hoàn thành (sắp xếp theo điểm cao nhất trước)
     */
    @Query("SELECT s FROM SpeakingSession s WHERE s.user.id = :userId AND s.status = com.vocabularyai.speaking.entity.SpeakingStatus.COMPLETED ORDER BY s.overallScore DESC, s.completedAt DESC")
    Page<SpeakingSession> findCompletedSessionsByUserIdOrderByScore(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Đếm số phiên của người dùng
     * 
     * @param userId ID của người dùng
     * @return Tổng số phiên
     */
    long countByUserId(Long userId);
    
    /**
     * Đếm số phiên chưa hoàn thành của người dùng
     * 
     * @param userId ID của người dùng
     * @return Số phiên đang thực hiện
     */
    long countByUserIdAndStatus(Long userId, SpeakingStatus status);
    
    /**
     * Kiểm tra phiên có tồn tại không (xác minh quyền truy cập)
     * 
     * @param id ID của phiên
     * @param userId ID của người dùng
     * @return true nếu phiên tồn tại và thuộc về user
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    @Query("SELECT MAX(s.overallScore) FROM SpeakingSession s WHERE s.user.id = :userId AND s.status = com.vocabularyai.speaking.entity.SpeakingStatus.COMPLETED")
    Double findMaxOverallScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(s.overallScore) FROM SpeakingSession s WHERE s.user.id = :userId AND s.overallScore IS NOT NULL")
    Double findAverageOverallScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM SpeakingSession s WHERE s.user.id = :userId AND s.status = com.vocabularyai.speaking.entity.SpeakingStatus.COMPLETED ORDER BY s.completedAt DESC")
    List<SpeakingSession> findLatestCompletedSessions(@Param("userId") Long userId, Pageable pageable);

    default Optional<SpeakingSession> findLatestCompletedSession(Long userId) {
        List<SpeakingSession> list = findLatestCompletedSessions(userId, org.springframework.data.domain.PageRequest.of(0, 1));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
