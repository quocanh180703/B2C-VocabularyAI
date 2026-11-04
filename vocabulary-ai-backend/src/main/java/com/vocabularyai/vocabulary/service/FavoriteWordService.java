package com.vocabularyai.vocabulary.service;

import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.vocabulary.dto.FavoriteWordResponse;
import com.vocabularyai.vocabulary.entity.FavoriteWord;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Favorite Word Service
 * 
 * Business logic for favorite words management
 */
@Slf4j
@Service
public class FavoriteWordService {
    
    private final FavoriteWordRepository favoriteWordRepository;
    private final WordRepository wordRepository;
    private final CefrWordRepository cefrWordRepository;

    public FavoriteWordService(
            FavoriteWordRepository favoriteWordRepository,
            WordRepository wordRepository,
            CefrWordRepository cefrWordRepository) {
        this.favoriteWordRepository = favoriteWordRepository;
        this.wordRepository = wordRepository;
        this.cefrWordRepository = cefrWordRepository;
    }
    
    @Transactional
    public FavoriteWordResponse addToFavorites(User user, Long wordId) {
        return addToFavorites(user, String.valueOf(wordId));
    }

    /**
     * Add word to favorites (supports numeric ID, CEFR UUID or lemma)
     */
    @Transactional
    public FavoriteWordResponse addToFavorites(User user, String wordIdentifier) {
        log.info("Adding word {} to favorites for user {}", wordIdentifier, user.getId());
        Word word = null;

        try {
            Long numericId = Long.parseLong(wordIdentifier);
            word = wordRepository.findById(numericId).orElse(null);
        } catch (NumberFormatException ignored) {}

        if (word == null) {
            var cefrOpt = cefrWordRepository.findById(wordIdentifier);
            if (cefrOpt.isEmpty()) {
                cefrOpt = cefrWordRepository.findByLemma(wordIdentifier);
            }
            if (cefrOpt.isPresent()) {
                var cefr = cefrOpt.get();
                word = wordRepository.findByWord(cefr.getLemma()).orElse(null);
                String viMeaning = (cefr.getVietnameseMeaning() != null && !cefr.getVietnameseMeaning().isBlank())
                    ? cefr.getVietnameseMeaning().trim()
                    : (cefr.getDefinition() != null ? cefr.getDefinition() : cefr.getLemma());

                if (word == null) {
                    word = wordRepository.save(Word.builder()
                        .word(cefr.getLemma())
                        .meaning(viMeaning)
                        .definition(cefr.getDefinition())
                        .pronunciation(cefr.getPronunciation())
                        .level(cefr.getCefrLevel() != null ? cefr.getCefrLevel().toString() : "A1")
                        .partOfSpeech(cefr.getPartOfSpeech() != null ? cefr.getPartOfSpeech().toString() : "")
                        .build());
                } else if (cefr.getVietnameseMeaning() != null && !cefr.getVietnameseMeaning().isBlank()) {
                    if (word.getMeaning() == null || word.getMeaning().isBlank() || word.getMeaning().equals(word.getDefinition())) {
                        word.setMeaning(viMeaning);
                        word = wordRepository.save(word);
                    }
                }
            }
        } else if (word.getMeaning() == null || word.getMeaning().isBlank() || word.getMeaning().equals(word.getDefinition())) {
            var cefrOpt = cefrWordRepository.findByLemma(word.getWord());
            if (cefrOpt.isPresent() && cefrOpt.get().getVietnameseMeaning() != null && !cefrOpt.get().getVietnameseMeaning().isBlank()) {
                word.setMeaning(cefrOpt.get().getVietnameseMeaning().trim());
                word = wordRepository.save(word);
            }
        }

        if (word == null) {
            throw ResourceNotFoundException.of("Word", "id", wordIdentifier);
        }

        if (favoriteWordRepository.existsByUserIdAndWordId(user.getId(), word.getId())) {
            var existing = favoriteWordRepository.findByUserIdAndWordId(user.getId(), word.getId()).get();
            return FavoriteWordResponse.fromEntity(existing);
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
     * Remove word from favorites (supports numeric word ID or CEFR UUID/lemma)
     */
    @Transactional
    public void removeFromFavorites(Long userId, String wordIdentifier) {
        log.info("Removing word {} from favorites for user {}", wordIdentifier, userId);
        Long wordId = null;

        try {
            wordId = Long.parseLong(wordIdentifier);
        } catch (NumberFormatException ignored) {}

        if (wordId == null) {
            var cefrOpt = cefrWordRepository.findById(wordIdentifier);
            if (cefrOpt.isEmpty()) {
                cefrOpt = cefrWordRepository.findByLemma(wordIdentifier);
            }
            if (cefrOpt.isPresent()) {
                var w = wordRepository.findByWord(cefrOpt.get().getLemma());
                if (w.isPresent()) {
                    wordId = w.get().getId();
                }
            }
        }

        if (wordId != null) {
            var fav = favoriteWordRepository.findByUserIdAndWordId(userId, wordId);
            fav.ifPresent(favoriteWordRepository::delete);
        }
        log.info("Word removed from favorites successfully");
    }
    
    /**
     * Check if word is favorite
     */
    public boolean isFavorite(Long userId, Long wordId) {
        return favoriteWordRepository.existsByUserIdAndWordId(userId, wordId);
    }
    
    /**
     * Get all favorite words for user (ensuring Vietnamese meaning is present)
     */
    @Transactional(readOnly = true)
    public Page<FavoriteWordResponse> getUserFavorites(Long userId, Pageable pageable) {
        log.info("Getting favorite words for user {}", userId);
        
        Page<FavoriteWord> favoriteWords = favoriteWordRepository.findByUserId(userId, pageable);
        List<FavoriteWordResponse> list = favoriteWords.getContent().stream()
            .map(fav -> {
                FavoriteWordResponse resp = FavoriteWordResponse.fromEntity(fav);
                if (resp != null && resp.getWord() != null) {
                    Word w = fav.getWord();
                    String meaning = (w != null) ? w.getMeaning() : null;
                    if (meaning == null || meaning.isBlank() || (w != null && meaning.equals(w.getDefinition()))) {
                        if (w != null && w.getWord() != null) {
                            cefrWordRepository.findByLemma(w.getWord()).ifPresent(cefr -> {
                                if (cefr.getVietnameseMeaning() != null && !cefr.getVietnameseMeaning().isBlank()) {
                                    resp.getWord().setMeaning(cefr.getVietnameseMeaning().trim());
                                    resp.getWord().setVietnameseMeaning(cefr.getVietnameseMeaning().trim());
                                }
                            });
                        }
                    } else if (resp.getWord().getVietnameseMeaning() == null) {
                        resp.getWord().setVietnameseMeaning(meaning);
                    }
                }
                return resp;
            })
            .collect(Collectors.toList());

        return new PageImpl<>(
            list,
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
