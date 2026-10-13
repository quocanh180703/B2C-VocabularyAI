package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.service.SpeakingFeedbackService;
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
@DisplayName("SpeakingFeedbackController Tests")
public class SpeakingFeedbackControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpeakingFeedbackService feedbackService;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get speaking feedback")
    public void testGetFeedback_Success() throws Exception {
        mockMvc.perform(get("/api/v1/speaking/feedback/1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should save feedback")
    public void testSaveFeedback_Success() throws Exception {
        String requestJson = "{\"attemptId\":1,\"pronunciation\":80,\"fluency\":75}";
        
        mockMvc.perform(post("/api/v1/speaking/feedback")
            .contentType("application/json")
            .content(requestJson))
            .andExpect(status().isOk());
    }
}
