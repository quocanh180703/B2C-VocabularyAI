package com.vocabularyai.vocabulary.service;

import com.vocabularyai.vocabulary.dto.CefrWordResponse;
import com.vocabularyai.vocabulary.dto.CefrWordSearchRequest;
import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CefrWordService
 * 
 * Business logic for CEFR word operations
 * - Get word by ID
 * - Search words (by lemma, definition, level)
 * - Filter by CEFR level
 * - Pagination support
 * 
 * Caching Strategy:
 * - Individual words: Cache by ID + lemma (TTL: 24 hours)
 * - Level-based queries: Cache by level + page (TTL: 1 hour)
 * - Statistics: Cache globally (TTL: 6 hours)
 * - Search results: Cache by query + level (TTL: 1 hour)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "cefr-words")
public class CefrWordService {

    private final CefrWordRepository cefrWordRepository;

    /**
     * Get word by ID
     * 
     * CACHED: 24 hours TTL
     * Cache key: cefr-word:{id}
     * 
     * @param id CefrWord UUID
     * @return CefrWordResponse or null if not found
     */
    @Cacheable(key = "'cefr-word:' + #id", cacheNames = "cefr-words")
    public CefrWordResponse getWordById(String id) {
        log.debug("Getting CEFR word by id: {}", id);
        
        Optional<CefrWord> word = cefrWordRepository.findById(id);
        return word.map(CefrWordResponse::fromEntity).orElse(null);
    }

    /**
     * Get word by lemma (word text)
     * 
     * CACHED: 24 hours TTL
     * Cache key: cefr-word:lemma:{lemma}
     * 
     * @param lemma Word text
     * @return CefrWordResponse or null if not found
     */
    @Cacheable(key = "'cefr-word:lemma:' + #lemma", cacheNames = "cefr-words")
    public CefrWordResponse getWordByLemma(String lemma) {
        log.debug("Getting CEFR word by lemma: {}", lemma);
        
        Optional<CefrWord> word = cefrWordRepository.findByLemma(lemma);
        return word.map(CefrWordResponse::fromEntity).orElse(null);
    }

    /**
     * Get all words at specific CEFR level (paginated)
     * 
     * CACHED: 1 hour TTL
     * Cache key: cefr-words:level:{level}:page:{page}:size:{size}
     * 
     * @param level CEFR level
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Page of words
     */
    @Cacheable(key = "'cefr-words:level:' + #level + ':page:' + #page + ':size:' + #size", cacheNames = "cefr-words")
    public Page<CefrWordResponse> getWordsByLevel(CEFRLevel level, int page, int size) {
        log.debug("Getting CEFR words by level: {}, page: {}, size: {}", level, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("lemma").ascending());
        Page<CefrWord> words = cefrWordRepository.findByCefrLevel(level, pageable);
        
        return words.map(CefrWordResponse::fromEntityMinimal);
    }

    /**
     * Search words by lemma or definition
     * 
     * CACHED: 1 hour TTL
     * Cache key: cefr-words:search:{query}:{level}:{page}:{size}
     * 
     * @param request Search request with query, level, pagination
     * @return Page of matching words
     */
    @Cacheable(
        key = "'cefr-words:search:' + #request.query + ':' + #request.level + ':' + #request.page + ':' + #request.size",
        cacheNames = "cefr-words"
    )
    public Page<CefrWordResponse> searchWords(CefrWordSearchRequest request) {
        log.debug("Searching CEFR words: query='{}', level={}, page={}, size={}", 
                request.getQuery(), request.getLevel(), request.getPage(), request.getSize());
        
        String query = request.getQuery() != null ? request.getQuery().trim() : "";
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        // Build pageable: If user is searching with a query and didn't specify explicit sortBy,
        // use unsorted PageRequest so the repository's exact match & CEFR level ranking takes precedence
        Pageable pageable;
        if (!query.isEmpty() && request.getSortBy() == null) {
            pageable = PageRequest.of(page, size);
        } else {
            Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortOrder()) 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, request.getSortBy() != null ? request.getSortBy() : "lemma");
            pageable = PageRequest.of(page, size, sort);
        }
        
        // If no query, return all words at level or all words
        if (query.isEmpty()) {
            if (request.getLevel() != null) {
                return getWordsByLevel(request.getLevel(), page, size);
            } else {
                Page<CefrWord> allWords = cefrWordRepository.findAll(pageable);
                return allWords.map(CefrWordResponse::fromEntityMinimal);
            }
        }
        
        // Search by lemma or definition
        Page<CefrWord> results = cefrWordRepository.searchByLemmaOrDefinition(query, pageable);
        
        // Filter by level if provided (note: repository query is paginated, so we must construct new predicate)
        if (request.getLevel() != null) {
            List<CefrWord> filtered = results.stream()
                    .filter(w -> w.getCefrLevel() == request.getLevel())
                    .toList();
            // Convert back to Page with manual PageImpl (not ideal but necessary for filtering after pagination)
            return new org.springframework.data.domain.PageImpl<>(
                    filtered.stream().map(CefrWordResponse::fromEntityMinimal).toList(),
                    pageable,
                    filtered.size()
            );
        }
        
        return results.map(CefrWordResponse::fromEntityMinimal);
    }

    /**
     * Autocomplete: Find words starting with prefix
     * 
     * CACHED: 1 hour TTL
     * Cache key: cefr-words:autocomplete:{prefix}:{limit}
     * 
     * @param prefix Word prefix
     * @param limit Max results
     * @return Page of matching words
     */
    @Cacheable(key = "'cefr-words:autocomplete:' + #prefix + ':' + #limit", cacheNames = "cefr-words")
    public Page<CefrWordResponse> autocomplete(String prefix, int limit) {
        log.debug("Autocomplete CEFR words: prefix='{}', limit={}", prefix, limit);
        
        if (prefix == null || prefix.trim().isEmpty()) {
            return Page.empty();
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        Page<CefrWord> results = cefrWordRepository.searchByLemmaPrefix(prefix.trim(), pageable);
        
        return results.map(CefrWordResponse::fromEntityMinimal);
    }

    /**
     * Get random word at specific level (for daily challenge)
     * 
     * NOT CACHED: Random word should be different each time
     * 
     * @param level CEFR level
     * @return Random word or null if no words at level
     */
    public CefrWordResponse getRandomWord(CEFRLevel level) {
        log.debug("Getting random CEFR word at level: {}", level);
        
        Optional<CefrWord> word = cefrWordRepository.findRandomByLevel(level.toString());
        return word.map(CefrWordResponse::fromEntity).orElse(null);
    }

    /**
     * Get random words batch for flashcards
     * 
     * NOT CACHED: Random words should be different each time
     * 
     * @param level CEFR level (optional, all levels if null)
     * @param limit Maximum number of words (1 to 100)
     * @return List of random CefrWordResponse objects
     */
    public List<CefrWordResponse> getRandomWords(CEFRLevel level, int limit) {
        String levelStr = level != null ? level.toString() : null;
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        log.debug("Getting random CEFR words: level={}, limit={}", levelStr, safeLimit);
        
        List<CefrWord> words = cefrWordRepository.findRandomWordsByLevel(levelStr, safeLimit);
        return words.stream()
                .map(CefrWordResponse::fromEntityMinimal)
                .toList();
    }

    /**
     * Get statistics: count words by level
     * 
     * CACHED: 6 hours TTL (less frequently accessed)
     * Cache key: cefr-words:statistics
     * 
     * @return Object with counts
     */
    @Cacheable(key = "'cefr-words:statistics'", cacheNames = "cefr-words")
    public CefrWordStatistics getStatistics() {
        log.debug("Getting CEFR word statistics");
        
        CefrWordStatistics stats = CefrWordStatistics.builder()
                .a1Count(cefrWordRepository.countByCefrLevel(CEFRLevel.A1))
                .a2Count(cefrWordRepository.countByCefrLevel(CEFRLevel.A2))
                .b1Count(cefrWordRepository.countByCefrLevel(CEFRLevel.B1))
                .b2Count(cefrWordRepository.countByCefrLevel(CEFRLevel.B2))
                .c1Count(cefrWordRepository.countByCefrLevel(CEFRLevel.C1))
                .totalCount(cefrWordRepository.count())
                .build();
        
        return stats;
    }

    /**
     * Inner class for statistics response
     */
    @lombok.Data
    @lombok.Builder
    public static class CefrWordStatistics {
        private long a1Count;
        private long a2Count;
        private long b1Count;
        private long b2Count;
        private long c1Count;
        private long totalCount;
    }
}
