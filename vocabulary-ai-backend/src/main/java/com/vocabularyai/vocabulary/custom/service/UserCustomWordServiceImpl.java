package com.vocabularyai.vocabulary.custom.service;

import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.custom.dto.CreateCustomWordRequest;
import com.vocabularyai.vocabulary.custom.dto.CustomWordResponse;
import com.vocabularyai.vocabulary.custom.dto.UpdateCustomWordRequest;
import com.vocabularyai.vocabulary.custom.entity.UserCustomWord;
import com.vocabularyai.vocabulary.custom.repository.UserCustomWordRepository;
import com.vocabularyai.vocabulary.entity.WordTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCustomWordServiceImpl implements UserCustomWordService {

    private final UserCustomWordRepository userCustomWordRepository;

    @Override
    @Transactional
    public CustomWordResponse createWord(User user, CreateCustomWordRequest request) {
        String trimmedLemma = request.getLemma().trim();
        log.info("User {} creating custom word: lemma={}", user.getId(), trimmedLemma);

        // Check if user already added this lemma
        if (userCustomWordRepository.existsByUserIdAndLemmaIgnoreCase(user.getId(), trimmedLemma)) {
            throw new DuplicateResourceException("Từ vựng '" + trimmedLemma + "' đã có trong danh sách từ riêng của bạn.");
        }

        String validTopic = normalizeTopic(request.getTopic());
        String validLevel = normalizeLevel(request.getCefrLevel());

        UserCustomWord word = UserCustomWord.builder()
                .userId(user.getId())
                .lemma(trimmedLemma)
                .vietnameseMeaning(request.getVietnameseMeaning().trim())
                .partOfSpeech(request.getPartOfSpeech() != null ? request.getPartOfSpeech().trim() : null)
                .example(request.getExample() != null ? request.getExample().trim() : null)
                .exampleTranslation(request.getExampleTranslation() != null ? request.getExampleTranslation().trim() : null)
                .cefrLevel(validLevel)
                .topic(validTopic)
                .build();

        UserCustomWord saved = userCustomWordRepository.save(word);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomWordResponse> listWords(User user, String level, String topic, String search, Pageable pageable) {
        String cleanLevel = (level != null && !level.isBlank() && !"ALL".equalsIgnoreCase(level) && !"Tất cả".equalsIgnoreCase(level))
                ? level.trim().toUpperCase() : null;
        String cleanTopic = (topic != null && !topic.isBlank() && !"ALL".equalsIgnoreCase(topic))
                ? topic.trim().toUpperCase() : null;
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        return userCustomWordRepository.searchUserWords(user.getId(), cleanLevel, cleanTopic, cleanSearch, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomWordResponse getWord(User user, Long id) {
        UserCustomWord word = userCustomWordRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Từ vựng riêng", "id", id));
        return mapToResponse(word);
    }

    @Override
    @Transactional
    public CustomWordResponse updateWord(User user, Long id, UpdateCustomWordRequest request) {
        log.info("User {} updating custom word id={}", user.getId(), id);

        UserCustomWord word = userCustomWordRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Từ vựng riêng", "id", id));

        word.setVietnameseMeaning(request.getVietnameseMeaning().trim());
        word.setPartOfSpeech(request.getPartOfSpeech() != null ? request.getPartOfSpeech().trim() : null);
        word.setExample(request.getExample() != null ? request.getExample().trim() : null);
        word.setExampleTranslation(request.getExampleTranslation() != null ? request.getExampleTranslation().trim() : null);
        word.setCefrLevel(normalizeLevel(request.getCefrLevel()));
        word.setTopic(normalizeTopic(request.getTopic()));

        UserCustomWord saved = userCustomWordRepository.save(word);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteWord(User user, Long id) {
        log.info("User {} deleting custom word id={}", user.getId(), id);

        UserCustomWord word = userCustomWordRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Từ vựng riêng", "id", id));

        userCustomWordRepository.delete(word);
    }

    private String normalizeTopic(String topic) {
        if (topic == null || topic.isBlank()) return "WORK";
        WordTopic wt = WordTopic.fromString(topic);
        return wt != null ? wt.name() : "WORK";
    }

    private String normalizeLevel(String level) {
        if (level == null || level.isBlank()) return "B1";
        String upper = level.trim().toUpperCase();
        if (upper.equals("A1") || upper.equals("A2") || upper.equals("B1") || upper.equals("B2") || upper.equals("C1")) {
            return upper;
        }
        return "B1";
    }

    private CustomWordResponse mapToResponse(UserCustomWord word) {
        return CustomWordResponse.builder()
                .id(word.getId())
                .userId(word.getUserId())
                .lemma(word.getLemma())
                .vietnameseMeaning(word.getVietnameseMeaning())
                .partOfSpeech(word.getPartOfSpeech())
                .example(word.getExample())
                .exampleTranslation(word.getExampleTranslation())
                .cefrLevel(word.getCefrLevel())
                .topic(word.getTopic())
                .createdAt(word.getCreatedAt())
                .updatedAt(word.getUpdatedAt())
                .build();
    }
}
