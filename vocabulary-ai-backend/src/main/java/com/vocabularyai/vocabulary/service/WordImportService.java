package com.vocabularyai.vocabulary.service;

import com.vocabularyai.vocabulary.dto.WordImportResult;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * WordImportService
 * 
 * Service for importing user vocabulary words (NOT for CEFR dataset)
 * 
 * NOTE: For CEFR dataset import, use CefrWordImportService instead
 * 
 * This service handles importing user vocabulary lists from external sources
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WordImportService {

    private final WordRepository wordRepository;
    private final ResourceLoader resourceLoader;

    @Value("${app.word-import.batch-size:100}")
    private int batchSize;

    /**
     * Import user vocabulary words
     * (Implementation TBD based on requirements)
     */
    public WordImportResult importWords(Resource resource) {
        log.info("Starting import of user vocabulary words");

        WordImportResult result = WordImportResult.builder()
                .startTime(LocalDateTime.now())
                .totalWords(0)
                .successCount(0)
                .skipCount(0)
                .errorCount(0)
                .build();

        result.setEndTime(LocalDateTime.now());
        log.info("User vocabulary import completed");
        return result;
    }
}
