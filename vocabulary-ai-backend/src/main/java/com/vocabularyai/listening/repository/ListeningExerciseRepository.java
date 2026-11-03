package com.vocabularyai.listening.repository;

import com.vocabularyai.listening.entity.ListeningExercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListeningExerciseRepository extends JpaRepository<ListeningExercise, Long> {

    Page<ListeningExercise> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<ListeningExercise> findByIdAndUserId(Long id, Long userId);
}

