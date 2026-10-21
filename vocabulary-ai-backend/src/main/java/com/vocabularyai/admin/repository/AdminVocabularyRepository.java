package com.vocabularyai.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vocabularyai.admin.entity.AdminQueryResult;

import java.util.List;

/**
 * Admin Vocabulary Repository
 * 
 * Provides queries for vocabulary management (admin system-wide view)
 * Supports listing, searching, and filtering CEFR words
 */
@Repository
public interface AdminVocabularyRepository extends JpaRepository<AdminQueryResult, Long> {
    
    // ==================== CEFR Word List ====================
    
    /**
     * Get paginated list of all CEFR words
     * 
     * @param pageable Pagination and sorting
     * @return Page of vocabulary words
     */
    @Query("SELECT new map(" +
           "cw.id as id, " +
           "cw.lemma as word, " +
           "cw.pronunciation as pronunciation, " +
           "cw.partOfSpeech as partOfSpeech, " +
           "cw.cefrLevel as cefr, " +
           "cw.vietnameseMeaning as vietnameseMeaning, " +
           "COALESCE(cw.vietnameseMeaning, cw.definition) as meaning, " +
           "cw.definition as definition, " +
           "cw.source as source, " +
           "cw.createdAt as createdAt) " +
           "FROM CefrWord cw " +
           "ORDER BY cw.lemma ASC")
    Page<?> getVocabularyList(Pageable pageable);
    
    /**
     * Get CEFR words filtered by level
     * 
     * @param level CEFR level (A1, A2, B1, B2, C1)
     * @param pageable Pagination
     * @return Page of words at this level
     */
    @Query("SELECT new map(" +
           "cw.id as id, " +
           "cw.lemma as word, " +
           "cw.pronunciation as pronunciation, " +
           "cw.partOfSpeech as partOfSpeech, " +
           "cw.cefrLevel as cefr, " +
           "cw.vietnameseMeaning as vietnameseMeaning, " +
           "COALESCE(cw.vietnameseMeaning, cw.definition) as meaning, " +
           "cw.definition as definition, " +
           "cw.source as source, " +
           "cw.createdAt as createdAt) " +
           "FROM CefrWord cw " +
           "WHERE cw.cefrLevel = :level " +
           "ORDER BY cw.lemma ASC")
    Page<?> getVocabularyByLevel(@Param("level") String level, Pageable pageable);
    
    /**
     * Search words by lemma or definition
     * 
     * @param searchTerm Search query (case-insensitive)
     * @param pageable Pagination
     * @return Page of matching words
     */
    @Query("SELECT new map(" +
           "cw.id as id, " +
           "cw.lemma as word, " +
           "cw.pronunciation as pronunciation, " +
           "cw.partOfSpeech as partOfSpeech, " +
           "cw.cefrLevel as cefr, " +
           "cw.vietnameseMeaning as vietnameseMeaning, " +
           "COALESCE(cw.vietnameseMeaning, cw.definition) as meaning, " +
           "cw.definition as definition, " +
           "cw.source as source, " +
           "cw.createdAt as createdAt) " +
           "FROM CefrWord cw " +
           "WHERE LOWER(cw.lemma) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cw.definition) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(cw.vietnameseMeaning) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY CASE " +
           "WHEN LOWER(cw.lemma) = LOWER(:searchTerm) THEN 0 " +
           "WHEN LOWER(cw.lemma) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 1 " +
           "ELSE 2 END, " +
           "CASE " +
           "WHEN cw.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.A1 THEN 1 " +
           "WHEN cw.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.A2 THEN 2 " +
           "WHEN cw.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.B1 THEN 3 " +
           "WHEN cw.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.B2 THEN 4 " +
           "WHEN cw.cefrLevel = com.vocabularyai.vocabulary.entity.CEFRLevel.C1 THEN 5 " +
           "ELSE 6 END, " +
           "LENGTH(cw.lemma) ASC, cw.lemma ASC")
    Page<?> searchVocabulary(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Get words by part of speech with CEFR level filter
     * 
     * @param pos Part of speech (NOUN, VERB, ADJECTIVE, etc.)
     * @param level CEFR level (optional filter)
     * @param pageable Pagination
     * @return Page of matching words
     */
    @Query("SELECT new map(" +
           "cw.id as id, " +
           "cw.lemma as word, " +
           "cw.pronunciation as pronunciation, " +
           "cw.partOfSpeech as partOfSpeech, " +
           "cw.cefrLevel as cefr, " +
           "cw.vietnameseMeaning as vietnameseMeaning, " +
           "COALESCE(cw.vietnameseMeaning, cw.definition) as meaning, " +
           "cw.definition as definition, " +
           "cw.source as source, " +
           "cw.createdAt as createdAt) " +
           "FROM CefrWord cw " +
           "WHERE cw.partOfSpeech = :pos AND " +
           "(:level IS NULL OR cw.cefrLevel = :level) " +
           "ORDER BY cw.lemma ASC")
    Page<?> getVocabularyByPartOfSpeech(@Param("pos") String pos, @Param("level") String level, Pageable pageable);
    
    // ==================== Vocabulary Detail ====================
    
    /**
     * Get complete vocabulary word information
     * 
     * @param wordId Word ID
     * @return Map with word details
     */
    @Query("SELECT new map(" +
           "cw.id as id, " +
           "cw.lemma as word, " +
           "cw.pronunciation as pronunciation, " +
           "cw.partOfSpeech as partOfSpeech, " +
           "cw.cefrLevel as cefr, " +
           "cw.vietnameseMeaning as vietnameseMeaning, " +
           "COALESCE(cw.vietnameseMeaning, cw.definition) as meaning, " +
           "cw.definition as definition, " +
           "cw.source as source, " +
           "cw.language as language, " +
           "0 as usageCount, " +
           "cw.createdAt as createdAt, " +
           "cw.updatedAt as updatedAt) " +
           "FROM CefrWord cw " +
           "WHERE cw.id = :wordId")
    List<?> getVocabularyDetail(@Param("wordId") String wordId);
    
    // ==================== Statistics ====================
    
    /**
     * Get word count by CEFR level
     * 
     * @return Map with counts per level
     */
    @Query("SELECT new map(" +
           "cw.cefrLevel as level, " +
           "COUNT(cw) as count) " +
           "FROM CefrWord cw " +
           "GROUP BY cw.cefrLevel")
    List<?> getCefrStatistics();
    
    /**
     * Get total unique words (including user vocabulary)
     * 
     * @return Total word count
     */
    @Query("SELECT COUNT(DISTINCT cw.id) FROM CefrWord cw")
    Long getTotalVocabularyCount();
}
