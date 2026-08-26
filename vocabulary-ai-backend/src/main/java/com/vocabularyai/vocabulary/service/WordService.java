package com.vocabularyai.vocabulary.service;

import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.vocabulary.dto.*;
import com.vocabularyai.vocabulary.entity.Category;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.CategoryRepository;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Word Service
 * 
 * Business logic for word management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {
    
    private final WordRepository wordRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * Get all words with pagination
     */
    public Page<WordResponse> getAllWords(Pageable pageable) {
        log.info("Getting all words with pagination");
        Page<Word> words = wordRepository.findAll(pageable);
        return new PageImpl<>(
            words.getContent().stream()
                .map(WordResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            words.getTotalElements()
        );
    }
    
    /**
     * Get word by ID
     */
    public WordResponse getWordById(Long wordId) {
        log.info("Getting word with id: {}", wordId);
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> ResourceNotFoundException.of("Word", "id", wordId));
        return WordResponse.fromEntity(word);
    }
    
    /**
     * Create new word
     */
    @Transactional
    public WordResponse createWord(CreateWordRequest request) {
        log.info("Creating new word: {}", request.getWord());
        
        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> ResourceNotFoundException.of("Category", "id", request.getCategoryId()));
        
        Word word = Word.builder()
            .category(category)
            .word(request.getWord())
            .pronunciation(request.getPronunciation())
            .phonetic(request.getPhonetic())
            .partOfSpeech(request.getPartOfSpeech())
            .meaning(request.getMeaning())
            .definition(request.getDefinition())
            .example(request.getExample())
            .audioUrl(request.getAudioUrl())
            .imageUrl(request.getImageUrl())
            .level(request.getLevel())
            .build();
        
        Word savedWord = wordRepository.save(word);
        log.info("Word created successfully with id: {}", savedWord.getId());
        
        return WordResponse.fromEntity(savedWord);
    }
    
    /**
     * Update word
     */
    @Transactional
    public WordResponse updateWord(Long wordId, UpdateWordRequest request) {
        log.info("Updating word with id: {}", wordId);
        
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> ResourceNotFoundException.of("Word", "id", wordId));
        
        if (request.getPronunciation() != null) {
            word.setPronunciation(request.getPronunciation());
        }
        if (request.getPhonetic() != null) {
            word.setPhonetic(request.getPhonetic());
        }
        if (request.getPartOfSpeech() != null) {
            word.setPartOfSpeech(request.getPartOfSpeech());
        }
        if (request.getMeaning() != null) {
            word.setMeaning(request.getMeaning());
        }
        if (request.getDefinition() != null) {
            word.setDefinition(request.getDefinition());
        }
        if (request.getExample() != null) {
            word.setExample(request.getExample());
        }
        if (request.getAudioUrl() != null) {
            word.setAudioUrl(request.getAudioUrl());
        }
        if (request.getImageUrl() != null) {
            word.setImageUrl(request.getImageUrl());
        }
        if (request.getLevel() != null) {
            word.setLevel(request.getLevel());
        }
        
        Word updatedWord = wordRepository.save(word);
        log.info("Word updated successfully with id: {}", wordId);
        
        return WordResponse.fromEntity(updatedWord);
    }
    
    /**
     * Delete word
     */
    @Transactional
    public void deleteWord(Long wordId) {
        log.info("Deleting word with id: {}", wordId);
        
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> ResourceNotFoundException.of("Word", "id", wordId));
        
        wordRepository.delete(word);
        log.info("Word deleted successfully with id: {}", wordId);
    }
    
    /**
     * Get words by category
     */
    public Page<WordResponse> getWordsByCategory(Long categoryId, Pageable pageable) {
        log.info("Getting words by category id: {}", categoryId);
        
        // Verify category exists
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> ResourceNotFoundException.of("Category", "id", categoryId));
        
        Page<Word> words = wordRepository.findByCategoryId(categoryId, pageable);
        return new PageImpl<>(
            words.getContent().stream()
                .map(WordResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            words.getTotalElements()
        );
    }
    
    /**
     * Search words
     */
    public Page<WordResponse> searchWords(WordSearchRequest request, Pageable pageable) {
        log.info("Searching words with keyword: {}, categoryId: {}, level: {}", 
            request.getKeyword(), request.getCategoryId(), request.getLevel());
        
        Page<Word> words;
        
        if (request.getKeyword() == null || request.getKeyword().isBlank()) {
            if (request.getCategoryId() != null && request.getLevel() != null) {
                words = wordRepository.findByCategoryIdAndLevel(request.getCategoryId(), request.getLevel(), pageable);
            } else if (request.getCategoryId() != null) {
                words = wordRepository.findByCategoryId(request.getCategoryId(), pageable);
            } else if (request.getLevel() != null) {
                // Note: This would require a separate query method
                words = wordRepository.findAll(pageable);
            } else {
                words = wordRepository.findAll(pageable);
            }
        } else {
            if (request.getCategoryId() != null && request.getLevel() != null) {
                words = wordRepository.searchByKeywordCategoryAndLevel(request.getKeyword(), request.getCategoryId(), request.getLevel(), pageable);
            } else if (request.getCategoryId() != null) {
                words = wordRepository.searchByKeywordAndCategory(request.getKeyword(), request.getCategoryId(), pageable);
            } else if (request.getLevel() != null) {
                words = wordRepository.searchByKeywordAndLevel(request.getKeyword(), request.getLevel(), pageable);
            } else {
                words = wordRepository.searchByKeyword(request.getKeyword(), pageable);
            }
        }
        
        return new PageImpl<>(
            words.getContent().stream()
                .map(WordResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            words.getTotalElements()
        );
    }
}
