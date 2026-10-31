package com.vocabularyai.speaking.repository;

import com.vocabularyai.speaking.entity.SpeakingAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SpeakingAttempt entity
 * 
 * Xử lý các thao tác cơ sở dữ liệu cho các lần ghi âm
 * - Lấy các lần ghi âm của một phiên
 * - Lấy lần ghi âm với feedback
 * - Hỗ trợ phân trang
 */
@Repository
public interface SpeakingAttemptRepository extends JpaRepository<SpeakingAttempt, Long> {
    
    /**
     * Lấy tất cả lần ghi âm của một phiên (có phân trang)
     * 
     * Dùng để hiển thị danh sách các lần ghi âm trong phiên
     * 
     * @param sessionId ID của phiên
     * @param pageable Thông tin phân trang
     * @return Trang chứa danh sách lần ghi âm
     */
    Page<SpeakingAttempt> findBySessionId(Long sessionId, Pageable pageable);
    
    /**
     * Lấy danh sách tất cả lần ghi âm của một phiên (không phân trang)
     * 
     * Dùng khi cần lấy toàn bộ attempts của phiên (để tính điểm trung bình, v.v.)
     * 
     * @param sessionId ID của phiên
     * @return Danh sách lần ghi âm được sắp xếp theo thứ tự attempt
     */
    @Query("SELECT a FROM SpeakingAttempt a WHERE a.session.id = :sessionId ORDER BY a.attemptNumber ASC")
    List<SpeakingAttempt> findBySessionIdOrderByAttemptNumber(@Param("sessionId") Long sessionId);
    
    /**
     * Lấy lần ghi âm cụ thể với thông tin feedback
     * 
     * Dùng khi cần xem chi tiết một lần ghi âm cùng feedback
     * 
     * @param id ID của lần ghi âm
     * @return Optional chứa lần ghi âm nếu tìm thấy
     */
    Optional<SpeakingAttempt> findById(Long id);
    
    /**
     * Lấy lần ghi âm cuối cùng của một phiên
     * 
     * Dùng để lấy attempt mới nhất trong phiên (nếu cần tiếp tục)
     * 
     * @param sessionId ID của phiên
     * @return Optional chứa lần ghi âm mới nhất
     */
    @Query("SELECT a FROM SpeakingAttempt a WHERE a.session.id = :sessionId ORDER BY a.attemptNumber DESC LIMIT 1")
    Optional<SpeakingAttempt> findLatestBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Lấy lần ghi âm cụ thể theo số thứ tự trong phiên
     * 
     * @param sessionId ID của phiên
     * @param attemptNumber Số thứ tự (1, 2, 3, ...)
     * @return Optional chứa lần ghi âm nếu tìm thấy
     */
    @Query("SELECT a FROM SpeakingAttempt a WHERE a.session.id = :sessionId AND a.attemptNumber = :attemptNumber")
    Optional<SpeakingAttempt> findBySessionIdAndAttemptNumber(
            @Param("sessionId") Long sessionId,
            @Param("attemptNumber") Integer attemptNumber
    );
    
    /**
     * Lấy số lần ghi âm của một phiên
     * 
     * Dùng để xác định số thứ tự của attempt tiếp theo
     * 
     * @param sessionId ID của phiên
     * @return Số lần ghi âm
     */
    long countBySessionId(Long sessionId);
    
    /**
     * Lấy các lần ghi âm có độ tin cậy transcript cao nhất
     * 
     * Dùng để lọc attempts có chất lượng tốt (transcript rõ ràng)
     * 
     * @param sessionId ID của phiên
     * @param minConfidence Độ tin cậy tối thiểu (0-100)
     * @param pageable Thông tin phân trang
     * @return Trang chứa lần ghi âm có độ tin cậy cao
     */
    @Query("SELECT a FROM SpeakingAttempt a WHERE a.session.id = :sessionId AND a.transcriptConfidence >= :minConfidence ORDER BY a.transcriptConfidence DESC")
    Page<SpeakingAttempt> findBySessionIdAndHighConfidence(
            @Param("sessionId") Long sessionId,
            @Param("minConfidence") Double minConfidence,
            Pageable pageable
    );
    
    /**
     * Lấy các lần ghi âm có feedback
     * 
     * Dùng để lấy attempts đã được AI đánh giá
     * 
     * @param sessionId ID của phiên
     * @param pageable Thông tin phân trang
     * @return Trang chứa lần ghi âm có feedback
     */
    @Query("SELECT a FROM SpeakingAttempt a WHERE a.session.id = :sessionId AND a.feedback IS NOT NULL ORDER BY a.attemptNumber DESC")
    Page<SpeakingAttempt> findBySessionIdWithFeedback(@Param("sessionId") Long sessionId, Pageable pageable);
    
    /**
     * Kiểm tra xem lần ghi âm có thuộc về phiên không
     * 
     * Dùng để xác minh quyền truy cập (authorization check)
     * 
     * @param id ID của lần ghi âm
     * @param sessionId ID của phiên
     * @return true nếu lần ghi âm thuộc về phiên
     */
    boolean existsByIdAndSessionId(Long id, Long sessionId);
    
    /**
     * Xóa tất cả lần ghi âm của một phiên
     * 
     * CẢNH BÁO: Thao tác này sẽ xóa toàn bộ attempts của phiên
     * Thường dùng khi xóa phiên (cascade delete sẽ xử lý)
     * 
     * @param sessionId ID của phiên
     */
    void deleteBySessionId(Long sessionId);

    @Query("SELECT COUNT(a) FROM SpeakingAttempt a WHERE a.session.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
