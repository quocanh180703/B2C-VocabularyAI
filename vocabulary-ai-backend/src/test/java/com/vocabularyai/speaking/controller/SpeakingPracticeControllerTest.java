package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.service.SpeakingPracticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SpeakingPracticeController Tests")
public class SpeakingPracticeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpeakingPracticeService speakingService;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should start speaking practice")
    public void testStartPractice_Success() throws Exception {
        mockMvc.perform(post("/api/v1/speaking/practice/start")
            .param("topicId", "1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get speaking prompts")
    public void testGetPrompt_Success() throws Exception {
        mockMvc.perform(get("/api/v1/speaking/practice/1/prompt"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should submit recording")
    public void testSubmitRecording_Success() throws Exception {
        mockMvc.perform(post("/api/v1/speaking/practice/1/submit")
            .param("audioPath", "path/to/audio.wav"))
            .andExpect(status().isOk());
    }
}
