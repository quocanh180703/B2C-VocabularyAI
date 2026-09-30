package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.service.SpeakingSessionService;
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
@DisplayName("SpeakingSessionController Tests")
public class SpeakingSessionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpeakingSessionService sessionService;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should create speaking session")
    public void testCreateSession_Success() throws Exception {
        mockMvc.perform(post("/api/v1/speaking/sessions"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get session details")
    public void testGetSession_Success() throws Exception {
        mockMvc.perform(get("/api/v1/speaking/sessions/1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should end session")
    public void testEndSession_Success() throws Exception {
        mockMvc.perform(post("/api/v1/speaking/sessions/1/end"))
            .andExpect(status().isOk());
    }
}
