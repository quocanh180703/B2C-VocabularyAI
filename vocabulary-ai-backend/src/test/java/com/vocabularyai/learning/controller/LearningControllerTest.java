package com.vocabularyai.learning.controller;

import com.vocabularyai.learning.service.LearningService;
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
@DisplayName("LearningController Tests")
public class LearningControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private LearningService learningService;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should start learning session")
    public void testStartSession_Success() throws Exception {
        mockMvc.perform(post("/api/v1/learning/sessions")
            .param("categoryId", "1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get learning progress")
    public void testGetProgress_Success() throws Exception {
        mockMvc.perform(get("/api/v1/learning/progress/1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should record learning attempt")
    public void testRecordAttempt_Success() throws Exception {
        String requestJson = "{\"wordId\":1,\"correct\":true,\"timeSpent\":1000}";
        
        mockMvc.perform(post("/api/v1/learning/progress/1/attempt")
            .contentType("application/json")
            .content(requestJson))
            .andExpect(status().isOk());
    }
}
