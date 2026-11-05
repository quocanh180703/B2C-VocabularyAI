package com.vocabularyai.ai.repository;

import com.vocabularyai.ai.entity.GrammarPracticeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrammarPracticeHistoryRepository extends JpaRepository<GrammarPracticeHistory, Long> {

    Page<GrammarPracticeHistory> findByUserId(Long userId, Pageable pageable);

    Page<GrammarPracticeHistory> findByUserIdAndMode(Long userId, String mode, Pageable pageable);

    Optional<GrammarPracticeHistory> findByIdAndUserId(Long id, Long userId);
}

