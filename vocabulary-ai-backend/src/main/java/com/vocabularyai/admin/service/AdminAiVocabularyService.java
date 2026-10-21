package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.AiVocabApproveRequest;
import com.vocabularyai.admin.dto.response.AiGeneratedWordDto;
import com.vocabularyai.admin.dto.response.AiVocabAuditResponse;
import com.vocabularyai.admin.dto.response.AiMissingWordDto;

import java.util.List;

public interface AdminAiVocabularyService {

    /**
     * Audit quality of vocabulary in 'words' table (missing examples, meanings, pronunciations, definitions)
     */
    AiVocabAuditResponse auditQuality();

    /**
     * Get list of words needing enrichment based on filter ('example', 'meaning', 'pronunciation', 'definition')
     */
    List<AiMissingWordDto> getWordsNeedingEnrichment(String filterType, int limit);

    /**
     * Generate content for a single word using Gemini AI
     */
    AiGeneratedWordDto generateWordContent(String word, String levelHint);

    /**
     * Generate content for a batch of words
     */
    List<AiGeneratedWordDto> batchGenerateWords(List<String> words, String levelHint);

    /**
     * Approve and write validated words into database
     */
    int approveAndSaveWords(AiVocabApproveRequest request);
}
