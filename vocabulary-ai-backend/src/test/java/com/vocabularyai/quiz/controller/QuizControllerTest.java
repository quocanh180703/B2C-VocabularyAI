package com.vocabularyai.quiz.controller;

import com.vocabularyai.quiz.service.QuizService;
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
@DisplayName("QuizController Tests")
public class QuizControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private QuizService quizService;
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should start quiz")
    public void testStartQuiz_Success() throws Exception {
        mockMvc.perform(post("/api/v1/quiz/start")
            .param("categoryId", "1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get quiz questions")
    public void testGetQuestions_Success() throws Exception {
        mockMvc.perform(get("/api/v1/quiz/1/questions"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should submit quiz answers")
    public void testSubmitQuiz_Success() throws Exception {
        String requestJson = "{\"answers\":[1,2,3]}";
        
        mockMvc.perform(post("/api/v1/quiz/1/submit")
            .contentType("application/json")
            .content(requestJson))
            .andExpect(status().isOk());
    }
}
