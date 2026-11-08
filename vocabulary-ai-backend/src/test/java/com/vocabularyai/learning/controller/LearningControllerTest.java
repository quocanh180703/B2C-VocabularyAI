package com.vocabularyai.learning.controller;

import com.vocabularyai.learning.dto.LearningProgressResponse;
import com.vocabularyai.learning.service.LearningService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("LearningController Tests")
public class LearningControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private LearningService learningService;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = User.builder().id(1L).username("testuser").email("test@example.com").build();
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should start learning word")
    public void testStartSession_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(learningService.startLearningWord(any(), anyString()))
            .thenReturn(new LearningProgressResponse());

        mockMvc.perform(post("/learning/words/1/start"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get learning progress")
    public void testGetProgress_Success() throws Exception {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(learningService.getUserProgress(anyLong(), any()))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/learning/progress"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should review word attempt")
    public void testRecordAttempt_Success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(learningService.reviewWord(any(), anyLong(), anyBoolean()))
            .thenReturn(new LearningProgressResponse());

        String requestJson = "{\"correct\":true}";
        
        mockMvc.perform(post("/learning/words/1/review")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isOk());
    }
}
