package com.vocabularyai.vocabulary.service;

import com.vocabularyai.vocabulary.dto.WordResponse;
import com.vocabularyai.vocabulary.dto.WordSearchRequest;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * WordService
 * 
 * Business logic for user vocabulary word operations (NOT CEFR dataset)
 * - Get word by ID
 * - Search words (by word, meaning, level)
 * - Filter by level
 * - Pagination support
 * 
 * NOTE: For CEFR dataset words, use CefrWordService instead
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "words")
public class WordService {

    private final WordRepository wordRepository;

    /**
     * Get word by ID (user vocabulary)
     */
    @Cacheable(key = "'word:' + #id", cacheNames = "words")
    public WordResponse getWordById(Long id) {
        log.debug("Getting user word by id: {}", id);
        
        Optional<Word> word = wordRepository.findById(id);
        return word.map(WordResponse::fromEntity).orElse(null);
    }

    /**
     * Search words by word text or meaning
     */
    @Cacheable(
        key = "'words:search:' + #request.query + ':' + #request.page + ':' + #request.size",
        cacheNames = "words"
    )
    public Page<WordResponse> searchWords(WordSearchRequest request) {
        log.debug("Searching user words: query='{}', page={}, size={}", 
                request.getQuery(), request.getPage(), request.getSize());
        
        String query = request.getQuery() != null ? request.getQuery().trim() : "";
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("word").ascending());
        
        if (query.isEmpty()) {
            Page<Word> allWords = wordRepository.findAll(pageable);
            return allWords.map(WordResponse::fromEntityMinimal);
        }
        
        // For now, just return empty (can implement search by word/meaning if needed)
        return Page.empty(pageable);
    }
}
