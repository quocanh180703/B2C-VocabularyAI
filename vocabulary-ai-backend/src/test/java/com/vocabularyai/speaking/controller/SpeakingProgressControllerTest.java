package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.service.SpeakingProgressService;
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
@DisplayName("SpeakingProgressController Tests")
public class SpeakingProgressControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpeakingProgressService progressService;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get speaking progress")
    public void testGetProgress_Success() throws Exception {
        mockMvc.perform(get("/api/v1/speaking/progress"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get topic progress")
    public void testGetTopicProgress_Success() throws Exception {
        mockMvc.perform(get("/api/v1/speaking/progress/topic/1"))
            .andExpect(status().isOk());
    }
}
