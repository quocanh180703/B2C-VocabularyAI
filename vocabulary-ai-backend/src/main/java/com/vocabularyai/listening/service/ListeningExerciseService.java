package com.vocabularyai.listening.service;

import com.vocabularyai.listening.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListeningExerciseService {

    CreateListeningResponse createExercise(Long userId, CreateListeningRequest request);

    byte[] getAudioBytes(Long exerciseId, Long userId);

    ListeningSubmitResponse submitExercise(Long exerciseId, Long userId, ListeningSubmitRequest request);

    ListeningSubmitResponse getExerciseDetail(Long exerciseId, Long userId);

    Page<ListeningHistoryItemResponse> getExerciseHistory(Long userId, Pageable pageable);
}

