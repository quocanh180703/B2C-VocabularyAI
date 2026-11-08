package com.vocabularyai.quiz.controller;

import com.vocabularyai.quiz.dto.QuizWithQuestionsResponse;
import com.vocabularyai.quiz.service.QuizService;
import com.vocabularyai.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@DisplayName("QuizController Tests")
public class QuizControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private QuizService quizService;

    @MockBean
    private UserService userService;
    
    @Test
    @DisplayName("Should get published quizzes")
    public void testGetPublishedQuizzes_Success() throws Exception {
        when(quizService.getPublishedQuizzes(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/quizzes"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should get quiz for taking")
    public void testGetQuizForTaking_Success() throws Exception {
        QuizWithQuestionsResponse response = new QuizWithQuestionsResponse();
        when(quizService.getQuizForTaking(anyLong(), any()))
            .thenReturn(response);

        mockMvc.perform(get("/quizzes/1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get user quiz history")
    public void testGetUserQuizHistory_Success() throws Exception {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(quizService.getUserQuizHistory(anyLong(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/quizzes/history"))
            .andExpect(status().isOk());
    }
}
