package com.vocabularyai.vocabulary.repository;

import com.vocabularyai.vocabulary.entity.FavoriteWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for FavoriteWord entity
 */
@Repository
public interface FavoriteWordRepository extends JpaRepository<FavoriteWord, Long> {
    
    /**
     * Find favorite word by user and word
     */
    Optional<FavoriteWord> findByUserIdAndWordId(Long userId, Long wordId);
    
    /**
     * Check if word is favorite for user
     */
    boolean existsByUserIdAndWordId(Long userId, Long wordId);
    
    /**
     * Get all favorite words for user with pagination
     */
    @org.springframework.data.jpa.repository.Query(
        value = "SELECT fw FROM FavoriteWord fw JOIN FETCH fw.word WHERE fw.user.id = :userId",
        countQuery = "SELECT COUNT(fw) FROM FavoriteWord fw WHERE fw.user.id = :userId"
    )
    Page<FavoriteWord> findByUserId(@org.springframework.data.repository.query.Param("userId") Long userId, Pageable pageable);
    
    /**
     * Delete favorite word for user
     */
    void deleteByUserIdAndWordId(Long userId, Long wordId);
    
    /**
     * Count favorite words for user
     */
    long countByUserId(Long userId);
}
