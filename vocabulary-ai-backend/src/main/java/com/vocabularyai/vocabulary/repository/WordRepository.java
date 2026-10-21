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
 * WordRepository
 * 
 * Data access layer for Word entity (User Vocabulary)
 * This is for user-created vocabulary entries, not CEFR dataset
 * For CEFR words, use CefrWordRepository instead
 */
@Repository
public interface WordRepository extends JpaRepository<Word, Long> {

    /**
     * Find word by word text
     * 
     * @param word Word text
     * @return Word if exists, empty if not
     */
    Optional<Word> findByWord(String word);

    /**
     * Search words by word text or meaning (paginated)
     * 
     * @param query Search keyword
     * @param pageable Pagination info
     * @return Page of matching words
     */
    @Query("SELECT w FROM Word w WHERE " +
           "LOWER(w.word) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(w.meaning) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Word> searchByWordOrMeaning(@Param("query") String query, Pageable pageable);

    /**
     * Find words by level (paginated)
     * 
     * @param level CEFR level string (A1, A2, B1, B2, C1, C2)
     * @param pageable Pagination info
     * @return Page of words at this level
     */
    Page<Word> findByLevel(String level, Pageable pageable);

    /**
     * Count words by level
     */
    long countByLevel(String level);

    @Query("SELECT COUNT(w) FROM Word w WHERE w.meaning IS NULL OR TRIM(w.meaning) = ''")
    long countWordsMissingMeaning();

    @Query("SELECT COUNT(w) FROM Word w WHERE w.example IS NULL OR TRIM(w.example) = ''")
    long countWordsMissingExample();

    @Query("SELECT COUNT(w) FROM Word w WHERE w.pronunciation IS NULL OR TRIM(w.pronunciation) = ''")
    long countWordsMissingPronunciation();

    @Query("SELECT COUNT(w) FROM Word w WHERE w.definition IS NULL OR TRIM(w.definition) = ''")
    long countWordsMissingDefinition();

    @Query("SELECT w FROM Word w WHERE w.example IS NULL OR TRIM(w.example) = '' ORDER BY w.id ASC")
    Page<Word> findWordsMissingExample(Pageable pageable);

    @Query("SELECT w FROM Word w WHERE w.meaning IS NULL OR TRIM(w.meaning) = '' ORDER BY w.id ASC")
    Page<Word> findWordsMissingMeaning(Pageable pageable);

    @Query("SELECT w FROM Word w WHERE w.pronunciation IS NULL OR TRIM(w.pronunciation) = '' ORDER BY w.id ASC")
    Page<Word> findWordsMissingPronunciation(Pageable pageable);

    @Query("SELECT w FROM Word w WHERE w.definition IS NULL OR TRIM(w.definition) = '' ORDER BY w.id ASC")
    Page<Word> findWordsMissingDefinition(Pageable pageable);
}
