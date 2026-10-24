package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateVocabularyRequest;
import com.vocabularyai.admin.dto.request.UpdateVocabularyRequest;
import com.vocabularyai.admin.dto.response.CefrStatsResponse;
import com.vocabularyai.admin.dto.response.CategoryListResponse;
import com.vocabularyai.admin.dto.response.VocabularyDetailResponse;
import com.vocabularyai.admin.dto.response.VocabularyListResponse;
import com.vocabularyai.admin.repository.AdminVocabularyRepository;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminVocabularyServiceImpl implements AdminVocabularyService {
    
    private final AdminVocabularyRepository repository;
    private final WordRepository wordRepository;
    private final CefrWordRepository cefrWordRepository;
    
    @Override
    public Page<VocabularyListResponse> getVocabularyList(Pageable pageable) {
        log.info("GET /api/admin/vocabulary - Fetching vocabulary list (page={}, size={})",
            pageable.getPageNumber(), pageable.getPageSize());
        
        Page<?> result = repository.getVocabularyList(pageable);
        
        List<VocabularyListResponse> content = result.getContent().stream()
            .map(this::mapToVocabularyListResponse)
            .collect(Collectors.toList());
        
        log.debug("Vocabulary list fetched: {} total", result.getTotalElements());
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }
    
    @Override
    public Page<VocabularyListResponse> getVocabularyByLevel(String level, Pageable pageable) {
        log.info("GET /api/admin/vocabulary - Filtering by level={}", level);
        
        Page<?> result = repository.getVocabularyByLevel(level, pageable);
        
        List<VocabularyListResponse> content = result.getContent().stream()
            .map(this::mapToVocabularyListResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }
    
    @Override
    public Page<VocabularyListResponse> searchVocabulary(String searchTerm, Pageable pageable) {
        log.info("GET /api/admin/vocabulary - Searching: {}", searchTerm);
        
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<?> result = repository.searchVocabulary(searchTerm, unsorted);
        
        List<VocabularyListResponse> content = result.getContent().stream()
            .map(this::mapToVocabularyListResponse)
            .collect(Collectors.toList());
        
        log.debug("Search results: {} matches found", result.getTotalElements());
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }
    
    @Override
    public Page<VocabularyListResponse> getVocabularyByPartOfSpeech(String pos, String level, Pageable pageable) {
        log.info("GET /api/admin/vocabulary - Filtering by pos={}, level={}", pos, level);
        
        Page<?> result = repository.getVocabularyByPartOfSpeech(pos, level, pageable);
        
        List<VocabularyListResponse> content = result.getContent().stream()
            .map(this::mapToVocabularyListResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, result.getTotalElements());
    }
    
    @Override
    public VocabularyDetailResponse getVocabularyDetail(String wordId) {
        log.info("GET /api/admin/vocabulary/{} - Fetching detail", wordId);
        
        List<?> result = repository.getVocabularyDetail(wordId);
        
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Word not found: " + wordId);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result.get(0);
        
        String vietnameseMeaning = (String) map.get("vietnameseMeaning");
        String definition = (String) map.get("definition");
        String meaning = (vietnameseMeaning != null && !vietnameseMeaning.trim().isEmpty())
            ? vietnameseMeaning : (String) map.get("meaning");
        if (meaning == null || meaning.trim().isEmpty()) {
            meaning = definition;
        }

        return VocabularyDetailResponse.builder()
            .id((String) map.get("id"))
            .word((String) map.get("word"))
            .pronunciation((String) map.get("pronunciation"))
            .partOfSpeech(String.valueOf(map.get("partOfSpeech")))
            .cefr(String.valueOf(map.get("cefr")))
            .definition(definition)
            .vietnameseMeaning(vietnameseMeaning)
            .meaning(meaning)
            .source((String) map.get("source"))
            .language((String) map.get("language"))
            .usageCount(map.get("usageCount") != null ? ((Number) map.get("usageCount")).intValue() : 0)
            .createdAt(String.valueOf(map.get("createdAt")))
            .updatedAt(String.valueOf(map.get("updatedAt")))
            .build();
    }
    
    @Override
    public CefrStatsResponse getCefrStatistics() {
        log.info("GET /api/admin/vocabulary/cefr-stats - Fetching statistics");
        
        List<?> result = repository.getCefrStatistics();
        
        long a1Count = 0, a2Count = 0, b1Count = 0, b2Count = 0, c1Count = 0;
        
        for (Object obj : result) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            String level = String.valueOf(map.get("level"));
            long count = ((Number) map.get("count")).longValue();
            
            switch (level) {
                case "A1" -> a1Count = count;
                case "A2" -> a2Count = count;
                case "B1" -> b1Count = count;
                case "B2" -> b2Count = count;
                case "C1" -> c1Count = count;
            }
        }
        
        long totalCount = a1Count + a2Count + b1Count + b2Count + c1Count;
        
        return CefrStatsResponse.builder()
            .a1Count(a1Count)
            .a2Count(a2Count)
            .b1Count(b1Count)
            .b2Count(b2Count)
            .c1Count(c1Count)
            .totalCount(totalCount)
            .build();
    }
    
    @Override
    public Page<CategoryListResponse> getCategories(Pageable pageable) {
        log.info("GET /api/admin/vocabulary/categories - Fetching categories");
        // TODO: Implement category listing from database
        return Page.empty(pageable);
    }
    
    @Override
    @Transactional
    public VocabularyDetailResponse createVocabulary(CreateVocabularyRequest request) {
        log.info("POST /api/admin/vocabulary - Creating new vocabulary: {}", request.getWord());
        
        // Validate required fields
        if (request.getWord() == null || request.getWord().isBlank()) {
            throw new IllegalArgumentException("Word is required");
        }
        if (request.getMeaning() == null || request.getMeaning().isBlank()) {
            throw new IllegalArgumentException("Meaning is required");
        }
        
        // Create new Word entity
        Word word = Word.builder()
            .word(request.getWord().trim())
            .meaning(request.getMeaning().trim())
            .partOfSpeech(request.getPartOfSpeech())
            .level(request.getLevel())
            .pronunciation(request.getPronunciation())
            .definition(request.getDefinition())
            .example(request.getExample())
            .audioUrl(request.getAudioUrl())
            .imageUrl(request.getImageUrl())
            .build();
        
        // Save to database
        Word saved = wordRepository.save(word);
        
        log.debug("Vocabulary created successfully: id={}, word={}", saved.getId(), saved.getWord());
        
        // Convert to response DTO
        return VocabularyDetailResponse.builder()
            .id(String.valueOf(saved.getId()))
            .word(saved.getWord())
            .pronunciation(saved.getPronunciation())
            .partOfSpeech(saved.getPartOfSpeech())
            .cefr(saved.getLevel())
            .definition(saved.getDefinition())
            .createdAt(saved.getCreatedAt().toString())
            .updatedAt(saved.getUpdatedAt().toString())
            .build();
    }
    
    @Override
    @Transactional
    public VocabularyDetailResponse updateVocabulary(String wordId, UpdateVocabularyRequest request) {
        log.info("PUT /api/admin/vocabulary/{} - Updating vocabulary", wordId);
        
        // Check if it's a CefrWord first (UUID ID)
        Optional<CefrWord> cefrOpt = cefrWordRepository.findById(wordId);
        if (cefrOpt.isPresent()) {
            CefrWord cefrWord = cefrOpt.get();
            if (request.getWord() != null && !request.getWord().isBlank()) {
                cefrWord.setLemma(request.getWord().trim());
            }
            if (request.getMeaning() != null && !request.getMeaning().isBlank()) {
                cefrWord.setVietnameseMeaning(request.getMeaning().trim());
            }
            if (request.getPronunciation() != null) {
                cefrWord.setPronunciation(request.getPronunciation());
            }
            if (request.getDefinition() != null) {
                cefrWord.setDefinition(request.getDefinition());
            }
            if (request.getLevel() != null && !request.getLevel().isBlank()) {
                try {
                    cefrWord.setCefrLevel(com.vocabularyai.vocabulary.entity.CEFRLevel.valueOf(request.getLevel()));
                } catch (Exception ignored) {}
            }
            if (request.getPartOfSpeech() != null && !request.getPartOfSpeech().isBlank()) {
                try {
                    cefrWord.setPartOfSpeech(com.vocabularyai.vocabulary.entity.PartOfSpeech.valueOf(request.getPartOfSpeech()));
                } catch (Exception ignored) {}
            }
            CefrWord savedCefr = cefrWordRepository.save(cefrWord);
            return VocabularyDetailResponse.builder()
                .id(savedCefr.getId())
                .word(savedCefr.getLemma())
                .pronunciation(savedCefr.getPronunciation())
                .partOfSpeech(String.valueOf(savedCefr.getPartOfSpeech()))
                .cefr(String.valueOf(savedCefr.getCefrLevel()))
                .meaning(savedCefr.getVietnameseMeaning())
                .vietnameseMeaning(savedCefr.getVietnameseMeaning())
                .definition(savedCefr.getDefinition())
                .createdAt(savedCefr.getCreatedAt() != null ? savedCefr.getCreatedAt().toString() : null)
                .updatedAt(savedCefr.getUpdatedAt() != null ? savedCefr.getUpdatedAt().toString() : null)
                .build();
        }
        
        // Otherwise try Word entity (Numeric ID)
        try {
            Long wordIdLong = Long.parseLong(wordId);
            Word word = wordRepository.findById(wordIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found: " + wordId));
            
            if (request.getWord() != null && !request.getWord().isBlank()) word.setWord(request.getWord().trim());
            if (request.getMeaning() != null && !request.getMeaning().isBlank()) word.setMeaning(request.getMeaning().trim());
            if (request.getPartOfSpeech() != null && !request.getPartOfSpeech().isBlank()) word.setPartOfSpeech(request.getPartOfSpeech());
            if (request.getLevel() != null && !request.getLevel().isBlank()) word.setLevel(request.getLevel());
            if (request.getPronunciation() != null) word.setPronunciation(request.getPronunciation());
            if (request.getDefinition() != null) word.setDefinition(request.getDefinition());
            if (request.getExample() != null) word.setExample(request.getExample());
            if (request.getAudioUrl() != null) word.setAudioUrl(request.getAudioUrl());
            if (request.getImageUrl() != null) word.setImageUrl(request.getImageUrl());
            
            Word updated = wordRepository.save(word);
            return VocabularyDetailResponse.builder()
                .id(String.valueOf(updated.getId()))
                .word(updated.getWord())
                .pronunciation(updated.getPronunciation())
                .partOfSpeech(updated.getPartOfSpeech())
                .cefr(updated.getLevel())
                .meaning(updated.getMeaning())
                .vietnameseMeaning(updated.getMeaning())
                .definition(updated.getDefinition())
                .createdAt(updated.getCreatedAt() != null ? updated.getCreatedAt().toString() : null)
                .updatedAt(updated.getUpdatedAt() != null ? updated.getUpdatedAt().toString() : null)
                .build();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Vocabulary not found: " + wordId);
        }
    }
    
    @Override
    @Transactional
    public void deleteVocabulary(String wordId) {
        log.info("DELETE /api/admin/vocabulary/{} - Deleting vocabulary", wordId);
        
        Optional<CefrWord> cefrOpt = cefrWordRepository.findById(wordId);
        if (cefrOpt.isPresent()) {
            cefrWordRepository.delete(cefrOpt.get());
            log.debug("CefrWord deleted successfully: id={}", wordId);
            return;
        }
        
        try {
            Long wordIdLong = Long.parseLong(wordId);
            Word word = wordRepository.findById(wordIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found: " + wordId));
            wordRepository.delete(word);
            log.debug("Word deleted successfully: id={}", wordId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Vocabulary not found: " + wordId);
        }
    }
    
    // Helper method to map map result to DTO
    private VocabularyListResponse mapToVocabularyListResponse(Object obj) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) obj;
        
        String vietnameseMeaning = (String) map.get("vietnameseMeaning");
        String definition = (String) map.get("definition");
        String meaning = (vietnameseMeaning != null && !vietnameseMeaning.trim().isEmpty())
            ? vietnameseMeaning
            : (String) map.get("meaning");
        if (meaning == null || meaning.trim().isEmpty()) {
            meaning = definition;
        }
        
        return VocabularyListResponse.builder()
            .id((String) map.get("id"))
            .word((String) map.get("word"))
            .pronunciation((String) map.get("pronunciation"))
            .partOfSpeech(String.valueOf(map.get("partOfSpeech")))
            .cefr(String.valueOf(map.get("cefr")))
            .meaning(meaning)
            .vietnameseMeaning(vietnameseMeaning)
            .definition(definition)
            .source((String) map.get("source"))
            .createdAt(String.valueOf(map.get("createdAt")))
            .build();
    }
}
