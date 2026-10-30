package com.vocabularyai.placement.repository;

import com.vocabularyai.placement.entity.PlacementAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlacementAttemptRepository extends JpaRepository<PlacementAttempt, Long> {

    @Query("SELECT p FROM PlacementAttempt p WHERE p.user.id = :userId ORDER BY p.completedAt DESC")
    List<PlacementAttempt> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);

    default Optional<PlacementAttempt> findTopByUserId(Long userId) {
        List<PlacementAttempt> list = findLatestByUserId(userId, org.springframework.data.domain.PageRequest.of(0, 1));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    long countByUserId(Long userId);
}
