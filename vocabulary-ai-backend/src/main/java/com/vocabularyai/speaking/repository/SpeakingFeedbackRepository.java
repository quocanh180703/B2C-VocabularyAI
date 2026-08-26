package com.vocabularyai.speaking.repository;

import com.vocabularyai.speaking.entity.SpeakingFeedback;
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
 * Repository for SpeakingFeedback entity
 * 
 * Xử lý các thao tác cơ sở dữ liệu cho feedback AI
 * - Lấy feedback cho attempts hoặc sessions
 * - Thống kê điểm số
 * - Hỗ trợ phân trang
 */
@Repository
public interface SpeakingFeedbackRepository extends JpaRepository<SpeakingFeedback, Long> {
    
    /**
     * Lấy feedback của một lần ghi âm cụ thể
     * 
     * Dùng để hiển thị feedback chi tiết khi user xem lại một attempt
     * 
     * @param attemptId ID của lần ghi âm
     * @return Optional chứa feedback nếu tìm thấy
     */
    Optional<SpeakingFeedback> findByAttemptId(Long attemptId);
    
    /**
     * Lấy feedback của một phiên
     * 
     * Dùng để lấy feedback tổng hợp của cả phiên
     * 
     * @param sessionId ID của phiên
     * @return Optional chứa feedback phiên nếu tìm thấy
     */
    Optional<SpeakingFeedback> findBySessionId(Long sessionId);
    
    /**
     * Lấy danh sách feedback của tất cả attempts trong một phiên
     * 
     * Dùng để lấy feedback của từng attempt khi user xem chi tiết phiên
     * 
     * @param sessionId ID của phiên
     * @return Danh sách feedback được sắp xếp theo thứ tự attempt
     */
    @Query("SELECT f FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL ORDER BY f.attempt.attemptNumber ASC")
    List<SpeakingFeedback> findAllAttemptFeedbackBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Lấy danh sách feedback của tất cả attempts trong phiên (có phân trang)
     * 
     * @param sessionId ID của phiên
     * @param pageable Thông tin phân trang
     * @return Trang chứa feedback của attempts
     */
    @Query("SELECT f FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL ORDER BY f.attempt.attemptNumber DESC")
    Page<SpeakingFeedback> findAllAttemptFeedbackBySessionIdPaginated(
            @Param("sessionId") Long sessionId,
            Pageable pageable
    );
    
    /**
     * Lấy danh sách feedback có điểm tổng thể cao nhất
     * 
     * Dùng để thống kê top attempts tốt nhất
     * 
     * @param sessionId ID của phiên
     * @param pageable Thông tin phân trang
     * @return Trang chứa feedback được sắp xếp theo điểm tổng thể cao nhất
     */
    @Query("SELECT f FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL ORDER BY f.overallScore DESC")
    Page<SpeakingFeedback> findAttemptFeedbackBySessionIdOrderByOverallScore(
            @Param("sessionId") Long sessionId,
            Pageable pageable
    );
    
    /**
     * Lấy feedback có điểm ngữ pháp cao nhất
     * 
     * Dùng để lọc attempts với ngữ pháp tốt
     * 
     * @param sessionId ID của phiên
     * @param pageable Thông tin phân trang
     * @return Trang chứa feedback được sắp xếp theo điểm grammar cao nhất
     */
    @Query("SELECT f FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL AND f.grammarScore IS NOT NULL ORDER BY f.grammarScore DESC")
    Page<SpeakingFeedback> findAttemptFeedbackBySessionIdOrderByGrammarScore(
            @Param("sessionId") Long sessionId,
            Pageable pageable
    );
    
    /**
     * Lấy feedback có điểm từ vựng cao nhất
     * 
     * Dùng để lọc attempts với từ vựng phong phú
     * 
     * @param sessionId ID của phiên
     * @param pageable Thông tin phân trang
     * @return Trang chứa feedback được sắp xếp theo điểm vocabulary cao nhất
     */
    @Query("SELECT f FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL AND f.vocabularyScore IS NOT NULL ORDER BY f.vocabularyScore DESC")
    Page<SpeakingFeedback> findAttemptFeedbackBySessionIdOrderByVocabularyScore(
            @Param("sessionId") Long sessionId,
            Pageable pageable
    );
    
    /**
     * Tính điểm trung bình của tất cả attempts trong phiên
     * 
     * Dùng để thống kê tiến độ học
     * 
     * @param sessionId ID của phiên
     * @return Điểm trung bình của phiên
     */
    @Query("SELECT AVG(f.overallScore) FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL")
    Optional<Double> calculateAverageScoreBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Tính điểm ngữ pháp trung bình của phiên
     * 
     * @param sessionId ID của phiên
     * @return Điểm grammar trung bình
     */
    @Query("SELECT AVG(f.grammarScore) FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL")
    Optional<Double> calculateAverageGrammarScoreBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Tính điểm từ vựng trung bình của phiên
     * 
     * @param sessionId ID của phiên
     * @return Điểm vocabulary trung bình
     */
    @Query("SELECT AVG(f.vocabularyScore) FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL")
    Optional<Double> calculateAverageVocabularyScoreBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Tính điểm trôi chảy trung bình của phiên
     * 
     * @param sessionId ID của phiên
     * @return Điểm fluency trung bình
     */
    @Query("SELECT AVG(f.fluencyScore) FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL")
    Optional<Double> calculateAverageFluencyScoreBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Tính điểm nội dung trung bình của phiên
     * 
     * @param sessionId ID của phiên
     * @return Điểm content trung bình
     */
    @Query("SELECT AVG(f.contentScore) FROM SpeakingFeedback f WHERE f.attempt.session.id = :sessionId AND f.attempt IS NOT NULL")
    Optional<Double> calculateAverageContentScoreBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Lấy feedback được tạo trong khoảng thời gian cụ thể
     * 
     * Dùng để thống kê feedback theo ngày/tuần/tháng
     * 
     * @param startDate Thời gian bắt đầu
     * @param endDate Thời gian kết thúc
     * @param pageable Thông tin phân trang
     * @return Trang chứa feedback được tạo trong khoảng thời gian
     */
    @Query("SELECT f FROM SpeakingFeedback f WHERE f.createdAt BETWEEN :startDate AND :endDate ORDER BY f.createdAt DESC")
    Page<SpeakingFeedback> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    /**
     * Đếm số feedback của phiên
     * 
     * @param sessionId ID của phiên
     * @return Số feedback của phiên
     */
    long countByAttemptSessionId(Long sessionId);
    
    /**
     * Kiểm tra xem feedback tồn tại cho attempt hay không
     * 
     * @param attemptId ID của attempt
     * @return true nếu có feedback cho attempt
     */
    boolean existsByAttemptId(Long attemptId);
    
    /**
     * Kiểm tra xem feedback tồn tại cho session hay không
     * 
     * @param sessionId ID của phiên
     * @return true nếu có feedback cho phiên
     */
    boolean existsBySessionId(Long sessionId);
}
