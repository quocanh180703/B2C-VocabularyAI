package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.service.storage.AudioMetadata;
import com.vocabularyai.speaking.dto.response.SpeakingAttemptResponse;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.mapper.SpeakingAttemptMapper;
import com.vocabularyai.speaking.repository.SpeakingAttemptRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.storage.AudioStorageService;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpeakingAttemptService Tests")
public class SpeakingAttemptServiceTest {

    @Mock
    private SpeakingAttemptRepository attemptRepository;

    @Mock
    private SpeakingSessionRepository sessionRepository;

    @Mock
    private SpeakingAttemptMapper attemptMapper;

    @Mock
    private AudioStorageService audioStorageService;

    @InjectMocks
    private SpeakingAttemptServiceImpl speakingAttemptService;

    private User testUser;
    private SpeakingSession testSession;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(100L).email("student@example.com").build();
        testSession = SpeakingSession.builder().id(1L).user(testUser).build();

        ReflectionTestUtils.setField(speakingAttemptService, "maxFileSize", 52428800L);
        ReflectionTestUtils.setField(speakingAttemptService, "maxDurationSeconds", 600L);
        ReflectionTestUtils.setField(speakingAttemptService, "supportedFormats", "WAV,MP3,M4A,WEBM,OGG,FLAC");
    }

    @Test
    @DisplayName("createAttempt - Uploads audio via AudioStorageService and saves attempt")
    void testCreateAttempt_UploadsAudioViaStorageService() {
        MockMultipartFile audioFile = new MockMultipartFile(
                "audioFile",
                "recording.webm",
                "audio/webm",
                new byte[]{1, 2, 3, 4}
        );

        AudioMetadata mockMetadata = AudioMetadata.builder()
                .audioUrl("/api/v1/speaking/audio/session-1/attempt-1-uuid.webm")
                .storagePath("/tmp/vocabulary-ai/audio/session-1/attempt-1-uuid.webm")
                .storageType("local")
                .fileSize(4L)
                .build();

        when(sessionRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(testSession));
        when(attemptRepository.countBySessionId(1L)).thenReturn(0L);
        when(audioStorageService.uploadAudio(1L, 1, audioFile)).thenReturn(mockMetadata);
        when(attemptRepository.save(any(SpeakingAttempt.class))).thenAnswer(invocation -> {
            SpeakingAttempt a = invocation.getArgument(0);
            a.setId(10L);
            return a;
        });

        SpeakingAttemptResponse mockResponse = SpeakingAttemptResponse.builder()
                .id(10L)
                .attemptNumber(1)
                .audioUrl(mockMetadata.getAudioUrl())
                .build();
        when(attemptMapper.toResponse(any(SpeakingAttempt.class))).thenReturn(mockResponse);

        SpeakingAttemptResponse response = speakingAttemptService.createAttempt(1L, 100L, audioFile);

        assertNotNull(response);
        assertEquals(1, response.getAttemptNumber());
        assertEquals("/api/v1/speaking/audio/session-1/attempt-1-uuid.webm", response.getAudioUrl());

        // Verify audioStorageService was invoked exactly once with uploadAudio
        verify(audioStorageService, times(1)).uploadAudio(1L, 1, audioFile);
        verify(attemptRepository, times(1)).save(argThat(attempt ->
                attempt.getAttemptNumber() == 1
                        && attempt.getAudioUrl().equals(mockMetadata.getAudioUrl())
        ));
    }
}
