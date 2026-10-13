package com.vocabularyai.vocabulary.service;

import com.vocabularyai.vocabulary.dto.CefrWordImportResult;
import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.entity.PartOfSpeech;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * CefrWordImportService
 * 
 * Service to import CEFR vocabulary dataset from CSV files
 * 
 * Features:
 * - Batch import (100 words at a time)
 * - Data validation (required fields, CEFR level, POS)
 * - Error handling & logging
 * - Transaction support (rollback on critical errors)
 * - Duplicate detection (skip if word already exists)
 * 
 * Import flow:
 * 1. Load CSV file from classpath (data_word/en-{LEVEL}.csv)
 * 2. Parse CSV (skip header row)
 * 3. Validate each row
 * 4. Build CefrWord entity
 * 5. Batch insert every 100 words
 * 6. Return statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CefrWordImportService {

    private final CefrWordRepository cefrWordRepository;
    private final ResourceLoader resourceLoader;

    @Value("${app.word-import.batch-size:100}")
    private int batchSize;

    private static final String CSV_PATH_TEMPLATE = "classpath:db/data_word/en-{LEVEL}.csv";
    private static final String[] LEVELS = {"A1", "A2", "B1", "B2", "C1"};
    private static final int CSV_HEADER_LINES = 1;

    /**
     * Import all CEFR levels (A1-C1)
     * 
     * @return Combined import result for all levels
     */
    @Transactional
    public CefrWordImportResult importAllLevels() {
        log.info("Starting import of all CEFR levels (A1-C1)...");

        CefrWordImportResult totalResult = CefrWordImportResult.builder()
                .startTime(LocalDateTime.now())
                .totalWords(0)
                .successCount(0)
                .skipCount(0)
                .errorCount(0)
                .build();

        for (String level : LEVELS) {
            try {
                CefrWordImportResult levelResult = importLevel(level);
                totalResult.setTotalWords(totalResult.getTotalWords() + levelResult.getTotalWords());
                totalResult.setSuccessCount(totalResult.getSuccessCount() + levelResult.getSuccessCount());
                totalResult.setSkipCount(totalResult.getSkipCount() + levelResult.getSkipCount());
                totalResult.setErrorCount(totalResult.getErrorCount() + levelResult.getErrorCount());
            } catch (Exception e) {
                log.error("Failed to import level {}: {}", level, e.getMessage());
                totalResult.addError("Level " + level + ": " + e.getMessage());
            }
        }

        totalResult.setEndTime(LocalDateTime.now());
        log.info("Import completed. Result: {}", totalResult);
        return totalResult;
    }

    /**
     * Import specific CEFR level
     * 
     * @param level CEFR level (A1, A2, B1, B2, C1)
     * @return Import result with statistics
     */
    @Transactional
    public CefrWordImportResult importLevel(String level) {
        log.info("Starting import for CEFR level: {}", level);

        if (!isValidLevel(level)) {
            throw new IllegalArgumentException("Invalid CEFR level: " + level);
        }

        CefrWordImportResult result = CefrWordImportResult.builder()
                .level(level)
                .startTime(LocalDateTime.now())
                .totalWords(0)
                .successCount(0)
                .skipCount(0)
                .errorCount(0)
                .build();

        try {
            // Load CSV file
            String csvPath = CSV_PATH_TEMPLATE.replace("{LEVEL}", level);
            Resource resource = resourceLoader.getResource(csvPath);

            if (!resource.exists()) {
                throw new IOException("CSV file not found: " + csvPath);
            }

            log.debug("Loading CSV file: {}", csvPath);

            // Parse and import
            importFromCsv(resource, level, result);

            result.setEndTime(LocalDateTime.now());
            result.setDurationSeconds((result.getEndTime().getSecond() - result.getStartTime().getSecond()));

            log.info("Import for CEFR level {} completed. Success: {}, Skip: {}, Error: {}",
                    level, result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());

            return result;

        } catch (IOException e) {
            log.error("IO error during import: {}", e.getMessage());
            result.addError("IO error: " + e.getMessage());
            return result;
        }
    }

    /**
     * Parse and import words from CSV file
     * 
     * CSV format:
     * id,lemma,language,cefr_level,pos,ipa,definition_target,source,created_at,updated_at
     * 
     * @param resource CSV file resource
     * @param level CEFR level for validation
     * @param result Result accumulator
     */
    private void importFromCsv(Resource resource, String level, CefrWordImportResult result) throws IOException {
        List<CefrWord> batch = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip header
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                result.setTotalWords(result.getTotalWords() + 1);

                try {
                    // Parse CSV line
                    String[] fields = parseCSVLine(line);

                    // Validate required fields
                    if (fields.length < 7) {
                        log.warn("Line {}: Insufficient fields ({})", lineNumber, fields.length);
                        result.setSkipCount(result.getSkipCount() + 1);
                        continue;
                    }

                    // Extract fields
                    String id = fields[0].trim();
                    String lemma = fields[1].trim();
                    String csvLevel = fields[3].trim();
                    String pos = fields[4].trim();
                    String ipa = fields.length > 5 ? fields[5].trim() : null;
                    String definition = fields.length > 6 ? fields[6].trim() : null;
                    String source = fields.length > 7 ? fields[7].trim() : null;

                    // Validate level matches
                    if (!level.equals(csvLevel)) {
                        log.warn("Line {}: Level mismatch (expected {}, got {})", lineNumber, level, csvLevel);
                        result.setSkipCount(result.getSkipCount() + 1);
                        continue;
                    }

                    // Validate lemma
                    if (lemma.isEmpty()) {
                        log.warn("Line {}: Empty lemma", lineNumber);
                        result.setSkipCount(result.getSkipCount() + 1);
                        continue;
                    }

                    // Check duplicate
                    if (cefrWordRepository.existsByLemmaIgnoreCase(lemma)) {
                        log.debug("Line {}: CEFR word already exists: {}", lineNumber, lemma);
                        result.setSkipCount(result.getSkipCount() + 1);
                        continue;
                    }

                    // Build CefrWord entity
                    CefrWord word = CefrWord.builder()
                            .id(id)
                            .lemma(lemma)
                            .language("en")
                            .cefrLevel(CEFRLevel.fromString(csvLevel))
                            .partOfSpeech(PartOfSpeech.fromString(pos))
                            .pronunciation(ipa)
                            .definition(definition)
                            .source(source)
                            .build();

                    batch.add(word);

                    // Batch insert every N words
                    if (batch.size() >= batchSize) {
                        cefrWordRepository.saveAll(batch);
                        log.debug("Saved batch of {} CEFR words", batch.size());
                        batch.clear();
                    }

                    result.setSuccessCount(result.getSuccessCount() + 1);

                } catch (Exception e) {
                    log.warn("Line {}: Error processing row: {}", lineNumber, e.getMessage());
                    result.setErrorCount(result.getErrorCount() + 1);
                    result.addError("Line " + lineNumber + ": " + e.getMessage());
                }
            }

            // Save remaining batch
            if (!batch.isEmpty()) {
                cefrWordRepository.saveAll(batch);
                log.debug("Saved final batch of {} CEFR words", batch.size());
            }

        }
    }

    /**
     * Parse CSV line (handle quoted fields)
     * 
     * Simple CSV parser that handles:
     * - Regular fields: word,definition
     * - Quoted fields: "word with comma, inside",definition
     * - Empty fields: word,,definition
     * 
     * @param line CSV line
     * @return Array of field values
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Validate CEFR level
     */
    private boolean isValidLevel(String level) {
        return Arrays.asList(LEVELS).contains(level.toUpperCase());
    }

    /**
     * Import words from uploaded CSV file (from form data)
     * 
     * @param rows List of parsed CSV rows (Map of column_name -> value)
     * @param level CEFR level for these words
     * @return Import result with statistics
     */
    @Transactional
    public CefrWordImportResult importFromRows(List<Map<String, String>> rows, String level) {
        log.info("Starting import of {} words from uploaded file for level: {}", rows.size(), level);

        if (!isValidLevel(level)) {
            throw new IllegalArgumentException("Invalid CEFR level: " + level);
        }

        CefrWordImportResult result = CefrWordImportResult.builder()
                .level(level)
                .startTime(LocalDateTime.now())
                .totalWords(rows.size())
                .successCount(0)
                .skipCount(0)
                .errorCount(0)
                .build();

        try {
            List<CefrWord> batch = new ArrayList<>();
            int rowNum = 0;

            for (Map<String, String> row : rows) {
                rowNum++;

                try {
                    // Extract fields from CSV row
                    String id = row.getOrDefault("id", UUID.randomUUID().toString());
                    String lemma = row.getOrDefault("lemma", row.getOrDefault("word", "")).trim();
                    String pos = row.getOrDefault("pos", row.getOrDefault("part_of_speech", "")).trim();
                    String pronunciation = row.getOrDefault("pronunciation", row.getOrDefault("ipa", "")).trim();
                    String definition = row.getOrDefault("definition", row.getOrDefault("meaning", "")).trim();
                    String source = row.getOrDefault("source", "uploaded");

                    // Validate lemma
                    if (lemma.isEmpty()) {
                        log.warn("Row {}: Empty lemma/word", rowNum);
                        result.setSkipCount(result.getSkipCount() + 1);
                        continue;
                    }

                    // Check duplicate
                    if (cefrWordRepository.existsByLemmaIgnoreCase(lemma)) {
                        log.debug("Row {}: CEFR word already exists: {}", rowNum, lemma);
                        result.setSkipCount(result.getSkipCount() + 1);
                        continue;
                    }

                    // Build CefrWord entity
                    CefrWord word = CefrWord.builder()
                            .id(id)
                            .lemma(lemma)
                            .language("en")
                            .cefrLevel(CEFRLevel.fromString(level))
                            .partOfSpeech(pos.isEmpty() ? null : PartOfSpeech.fromString(pos))
                            .pronunciation(pronunciation.isEmpty() ? null : pronunciation)
                            .definition(definition.isEmpty() ? null : definition)
                            .source(source)
                            .build();

                    batch.add(word);

                    // Batch insert every N words
                    if (batch.size() >= batchSize) {
                        cefrWordRepository.saveAll(batch);
                        log.debug("Saved batch of {} CEFR words", batch.size());
                        batch.clear();
                    }

                    result.setSuccessCount(result.getSuccessCount() + 1);

                } catch (Exception e) {
                    log.warn("Row {}: Error processing row: {}", rowNum, e.getMessage());
                    result.setErrorCount(result.getErrorCount() + 1);
                    result.addError("Row " + rowNum + ": " + e.getMessage());
                }
            }

            // Save remaining batch
            if (!batch.isEmpty()) {
                cefrWordRepository.saveAll(batch);
                log.debug("Saved final batch of {} CEFR words", batch.size());
            }

            result.setEndTime(LocalDateTime.now());
            log.info("Upload import completed. Success: {}, Skip: {}, Error: {}",
                    result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());

            return result;

        } catch (Exception e) {
            log.error("Error during upload import: {}", e.getMessage());
            result.addError("Upload import error: " + e.getMessage());
            result.setEndTime(LocalDateTime.now());
            return result;
        }
    }

    /**
     * Get import statistics
     * 
     * @return Current word count by level
     */
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new LinkedHashMap<>();
        
        for (String level : LEVELS) {
            CEFRLevel cefrLevel = CEFRLevel.fromString(level);
            long count = cefrWordRepository.countByCefrLevel(cefrLevel);
            stats.put(level, count);
        }
        
        stats.put("TOTAL", cefrWordRepository.count());
        return stats;
    }
}
