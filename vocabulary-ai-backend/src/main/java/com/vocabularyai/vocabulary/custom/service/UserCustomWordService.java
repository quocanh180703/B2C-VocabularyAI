package com.vocabularyai.vocabulary.custom.service;

import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.custom.dto.CreateCustomWordRequest;
import com.vocabularyai.vocabulary.custom.dto.CustomWordResponse;
import com.vocabularyai.vocabulary.custom.dto.UpdateCustomWordRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserCustomWordService {

    CustomWordResponse createWord(User user, CreateCustomWordRequest request);

    Page<CustomWordResponse> listWords(User user, String level, String topic, String search, Pageable pageable);

    CustomWordResponse getWord(User user, Long id);

    CustomWordResponse updateWord(User user, Long id, UpdateCustomWordRequest request);

    void deleteWord(User user, Long id);
}
