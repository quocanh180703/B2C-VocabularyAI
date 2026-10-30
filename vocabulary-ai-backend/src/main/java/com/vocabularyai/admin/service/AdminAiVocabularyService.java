package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.AiVocabApproveRequest;
import com.vocabularyai.admin.dto.request.AiVocabTagApproveRequest;
import com.vocabularyai.admin.dto.request.AiVocabTagBackfillRequest;
import com.vocabularyai.admin.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Classify topic for a batch of lemmas (up to 20 words) using Gemini AI
     */
    List<AiVocabTagClassifyItemDto> classifyWordTopics(List<String> lemmas);

    /**
     * Approve and save topics for given words, evicting cache and updating queue
     */
    int approveAndSaveTopics(AiVocabTagApproveRequest request);

    /**
     * Start background backfill job for all untagged CEFR words
     */
    AiVocabTagBackfillStatusResponse startTopicBackfill(AiVocabTagBackfillRequest request);

    /**
     * Get current status of a background backfill job
     */
    AiVocabTagBackfillStatusResponse getTopicBackfillStatus(String jobId);

    /**
     * Cancel / stop the currently running backfill job
     */
    AiVocabTagBackfillStatusResponse stopTopicBackfill();

    /**
     * Get paginated list of items in topic tag queue
     */
    Page<TopicTagQueueDto> getTopicTagQueue(String status, Pageable pageable);

    /**
     * Get distinct dataset sources from CEFR words
     */
    List<String> getDistinctWordSources();
}
