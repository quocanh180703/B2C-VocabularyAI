package com.vocabularyai.vocabulary.repository;

import com.vocabularyai.vocabulary.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Word entity
 */
@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    
    /**
     * Find word by word string
     */
    Optional<Word> findByWord(String word);
    
    /**
     * Find words by category with pagination
     */
    Page<Word> findByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * Find words by category and level with pagination
     */
    Page<Word> findByCategoryIdAndLevel(Long categoryId, Word.WordLevel level, Pageable pageable);
    
    /**
     * Search words by keyword (word, meaning, or definition)
     */
    @Query("SELECT w FROM Word w WHERE " +
           "LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.definition) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Word> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Search words by keyword and level
     */
    @Query("SELECT w FROM Word w WHERE " +
           "(LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.definition) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "w.level = :level")
    Page<Word> searchByKeywordAndLevel(@Param("keyword") String keyword, @Param("level") Word.WordLevel level, Pageable pageable);
    
    /**
     * Search words by keyword and category
     */
    @Query("SELECT w FROM Word w WHERE " +
           "w.category.id = :categoryId AND " +
           "(LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.definition) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Word> searchByKeywordAndCategory(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Search words by keyword, category and level
     */
    @Query("SELECT w FROM Word w WHERE " +
           "w.category.id = :categoryId AND " +
           "w.level = :level AND " +
           "(LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.definition) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Word> searchByKeywordCategoryAndLevel(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("level") Word.WordLevel level, Pageable pageable);
}
