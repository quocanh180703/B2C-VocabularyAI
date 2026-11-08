package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.tts.TextToSpeechResult;
import com.vocabularyai.speaking.service.tts.XahTextToSpeechService;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TextToSpeechController Tests")
public class TextToSpeechControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private XahTextToSpeechService textToSpeechService;

    @MockBean
    private SpeakingSessionRepository sessionRepository;

    @BeforeEach
    public void setUp() {
        User testUser = User.builder().id(1L).username("testuser").email("test@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser, "password", Collections.emptyList())
        );
    }
    
    @Test
    @DisplayName("Should synthesize text to speech")
    public void testSynthesizeSpeech_Success() throws Exception {
        TextToSpeechResult result = TextToSpeechResult.builder()
            .text("Hello world")
            .audioUrl("https://example.com/audio.mp3")
            .audioFormat("audio/mp3")
            .voice("alloy")
            .languageCode("en-US")
            .success(true)
            .build();

        when(textToSpeechService.synthesize(eq("Hello world"), eq("en-US"))).thenReturn(result);

        String requestJson = "{\"text\":\"Hello world\",\"language\":\"en-US\"}";
        
        mockMvc.perform(post("/api/ai/speaking/tts/synthesize")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should stream audio MP3 directly")
    public void testStreamTts_Success() throws Exception {
        byte[] fakeAudio = "fake mp3 data".getBytes();

        when(textToSpeechService.synthesizeRawAudio(eq("Hello world"), anyString())).thenReturn(fakeAudio);

        mockMvc.perform(get("/api/ai/speaking/tts/stream")
            .param("text", "Hello world"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("audio/mpeg"));
    }
}
