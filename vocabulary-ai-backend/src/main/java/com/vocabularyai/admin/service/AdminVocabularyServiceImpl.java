package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateVocabularyRequest;
import com.vocabularyai.admin.dto.request.UpdateVocabularyRequest;
import com.vocabularyai.admin.dto.response.CefrStatsResponse;
import com.vocabularyai.admin.dto.response.CategoryListResponse;
import com.vocabularyai.admin.dto.response.VocabularyDetailResponse;
import com.vocabularyai.admin.dto.response.VocabularyListResponse;
import com.vocabularyai.admin.repository.AdminVocabularyRepository;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminVocabularyServiceImpl implements AdminVocabularyService {
    
    private final AdminVocabularyRepository repository;
    private final WordRepository wordRepository;
    
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
        
        Page<?> result = repository.searchVocabulary(searchTerm, pageable);
        
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
        
        return VocabularyDetailResponse.builder()
            .id((String) map.get("id"))
            .word((String) map.get("word"))
            .pronunciation((String) map.get("pronunciation"))
            .partOfSpeech(String.valueOf(map.get("partOfSpeech")))
            .cefr(String.valueOf(map.get("cefr")))
            .definition((String) map.get("definition"))
            .source((String) map.get("source"))
            .language((String) map.get("language"))
            .usageCount(((Number) map.get("usageCount")).intValue())
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
        
        // Find word by ID
        Long wordIdLong = Long.parseLong(wordId);
        Word word = wordRepository.findById(wordIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found: " + wordId));
        
        // Update fields if provided
        if (request.getWord() != null && !request.getWord().isBlank()) {
            word.setWord(request.getWord().trim());
        }
        if (request.getMeaning() != null && !request.getMeaning().isBlank()) {
            word.setMeaning(request.getMeaning().trim());
        }
        if (request.getPartOfSpeech() != null && !request.getPartOfSpeech().isBlank()) {
            word.setPartOfSpeech(request.getPartOfSpeech());
        }
        if (request.getLevel() != null && !request.getLevel().isBlank()) {
            word.setLevel(request.getLevel());
        }
        if (request.getPronunciation() != null) {
            word.setPronunciation(request.getPronunciation());
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
        
        // Save updated word
        Word updated = wordRepository.save(word);
        
        log.debug("Vocabulary updated successfully: id={}, word={}", updated.getId(), updated.getWord());
        
        // Convert to response DTO
        return VocabularyDetailResponse.builder()
            .id(String.valueOf(updated.getId()))
            .word(updated.getWord())
            .pronunciation(updated.getPronunciation())
            .partOfSpeech(updated.getPartOfSpeech())
            .cefr(updated.getLevel())
            .definition(updated.getDefinition())
            .createdAt(updated.getCreatedAt().toString())
            .updatedAt(updated.getUpdatedAt().toString())
            .build();
    }
    
    @Override
    @Transactional
    public void deleteVocabulary(String wordId) {
        log.info("DELETE /api/admin/vocabulary/{} - Deleting vocabulary", wordId);
        
        // Find word by ID
        Long wordIdLong = Long.parseLong(wordId);
        Word word = wordRepository.findById(wordIdLong)
            .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found: " + wordId));
        
        // Soft delete: delete from database (can implement soft delete logic if needed)
        wordRepository.delete(word);
        
        log.debug("Vocabulary deleted successfully: id={}", wordId);
    }
    
    // Helper method to map map result to DTO
    private VocabularyListResponse mapToVocabularyListResponse(Object obj) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) obj;
        
        return VocabularyListResponse.builder()
            .id((String) map.get("id"))
            .word((String) map.get("word"))
            .pronunciation((String) map.get("pronunciation"))
            .partOfSpeech(String.valueOf(map.get("partOfSpeech")))
            .cefr(String.valueOf(map.get("cefr")))
            .meaning((String) map.get("meaning"))
            .source((String) map.get("source"))
            .createdAt(String.valueOf(map.get("createdAt")))
            .build();
    }
}
