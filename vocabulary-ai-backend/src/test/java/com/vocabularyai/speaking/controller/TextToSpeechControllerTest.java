package com.vocabularyai.speaking.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TextToSpeechController Tests")
public class TextToSpeechControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should synthesize text to speech")
    public void testSynthesizeSpeech_Success() throws Exception {
        String requestJson = "{\"text\":\"Hello world\",\"language\":\"en\"}";
        
        mockMvc.perform(post("/api/v1/speaking/tts/synthesize")
            .contentType("application/json")
            .content(requestJson))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get audio file")
    public void testGetAudio_Success() throws Exception {
        mockMvc.perform(get("/api/v1/speaking/tts/audio/1"))
            .andExpect(status().isOk());
    }
}
