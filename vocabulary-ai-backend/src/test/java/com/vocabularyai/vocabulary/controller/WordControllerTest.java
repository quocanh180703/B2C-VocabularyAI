package com.vocabularyai.vocabulary.controller;

import com.vocabularyai.vocabulary.dto.WordResponse;
import com.vocabularyai.vocabulary.dto.WordSearchRequest;
import com.vocabularyai.vocabulary.service.WordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("WordController Tests")
public class WordControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private WordService wordService;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get word by ID")
    public void testGetWord_Success() throws Exception {
        WordResponse response = new WordResponse();
        when(wordService.getWordById(anyLong())).thenReturn(response);

        mockMvc.perform(get("/words/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should search words by keyword")
    public void testSearchWords_Success() throws Exception {
        when(wordService.searchWords(any(WordSearchRequest.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/words/search")
            .param("query", "test")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
