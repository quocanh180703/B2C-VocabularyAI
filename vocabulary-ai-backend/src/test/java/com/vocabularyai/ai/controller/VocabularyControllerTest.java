package com.vocabularyai.ai.controller;

import com.vocabularyai.ai.dto.request.ExplainWordRequest;
import com.vocabularyai.ai.dto.request.ExplainWordsRequest;
import com.vocabularyai.ai.dto.response.ExplanationResponse;
import com.vocabularyai.ai.service.vocabulary.VocabularyExplanationService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("VocabularyController Tests")
public class VocabularyControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private VocabularyExplanationService vocabularyService;
    
    @MockBean
    private UserService userService;
    
    private User testUser;
    private ExplanationResponse explanationResponse;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder().id(1L).username("testuser").build();
        
        explanationResponse = ExplanationResponse.builder()
            .wordId(1L)
            .word("example")
            .explanation("Detailed explanation of the word")
            .examples(Arrays.asList("This is an example.", "Another example."))
            .etymology("From Latin")
            .relatedWords(Arrays.asList("sample", "instance"))
            .partOfSpeech("noun")
            .build();
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should explain word successfully")
    public void testExplainWord_Success() throws Exception {
        ExplainWordRequest request = ExplainWordRequest.builder()
            .wordId(1L).context("business").build();
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(vocabularyService.explainWord(1L, "business", testUser.getId()))
            .thenReturn(explanationResponse);
        
        mockMvc.perform(post("/api/v1/ai/vocabulary/explain")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.word").value("example"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should explain words batch successfully")
    public void testExplainWordsBatch_Success() throws Exception {
        ExplainWordsRequest request = ExplainWordsRequest.builder()
            .wordIds(Arrays.asList(1L, 2L)).build();
        
        List<ExplanationResponse> responses = Arrays.asList(explanationResponse, explanationResponse);
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(vocabularyService.explainWords(Arrays.asList(1L, 2L), testUser.getId()))
            .thenReturn(responses);
        
        mockMvc.perform(post("/api/v1/ai/vocabulary/explain-batch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(2));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should invalidate word cache")
    public void testInvalidateWordCache_Success() throws Exception {
        doNothing().when(vocabularyService).invalidateExplanationCache(1L);
        
        mockMvc.perform(delete("/api/v1/ai/vocabulary/cache/1"))
            .andExpect(status().isNoContent());
    }
}
