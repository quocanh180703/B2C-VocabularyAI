package com.vocabularyai.ai.repository;

import com.vocabularyai.ai.entity.AiRequestLog;
import com.vocabularyai.ai.entity.AiRequestLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {

    Page<AiRequestLog> findByCreatedAtAfter(LocalDateTime since, Pageable pageable);

    Page<AiRequestLog> findByFeatureAndCreatedAtAfter(String feature, LocalDateTime since, Pageable pageable);

    Page<AiRequestLog> findByStatusAndCreatedAtAfter(AiRequestLogStatus status, LocalDateTime since, Pageable pageable);

    @Query("SELECT COUNT(l) FROM AiRequestLog l WHERE l.createdAt >= :since")
    long countRequestsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(l.totalTokens), 0) FROM AiRequestLog l WHERE l.createdAt >= :since")
    long sumTotalTokensSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(l.promptTokens), 0) FROM AiRequestLog l WHERE l.createdAt >= :since")
    long sumPromptTokensSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(l.completionTokens), 0) FROM AiRequestLog l WHERE l.createdAt >= :since")
    long sumCompletionTokensSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(l.estimatedCostUsd), 0) FROM AiRequestLog l WHERE l.createdAt >= :since")
    BigDecimal sumCostUsdSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(AVG(l.latencyMs), 0) FROM AiRequestLog l WHERE l.createdAt >= :since")
    double avgLatencyMsSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(l) FROM AiRequestLog l WHERE l.status = :status AND l.createdAt >= :since")
    long countByStatusSince(@Param("status") AiRequestLogStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT l.feature, COUNT(l), COALESCE(SUM(l.totalTokens), 0), COALESCE(SUM(l.estimatedCostUsd), 0), COALESCE(AVG(l.latencyMs), 0) " +
           "FROM AiRequestLog l WHERE l.createdAt >= :since GROUP BY l.feature")
    List<Object[]> getFeatureBreakdownSince(@Param("since") LocalDateTime since);

    @Query("SELECT FUNCTION('DATE', l.createdAt), COUNT(l), COALESCE(SUM(l.totalTokens), 0), COALESCE(SUM(l.estimatedCostUsd), 0) " +
           "FROM AiRequestLog l WHERE l.createdAt >= :since GROUP BY FUNCTION('DATE', l.createdAt) ORDER BY FUNCTION('DATE', l.createdAt) ASC")
    List<Object[]> getDailyTrendSince(@Param("since") LocalDateTime since);

    @Query("SELECT l FROM AiRequestLog l WHERE l.createdAt >= :since AND " +
           "(l.totalTokens > :tokenThreshold OR l.latencyMs > :latencyThreshold OR l.status != 'SUCCESS') " +
           "ORDER BY l.createdAt DESC")
    List<AiRequestLog> findAnomaliesSince(
            @Param("since") LocalDateTime since,
            @Param("tokenThreshold") int tokenThreshold,
            @Param("latencyThreshold") long latencyThreshold,
            Pageable pageable
    );
}
