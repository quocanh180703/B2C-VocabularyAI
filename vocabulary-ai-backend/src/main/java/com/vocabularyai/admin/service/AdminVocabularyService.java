package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateVocabularyRequest;
import com.vocabularyai.admin.dto.request.UpdateVocabularyRequest;
import com.vocabularyai.admin.dto.response.CefrStatsResponse;
import com.vocabularyai.admin.dto.response.CategoryListResponse;
import com.vocabularyai.admin.dto.response.VocabularyDetailResponse;
import com.vocabularyai.admin.dto.response.VocabularyListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminVocabularyService {
    
    /**
     * Get paginated list of all CEFR words
     */
    Page<VocabularyListResponse> getVocabularyList(Pageable pageable);
    
    /**
     * Get CEFR words filtered by level
     */
    Page<VocabularyListResponse> getVocabularyByLevel(String level, Pageable pageable);
    
    /**
     * Search words by lemma or definition
     */
    Page<VocabularyListResponse> searchVocabulary(String searchTerm, Pageable pageable);
    
    /**
     * Get words by part of speech with optional CEFR level filter
     */
    Page<VocabularyListResponse> getVocabularyByPartOfSpeech(String pos, String level, Pageable pageable);
    
    /**
     * Get vocabulary word detail with usage statistics
     */
    VocabularyDetailResponse getVocabularyDetail(String wordId);
    
    /**
     * Get word count statistics by CEFR level
     */
    CefrStatsResponse getCefrStatistics();
    
    /**
     * Get word categories
     */
    Page<CategoryListResponse> getCategories(Pageable pageable);
    
    /**
     * Create new vocabulary entry
     */
    VocabularyDetailResponse createVocabulary(CreateVocabularyRequest request);
    
    /**
     * Update existing vocabulary entry
     */
    VocabularyDetailResponse updateVocabulary(String wordId, UpdateVocabularyRequest request);
    
    /**
     * Delete vocabulary entry (soft delete)
     */
    void deleteVocabulary(String wordId);
}
