package com.vocabularyai.vocabulary.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.vocabulary.dto.FavoriteWordResponse;
import com.vocabularyai.vocabulary.entity.FavoriteWord;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.FavoriteWordRepository;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vocabularyai.user.entity.User;

import java.util.stream.Collectors;

/**
 * Favorite Word Service
 * 
 * Business logic for favorite words management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteWordService {
    
    private final FavoriteWordRepository favoriteWordRepository;
    private final WordRepository wordRepository;
    
    /**
     * Add word to favorites
     */
    @Transactional
    public FavoriteWordResponse addToFavorites(User user, Long wordId) {
        log.info("Adding word {} to favorites for user {}", wordId, user.getId());
        
        // Check if word exists
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> ResourceNotFoundException.of("Word", "id", wordId));
        
        // Check if already favorited
        if (favoriteWordRepository.existsByUserIdAndWordId(user.getId(), wordId)) {
            log.warn("Word {} is already favorited by user {}", wordId, user.getId());
            throw new BadRequestException("Word is already in your favorites");
        }
        
        FavoriteWord favoriteWord = FavoriteWord.builder()
            .user(user)
            .word(word)
            .build();
        
        FavoriteWord savedFavorite = favoriteWordRepository.save(favoriteWord);
        log.info("Word added to favorites successfully");
        
        return FavoriteWordResponse.fromEntity(savedFavorite);
    }
    
    /**
     * Remove word from favorites
     */
    @Transactional
    public void removeFromFavorites(Long userId, Long wordId) {
        log.info("Removing word {} from favorites for user {}", wordId, userId);
        
        FavoriteWord favoriteWord = favoriteWordRepository.findByUserIdAndWordId(userId, wordId)
            .orElseThrow(() -> new BadRequestException("Word is not in your favorites"));
        
        favoriteWordRepository.delete(favoriteWord);
        log.info("Word removed from favorites successfully");
    }
    
    /**
     * Check if word is favorite
     */
    public boolean isFavorite(Long userId, Long wordId) {
        return favoriteWordRepository.existsByUserIdAndWordId(userId, wordId);
    }
    
    /**
     * Get all favorite words for user
     */
    @Transactional(readOnly = true)
    public Page<FavoriteWordResponse> getUserFavorites(Long userId, Pageable pageable) {
        log.info("Getting favorite words for user {}", userId);
        
        Page<FavoriteWord> favoriteWords = favoriteWordRepository.findByUserId(userId, pageable);
        return new PageImpl<>(
            favoriteWords.getContent().stream()
                .map(FavoriteWordResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            favoriteWords.getTotalElements()
        );
    }
    
    /**
     * Get count of favorite words
     */
    public long countUserFavorites(Long userId) {
        return favoriteWordRepository.countByUserId(userId);
    }
}
