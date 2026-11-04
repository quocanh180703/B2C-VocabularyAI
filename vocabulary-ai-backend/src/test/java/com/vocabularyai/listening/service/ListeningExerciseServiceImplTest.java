package com.vocabularyai.listening.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.listening.dto.ListeningSubmitRequest;
import com.vocabularyai.listening.dto.ListeningSubmitResponse;
import com.vocabularyai.listening.entity.ListeningExercise;
import com.vocabularyai.listening.repository.ListeningExerciseRepository;
import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.speaking.service.tts.XahTextToSpeechService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListeningExerciseServiceImpl Tests")
class ListeningExerciseServiceImplTest {

    @Mock
    private ListeningExerciseRepository repository;

    @Mock
    private GeminiAIClientService geminiAIClientService;

    @Mock
    private XahTextToSpeechService xahTextToSpeechService;

    @Mock
    private FeatureUsageService featureUsageService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ListeningExerciseServiceImpl listeningExerciseService;

    private final Long userId = 100L;
    private final Long exerciseId = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listeningExerciseService, "basePath", "/tmp/vocabulary-ai/audio");
    }

    @Test
    @DisplayName("Submit completed exercise throws DuplicateResourceException (409) and does not save")
    void testSubmitExercise_AlreadyCompleted_ThrowsDuplicateResourceException() {
        ListeningExercise completedExercise = ListeningExercise.builder()
                .id(exerciseId)
                .userId(userId)
                .topic("Introduce Yourself")
                .level("A2")
                .passageText("Hello my name is Alex.")
                .status("COMPLETED")
                .score(85)
                .completedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(repository.findByIdAndUserId(exerciseId, userId)).thenReturn(Optional.of(completedExercise));

        ListeningSubmitRequest request = new ListeningSubmitRequest();
        request.setTypedText("Hello my name is Alex.");

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                listeningExerciseService.submitExercise(exerciseId, userId, request)
        );

        assertEquals("Bài nghe này đã được chấm điểm", ex.getMessage());
        verify(repository, never()).save(any(ListeningExercise.class));
    }

    @Test
    @DisplayName("Submit exercise of another user throws ResourceNotFoundException (404)")
    void testSubmitExercise_BelongsToOtherUser_ThrowsResourceNotFoundException() {
        when(repository.findByIdAndUserId(exerciseId, userId)).thenReturn(Optional.empty());

        ListeningSubmitRequest request = new ListeningSubmitRequest();
        request.setTypedText("Hello world");

        assertThrows(ResourceNotFoundException.class, () ->
                listeningExerciseService.submitExercise(exerciseId, userId, request)
        );

        verify(repository, never()).save(any(ListeningExercise.class));
    }

    @Test
    @DisplayName("Detail of CREATED exercise does NOT leak transcript")
    void testGetExerciseDetail_StatusCreated_TranscriptIsNull() {
        ListeningExercise createdExercise = ListeningExercise.builder()
                .id(exerciseId)
                .userId(userId)
                .topic("Daily Routine")
                .level("A2")
                .passageText("Secret answer text that should not leak before submit.")
                .status("CREATED")
                .build();

        when(repository.findByIdAndUserId(exerciseId, userId)).thenReturn(Optional.of(createdExercise));

        ListeningSubmitResponse response = listeningExerciseService.getExerciseDetail(exerciseId, userId);

        assertNotNull(response);
        assertNull(response.getTranscript(), "Transcript must be hidden for CREATED status");
    }

    @Test
    @DisplayName("Detail of COMPLETED exercise reveals full transcript")
    void testGetExerciseDetail_StatusCompleted_RevealsTranscript() {
        String originalPassage = "I wake up at seven every morning and drink coffee.";
        ListeningExercise completedExercise = ListeningExercise.builder()
                .id(exerciseId)
                .userId(userId)
                .topic("Daily Routine")
                .level("A2")
                .passageText(originalPassage)
                .status("COMPLETED")
                .score(90)
                .wordResults("[]")
                .completedAt(LocalDateTime.now())
                .build();

        when(repository.findByIdAndUserId(exerciseId, userId)).thenReturn(Optional.of(completedExercise));

        ListeningSubmitResponse response = listeningExerciseService.getExerciseDetail(exerciseId, userId);

        assertNotNull(response);
        assertEquals(originalPassage, response.getTranscript(), "Completed exercise must reveal transcript");
    }
}

