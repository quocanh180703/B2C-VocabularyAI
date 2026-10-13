package com.vocabularyai.vocabulary.repository;

import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.entity.PartOfSpeech;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CefrWordRepository
 * 
 * Data access layer for CefrWord entity
 * Supports queries: by lemma, level, POS, full-text search
 */
@Repository
public interface CefrWordRepository extends JpaRepository<CefrWord, String> {

    /**
     * Find word by lemma (word text)
     * 
     * @param lemma Word text (e.g., "hello", "goodbye")
     * @return CefrWord if exists, empty if not
     */
    Optional<CefrWord> findByLemma(String lemma);

    /**
     * Check if word exists (case-insensitive)
     */
    boolean existsByLemmaIgnoreCase(String lemma);

    /**
     * Find all words at specific CEFR level (paginated)
     * 
     * @param level CEFR level (A1, A2, B1, B2, C1)
     * @param pageable Pagination info
     * @return Page of words
     */
    Page<CefrWord> findByCefrLevel(CEFRLevel level, Pageable pageable);

    /**
     * Find all words by part of speech (paginated)
     * 
     * @param pos Part of speech (NOUN, VERB, ADJECTIVE, etc.)
     * @param pageable Pagination info
     * @return Page of words
     */
    Page<CefrWord> findByPartOfSpeech(PartOfSpeech pos, Pageable pageable);

    /**
     * Find words by multiple levels (paginated)
     * 
     * @param levels List of CEFR levels
     * @param pageable Pagination info
     * @return Page of words
     */
    Page<CefrWord> findByCefrLevelIn(List<CEFRLevel> levels, Pageable pageable);

    /**
     * Full-text search: lemma and definition combined
     * 
     * Uses LIKE for lemma, FULLTEXT for definition
     * Case-insensitive
     * 
     * @param query Search keyword
     * @param pageable Pagination info
     * @return Page of matching words
     */
    @Query("SELECT w FROM CefrWord w WHERE " +
           "LOWER(w.lemma) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(w.definition) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<CefrWord> searchByLemmaOrDefinition(@Param("query") String query, Pageable pageable);

    /**
     * Search lemma only (prefix match for autocomplete)
     * 
     * @param prefix Word prefix (e.g., "hel" for "hello", "help")
     * @param pageable Pagination info
     * @return Page of matching words
     */
    @Query("SELECT w FROM CefrWord w WHERE LOWER(w.lemma) LIKE LOWER(CONCAT(:prefix, '%')) " +
           "ORDER BY w.lemma ASC")
    Page<CefrWord> searchByLemmaPrefix(@Param("prefix") String prefix, Pageable pageable);

    /**
     * Count words by CEFR level
     * 
     * @param level CEFR level
     * @return Count of words at this level
     */
    long countByCefrLevel(CEFRLevel level);

    /**
     * Get all words at level (no pagination)
     * 
     * Use with caution for large levels
     * 
     * @param level CEFR level
     * @return List of words (unordered, all results)
     */
    List<CefrWord> findByCefrLevel(CEFRLevel level);

    /**
     * Count total words in database
     * 
     * @return Total word count
     */
    long count();

    /**
     * Check if word exists by ID
     */
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN TRUE ELSE FALSE END FROM CefrWord w WHERE w.id = :id")
    boolean existsById(@Param("id") String id);

    /**
     * Find random word at specific level (for daily challenge)
     * 
     * @param level CEFR level
     * @return Random word, or empty if no words at level
     */
    @Query(value = "SELECT * FROM cefr_words WHERE cefr_level = :level ORDER BY RAND() LIMIT 1",
           nativeQuery = true)
    Optional<CefrWord> findRandomByLevel(@Param("level") String level);
}
