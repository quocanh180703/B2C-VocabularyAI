package com.vocabularyai.ai.repository;

import com.vocabularyai.ai.entity.GrammarPracticeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrammarPracticeHistoryRepository extends JpaRepository<GrammarPracticeHistory, Long> {

    Page<GrammarPracticeHistory> findByUserId(Long userId, Pageable pageable);

    Page<GrammarPracticeHistory> findByUserIdAndMode(Long userId, String mode, Pageable pageable);

    Optional<GrammarPracticeHistory> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT AVG(g.score) FROM GrammarPracticeHistory g WHERE g.userId = :userId")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(g.score) FROM GrammarPracticeHistory g WHERE g.userId = :userId")
    Double getMaxScoreByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);

    @Query("SELECT g.mode, AVG(g.score) FROM GrammarPracticeHistory g WHERE g.userId = :userId GROUP BY g.mode")
    List<Object[]> getAverageScoresByMode(@Param("userId") Long userId);
}

