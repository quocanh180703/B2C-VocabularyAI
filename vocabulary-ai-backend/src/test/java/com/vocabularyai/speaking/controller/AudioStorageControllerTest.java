package com.vocabularyai.speaking.controller;

import com.vocabularyai.common.exception.GlobalExceptionHandler;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import com.vocabularyai.speaking.entity.SpeakingSession;
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
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AudioStorageController Tests")
public class AudioStorageControllerTest {

    @Mock
    private AudioStorageService audioStorageService;

    @Mock
    private SpeakingSessionRepository sessionRepository;

    @Mock
    private SpeakingAttemptRepository attemptRepository;

    @InjectMocks
    private AudioStorageController audioStorageController;

    private MockMvc mockMvc;
    private User testUser;
    private SpeakingSession testSession;
    private SpeakingAttempt testAttempt;
    private final byte[] dummyAudioBytes = new byte[]{0x1a, 0x45, (byte) 0xdf, (byte) 0xa3}; // WebM magic bytes

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(100L).email("student@example.com").build();
        testSession = SpeakingSession.builder().id(1L).user(testUser).build();
        testAttempt = SpeakingAttempt.builder()
                .id(10L)
                .session(testSession)
                .attemptNumber(1)
                .audioUrl("/api/v1/speaking/audio/session-1/attempt-1-abc123.webm")
                .build();

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().equals(User.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testUser;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(audioStorageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("Download audio - Owner and file exists -> 200 OK with attachment header")
    void testDownloadAudio_Success() throws Exception {
        when(sessionRepository.existsById(1L)).thenReturn(true);
        when(sessionRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(testSession));
        when(attemptRepository.findBySessionIdAndAttemptNumber(1L, 1)).thenReturn(Optional.of(testAttempt));
        when(audioStorageService.audioExists(testAttempt.getAudioUrl())).thenReturn(true);
        when(audioStorageService.downloadAudio(testAttempt.getAudioUrl())).thenReturn(dummyAudioBytes);

        mockMvc.perform(get("/api/ai/speaking/audio/1/attempt-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/webm"))
                .andExpect(content().bytes(dummyAudioBytes))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attempt-1.webm\""));

        verify(audioStorageService).downloadAudio(testAttempt.getAudioUrl());
    }

    @Test
    @DisplayName("Stream audio - Owner and file exists -> 200 OK with inline header")
    void testStreamAudio_Success() throws Exception {
        when(sessionRepository.existsById(1L)).thenReturn(true);
        when(sessionRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(testSession));
        when(attemptRepository.findBySessionIdAndAttemptNumber(1L, 1)).thenReturn(Optional.of(testAttempt));
        when(audioStorageService.audioExists(testAttempt.getAudioUrl())).thenReturn(true);
        when(audioStorageService.downloadAudio(testAttempt.getAudioUrl())).thenReturn(dummyAudioBytes);

        mockMvc.perform(get("/api/ai/speaking/audio/1/attempt-1/stream"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/webm"))
                .andExpect(content().bytes(dummyAudioBytes))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"attempt-1.webm\""))
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "none"));

        verify(audioStorageService).downloadAudio(testAttempt.getAudioUrl());
    }

    @Test
    @DisplayName("Download audio via fallback route /ai/speaking/audio -> 200 OK")
    void testDownloadAudio_FallbackRoute() throws Exception {
        when(sessionRepository.existsById(1L)).thenReturn(true);
        when(sessionRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(testSession));
        when(attemptRepository.findBySessionIdAndAttemptNumber(1L, 1)).thenReturn(Optional.of(testAttempt));
        when(audioStorageService.audioExists(testAttempt.getAudioUrl())).thenReturn(true);
        when(audioStorageService.downloadAudio(testAttempt.getAudioUrl())).thenReturn(dummyAudioBytes);

        mockMvc.perform(get("/ai/speaking/audio/1/attempt-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/webm"));
    }

    @Test
    @DisplayName("Session not found -> 404 NOT_FOUND")
    void testDownloadAudio_SessionNotFound() throws Exception {
        when(sessionRepository.existsById(999L)).thenReturn(false);

        mockMvc.perform(get("/api/ai/speaking/audio/999/attempt-1"))
                .andExpect(status().isNotFound());

        verify(sessionRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("Session belongs to another user -> 403 FORBIDDEN")
    void testDownloadAudio_ForbiddenForOtherUser() throws Exception {
        when(sessionRepository.existsById(1L)).thenReturn(true);
        when(sessionRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ai/speaking/audio/1/attempt-1"))
                .andExpect(status().isForbidden());

        verify(attemptRepository, never()).findBySessionIdAndAttemptNumber(any(), any());
    }

    @Test
    @DisplayName("Attempt not found in session -> 404 NOT_FOUND")
    void testDownloadAudio_AttemptNotFound() throws Exception {
        when(sessionRepository.existsById(1L)).thenReturn(true);
        when(sessionRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(testSession));
        when(attemptRepository.findBySessionIdAndAttemptNumber(1L, 5)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ai/speaking/audio/1/attempt-5"))
                .andExpect(status().isNotFound());

        verify(audioStorageService, never()).downloadAudio(any());
    }

    @Test
    @DisplayName("Audio file not found on disk / storage -> 404 NOT_FOUND")
    void testDownloadAudio_AudioFileNotFoundOnStorage() throws Exception {
        when(sessionRepository.existsById(1L)).thenReturn(true);
        when(sessionRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(testSession));
        when(attemptRepository.findBySessionIdAndAttemptNumber(1L, 1)).thenReturn(Optional.of(testAttempt));
        when(audioStorageService.audioExists(testAttempt.getAudioUrl())).thenReturn(false);

        mockMvc.perform(get("/api/ai/speaking/audio/1/attempt-1"))
                .andExpect(status().isNotFound());

        verify(audioStorageService, never()).downloadAudio(any());
    }

    @Test
    @DisplayName("Content-Type is resolved correctly for WAV file")
    void testStreamAudio_WavFormat() throws Exception {
        SpeakingAttempt wavAttempt = SpeakingAttempt.builder()
                .session(testSession)
                .attemptNumber(2)
                .audioUrl("/api/v1/speaking/audio/session-1/attempt-2.wav")
                .build();

        when(sessionRepository.existsById(1L)).thenReturn(true);
        when(sessionRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(testSession));
        when(attemptRepository.findBySessionIdAndAttemptNumber(1L, 2)).thenReturn(Optional.of(wavAttempt));
        when(audioStorageService.audioExists(wavAttempt.getAudioUrl())).thenReturn(true);
        when(audioStorageService.downloadAudio(wavAttempt.getAudioUrl())).thenReturn(dummyAudioBytes);

        mockMvc.perform(get("/api/ai/speaking/audio/1/attempt-2/stream"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/wav"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"attempt-2.wav\""));
    }
}
