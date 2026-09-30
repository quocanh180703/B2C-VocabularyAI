package com.vocabularyai.ai.controller;

import com.vocabularyai.ai.dto.request.GrammarCheckRequest;
import com.vocabularyai.ai.dto.response.GrammarCheckResponse;
import com.vocabularyai.ai.dto.response.GrammarError;
import com.vocabularyai.ai.dto.response.GrammarSuggestion;
import com.vocabularyai.ai.service.grammar.GrammarCheckingService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for GrammarController
 * Tests grammar checking endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GrammarController Tests")
public class GrammarControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private GrammarCheckingService grammarService;
    
    @MockBean
    private UserService userService;
    
    private User testUser;
    private GrammarCheckResponse grammarCheckResponse;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
        
        GrammarError error = GrammarError.builder()
            .startOffset(3)
            .endOffset(5)
            .errorType("GRAMMAR")
            .suggestion("went")
            .explanation("Should use past tense 'went' instead of 'go'")
            .build();
        
        GrammarSuggestion suggestion = GrammarSuggestion.builder()
            .errorType("GRAMMAR")
            .suggestion("Use correct verb tense")
            .explanation("Past tense is required for time reference 'yesterday'")
            .build();
        
        grammarCheckResponse = GrammarCheckResponse.builder()
            .originalText("He go to school yesterday")
            .correctedText("He went to school yesterday")
            .errors(Arrays.asList(error))
            .suggestions(Arrays.asList(suggestion))
            .build();
    }
    
    // ==================== Single Text Grammar Check Tests ====================
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should check grammar successfully")
    public void testCheckGrammar_Success() throws Exception {
        // Arrange
        GrammarCheckRequest request = GrammarCheckRequest.builder()
            .text("He go to school yesterday")
            .language("English")
            .includeSuggestions(true)
            .build();
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(grammarService.checkGrammar(
            request.getText(),
            request.getLanguage(),
            request.getIncludeSuggestions(),
            testUser.getId()
        )).thenReturn(grammarCheckResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/grammar/check")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.correctedText").value("He went to school yesterday"))
            .andExpect(jsonPath("$.data.errors[0].errorType").value("GRAMMAR"));
        
        verify(grammarService, times(1)).checkGrammar(
            request.getText(),
            request.getLanguage(),
            request.getIncludeSuggestions(),
            testUser.getId()
        );
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should reject empty text")
    public void testCheckGrammar_EmptyText() throws Exception {
        // Arrange
        GrammarCheckRequest request = GrammarCheckRequest.builder()
            .text("")
            .language("English")
            .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/grammar/check")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should reject very long text (> 10000 chars)")
    public void testCheckGrammar_TextTooLong() throws Exception {
        // Arrange
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            longText.append("This is a very long text that exceeds the maximum length. ");
        }
        
        GrammarCheckRequest request = GrammarCheckRequest.builder()
            .text(longText.toString())
            .language("English")
            .build();
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(grammarService.checkGrammar(
            anyString(),
            anyString(),
            anyBoolean(),
            eq(testUser.getId())
        )).thenThrow(new IllegalArgumentException("Text is too long"));
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/grammar/check")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should require authentication to check grammar")
    public void testCheckGrammar_Unauthenticated() throws Exception {
        // Arrange
        GrammarCheckRequest request = GrammarCheckRequest.builder()
            .text("Some text")
            .language("English")
            .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/grammar/check")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
    
    // ==================== Batch Grammar Check Tests ====================
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should check grammar batch successfully")
    public void testCheckGrammarBatch_Success() throws Exception {
        // Arrange
        List<String> texts = Arrays.asList(
            "He go to school",
            "She dont like apples",
            "I am learning english"
        );
        
        List<GrammarCheckResponse> responses = Arrays.asList(
            grammarCheckResponse,
            grammarCheckResponse,
            grammarCheckResponse
        );
        
        String requestJson = "{\"texts\": [" +
            "\"He go to school\", " +
            "\"She dont like apples\", " +
            "\"I am learning english\"" +
            "]}";
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(grammarService.checkGrammarBatch(texts, testUser.getId()))
            .thenReturn(responses);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/grammar/check-batch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].correctedText").value("He went to school yesterday"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should reject batch with empty texts list")
    public void testCheckGrammarBatch_EmptyList() throws Exception {
        // Arrange
        String requestJson = "{\"texts\": []}";
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/grammar/check-batch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should reject batch with more than 10 texts")
    public void testCheckGrammarBatch_TooManyTexts() throws Exception {
        // Arrange
        StringBuilder requestJson = new StringBuilder("{\"texts\": [");
        for (int i = 0; i < 11; i++) {
            if (i > 0) requestJson.append(", ");
            requestJson.append("\"Text ").append(i).append("\"");
        }
        requestJson.append("]}");
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(grammarService.checkGrammarBatch(anyList(), eq(testUser.getId())))
            .thenThrow(new IllegalArgumentException("Cannot check more than 10 texts at once"));
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/grammar/check-batch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson.toString()))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should handle batch with mixed correct and incorrect texts")
    public void testCheckGrammarBatch_MixedTexts() throws Exception {
        // Arrange
        List<String> texts = Arrays.asList(
            "This is a correct sentence.",
            "He go to school"
        );
        
        GrammarCheckResponse correctResponse = GrammarCheckResponse.builder()
            .originalText("This is a correct sentence.")
            .correctedText("This is a correct sentence.")
            .errors(Collections.emptyList())
            .suggestions(Collections.emptyList())
            .build();
        
        List<GrammarCheckResponse> responses = Arrays.asList(
            correctResponse,
            grammarCheckResponse
        );
        
        String requestJson = "{\"texts\": [\"This is a correct sentence.\", \"He go to school\"]}";
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(grammarService.checkGrammarBatch(texts, testUser.getId()))
            .thenReturn(responses);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/ai/grammar/check-batch")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(2));
    }
    
    // ==================== Cache Management Tests ====================
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should invalidate specific grammar cache")
    public void testInvalidateGrammarCache_Success() throws Exception {
        // Arrange
        String textHash = "abc123def456";
        doNothing().when(grammarService).invalidateGrammarCache(textHash);
        
        // Act & Assert
        mockMvc.perform(delete("/api/v1/ai/grammar/cache/" + textHash))
            .andExpect(status().isNoContent());
        
        verify(grammarService, times(1)).invalidateGrammarCache(textHash);
    }
    
    @Test
    @WithMockUser(username = "testuser") // Not admin
    @DisplayName("Should deny cache invalidation for non-admin")
    public void testInvalidateGrammarCache_Forbidden() throws Exception {
        // Arrange
        String textHash = "abc123def456";
        
        // Act & Assert
        mockMvc.perform(delete("/api/v1/ai/grammar/cache/" + textHash))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should invalidate all grammar caches")
    public void testInvalidateAllGrammarCache_Success() throws Exception {
        // Arrange
        doNothing().when(grammarService).invalidateAllGrammarCache();
        
        // Act & Assert
        mockMvc.perform(delete("/api/v1/ai/grammar/cache"))
            .andExpect(status().isNoContent());
        
        verify(grammarService, times(1)).invalidateAllGrammarCache();
    }
    
    @Test
    @WithMockUser(username = "testuser") // Not admin
    @DisplayName("Should deny all cache invalidation for non-admin")
    public void testInvalidateAllGrammarCache_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/ai/grammar/cache"))
            .andExpect(status().isForbidden());
    }
}
