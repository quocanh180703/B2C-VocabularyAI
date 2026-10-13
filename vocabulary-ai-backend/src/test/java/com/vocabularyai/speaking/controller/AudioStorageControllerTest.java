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
@DisplayName("AudioStorageController Tests")
public class AudioStorageControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should upload audio")
    public void testUploadAudio_Success() throws Exception {
        mockMvc.perform(post("/api/v1/speaking/audio/upload")
            .param("attemptId", "1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should download audio")
    public void testDownloadAudio_Success() throws Exception {
        mockMvc.perform(get("/api/v1/speaking/audio/download/1"))
            .andExpect(status().isOk());
    }
}
