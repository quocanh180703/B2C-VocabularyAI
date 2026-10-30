package com.vocabularyai.vocabulary.repository;

import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.entity.PartOfSpeech;
import com.vocabularyai.vocabulary.entity.WordTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
     * Find first word by lemma (case-insensitive)
     */
    Optional<CefrWord> findFirstByLemmaIgnoreCase(String lemma);

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
           "LOWER(w.definition) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY CASE " +
           "WHEN LOWER(w.lemma) = LOWER(:query) THEN 0 " +
           "WHEN LOWER(w.lemma) LIKE LOWER(CONCAT(:query, '%')) THEN 1 " +
           "ELSE 2 END, " +
           "CASE " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.A1 THEN 1 " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.A2 THEN 2 " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.B1 THEN 3 " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.B2 THEN 4 " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.C1 THEN 5 " +
           "ELSE 6 END, " +
           "LENGTH(w.lemma) ASC, w.lemma ASC")
    Page<CefrWord> searchByLemmaOrDefinition(@Param("query") String query, Pageable pageable);

    /**
     * Search lemma only (prefix match for autocomplete)
     * 
     * @param prefix Word prefix (e.g., "hel" for "hello", "help")
     * @param pageable Pagination info
     * @return Page of matching words
     */
    @Query("SELECT w FROM CefrWord w WHERE LOWER(w.lemma) LIKE LOWER(CONCAT(:prefix, '%')) " +
           "ORDER BY CASE WHEN LOWER(w.lemma) = LOWER(:prefix) THEN 0 ELSE 1 END, " +
           "CASE " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.A1 THEN 1 " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.A2 THEN 2 " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.B1 THEN 3 " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.B2 THEN 4 " +
           "WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.C1 THEN 5 " +
           "ELSE 6 END, " +
           "LENGTH(w.lemma) ASC, w.lemma ASC")
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

    /**
     * Find random words at specific level (or all levels if level is null/empty)
     *
     * @param level CEFR level (optional)
     * @param limit Number of words to return
     * @return List of random CefrWord entities
     */
    @Query(value = "SELECT * FROM cefr_words WHERE (:level IS NULL OR :level = '' OR cefr_level = :level) ORDER BY RAND() LIMIT :limit",
           nativeQuery = true)
    List<CefrWord> findRandomWordsByLevel(@Param("level") String level, @Param("limit") int limit);

    // ==================== AI Vocabulary Studio Audit & Enrichment Queries ====================

    @Query("SELECT COUNT(w) FROM CefrWord w WHERE w.vietnameseMeaning IS NULL OR TRIM(w.vietnameseMeaning) = ''")
    long countWordsMissingMeaning();

    @Query("SELECT COUNT(w) FROM CefrWord w WHERE w.pronunciation IS NULL OR TRIM(w.pronunciation) = ''")
    long countWordsMissingPronunciation();

    @Query("SELECT COUNT(w) FROM CefrWord w WHERE w.definition IS NULL OR TRIM(w.definition) = ''")
    long countWordsMissingDefinition();

    @Query("SELECT COUNT(w) FROM CefrWord w WHERE w.example IS NULL OR TRIM(w.example) = ''")
    long countWordsMissingExample();

    @Query("SELECT w FROM CefrWord w WHERE w.vietnameseMeaning IS NULL OR TRIM(w.vietnameseMeaning) = '' ORDER BY w.lemma ASC")
    Page<CefrWord> findWordsMissingMeaning(Pageable pageable);

    @Query("SELECT w FROM CefrWord w WHERE w.pronunciation IS NULL OR TRIM(w.pronunciation) = '' ORDER BY w.lemma ASC")
    Page<CefrWord> findWordsMissingPronunciation(Pageable pageable);

    @Query("SELECT w FROM CefrWord w WHERE w.definition IS NULL OR TRIM(w.definition) = '' ORDER BY w.lemma ASC")
    Page<CefrWord> findWordsMissingDefinition(Pageable pageable);

    @Query("SELECT w FROM CefrWord w WHERE w.example IS NULL OR TRIM(w.example) = '' ORDER BY w.lemma ASC")
    Page<CefrWord> findWordsMissingExample(Pageable pageable);

    // ==================== Topic Filter, Group Counts & Backfill Queries ====================

    @Query("SELECT w FROM CefrWord w WHERE " +
           "(:level IS NULL OR w.cefrLevel = :level) AND " +
           "(:topic IS NULL OR w.topic = :topic) AND " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(w.lemma) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(w.definition) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY CASE " +
           "  WHEN :query IS NOT NULL AND :query <> '' AND LOWER(w.lemma) = LOWER(:query) THEN 0 " +
           "  WHEN :query IS NOT NULL AND :query <> '' AND LOWER(w.lemma) LIKE LOWER(CONCAT(:query, '%')) THEN 1 " +
           "  ELSE 2 END, " +
           "CASE " +
           "  WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.A1 THEN 1 " +
           "  WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.A2 THEN 2 " +
           "  WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.B1 THEN 3 " +
           "  WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.B2 THEN 4 " +
           "  WHEN w.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.C1 THEN 5 " +
           "  ELSE 6 END, " +
           "LENGTH(w.lemma) ASC, w.lemma ASC")
    Page<CefrWord> searchAdvanced(
            @Param("query") String query,
            @Param("level") CEFRLevel level,
            @Param("topic") WordTopic topic,
            Pageable pageable
    );

    @Query("SELECT w.topic, COUNT(w) FROM CefrWord w " +
           "WHERE (:level IS NULL OR w.cefrLevel = :level) " +
           "  AND w.topic IS NOT NULL " +
           "GROUP BY w.topic")
    List<Object[]> countWordsGroupedByTopic(@Param("level") CEFRLevel level);

    @Query("SELECT w FROM CefrWord w WHERE w.topic IS NULL ORDER BY w.lemma ASC")
    Page<CefrWord> findWordsMissingTopic(Pageable pageable);

    @Query("SELECT w FROM CefrWord w WHERE w.topic IS NULL AND (:lastId IS NULL OR w.id > :lastId) ORDER BY w.id ASC")
    List<CefrWord> findWordsMissingTopicAfterId(@Param("lastId") String lastId, Pageable pageable);

    @Query("SELECT COUNT(w) FROM CefrWord w WHERE w.topic IS NULL")
    long countWordsMissingTopic();

    Page<CefrWord> findByTopic(WordTopic topic, Pageable pageable);

    Page<CefrWord> findByCefrLevelAndTopic(CEFRLevel level, WordTopic topic, Pageable pageable);

    long countByTopic(WordTopic topic);

    long countByCefrLevelAndTopic(CEFRLevel level, WordTopic topic);

    // ==================== Scoped Topic Backfill Queries ====================

    @Query("SELECT w FROM CefrWord w WHERE w.topic IS NULL AND w.id IN :ids ORDER BY w.lemma ASC")
    List<CefrWord> findWordsMissingTopicByIds(@Param("ids") List<String> ids);

    @Query("SELECT COUNT(w) FROM CefrWord w WHERE w.topic IS NULL AND " +
           "(:createdAfter IS NULL OR w.createdAt >= :createdAfter) AND " +
           "(:source IS NULL OR :source = '' OR w.source = :source)")
    long countWordsMissingTopicByImport(
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("source") String source
    );

    @Query("SELECT w FROM CefrWord w WHERE w.topic IS NULL AND " +
           "(:createdAfter IS NULL OR w.createdAt >= :createdAfter) AND " +
           "(:source IS NULL OR :source = '' OR w.source = :source) AND " +
           "(:lastId IS NULL OR w.id > :lastId) " +
           "ORDER BY w.id ASC")
    List<CefrWord> findWordsMissingTopicByImportAfterId(
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("source") String source,
            @Param("lastId") String lastId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT w.source FROM CefrWord w WHERE w.source IS NOT NULL AND TRIM(w.source) <> '' ORDER BY w.source ASC")
    List<String> findDistinctSources();
}
