package com.vocabularyai.report.repository;

import com.vocabularyai.report.entity.ContentReport;
import com.vocabularyai.report.entity.ContentReportStatus;
import com.vocabularyai.report.entity.ContentReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    boolean existsByUserIdAndCefrWordIdAndStatus(Long userId, String cefrWordId, ContentReportStatus status);

    long countByUserIdAndStatus(Long userId, ContentReportStatus status);

    Page<ContentReport> findByStatus(ContentReportStatus status, Pageable pageable);

    Page<ContentReport> findByType(ContentReportType type, Pageable pageable);

    Page<ContentReport> findByStatusAndType(ContentReportStatus status, ContentReportType type, Pageable pageable);
}
