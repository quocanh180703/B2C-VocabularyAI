package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.vocabulary.service.WordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("WordController Tests")
public class WordControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private WordService wordService;
    
    @BeforeEach
    public void setUp() {
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get word by ID")
    public void testGetWord_Success() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary/words/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should search words by keyword")
    public void testSearchWords_Success() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary/words/search")
            .param("keyword", "test")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should require authentication to access words")
    public void testGetWord_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/vocabulary/words/1"))
            .andExpect(status().isUnauthorized());
    }
}
