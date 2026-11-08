package com.vocabularyai.speaking.controller;

import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.speaking.dto.response.SpeakingPracticeResponse;
import com.vocabularyai.speaking.service.SpeakingPracticeService;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SpeakingPracticeController Tests")
public class SpeakingPracticeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpeakingPracticeService speakingPracticeService;

    @MockBean
    private FeatureUsageService featureUsageService;

    @BeforeEach
    public void setUp() {
        User testUser = User.builder().id(1L).username("testuser").email("test@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser, "password", Collections.emptyList())
        );
    }
    
    @Test
    @DisplayName("Should get practice session info")
    public void testGetPracticeSession_Success() throws Exception {
        SpeakingPracticeResponse.StatisticsInfo stats = SpeakingPracticeResponse.StatisticsInfo.builder()
            .totalAttempts(1)
            .build();
        SpeakingPracticeResponse response = SpeakingPracticeResponse.builder()
            .statistics(stats)
            .build();

        when(speakingPracticeService.getPracticeSession(eq(1L), anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/ai/speaking/attempts/1"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should submit speaking attempt successfully")
    public void testSubmitSpeakingAttempt_Success() throws Exception {
        MockMultipartFile audioFile = new MockMultipartFile(
            "audioFile", "test.mp3", "audio/mpeg", "test audio content".getBytes()
        );
        SpeakingPracticeResponse.AttemptInfo attemptInfo = SpeakingPracticeResponse.AttemptInfo.builder()
            .id(10L)
            .build();
        SpeakingPracticeResponse.ScoresInfo scoresInfo = SpeakingPracticeResponse.ScoresInfo.builder()
            .overallScore(80.0)
            .build();
        SpeakingPracticeResponse response = SpeakingPracticeResponse.builder()
            .attempt(attemptInfo)
            .scores(scoresInfo)
            .build();

        when(speakingPracticeService.submitSpeakingPractice(eq(1L), anyLong(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/ai/speaking/attempts")
            .file(audioFile)
            .param("sessionId", "1"))
            .andExpect(status().isCreated());
    }
}
