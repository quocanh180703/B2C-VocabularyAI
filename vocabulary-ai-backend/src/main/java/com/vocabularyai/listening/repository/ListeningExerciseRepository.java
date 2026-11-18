package com.vocabularyai.listening.repository;

import com.vocabularyai.listening.entity.ListeningExercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListeningExerciseRepository extends JpaRepository<ListeningExercise, Long> {

    Page<ListeningExercise> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<ListeningExercise> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT AVG(l.score) FROM ListeningExercise l WHERE l.userId = :userId AND l.status = 'COMPLETED'")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(l.score) FROM ListeningExercise l WHERE l.userId = :userId AND l.status = 'COMPLETED'")
    Double getMaxScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM ListeningExercise l WHERE l.userId = :userId AND l.status = 'COMPLETED'")
    long countCompletedByUserId(@Param("userId") Long userId);
}

