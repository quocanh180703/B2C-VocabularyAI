package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.dto.response.SpeakingFeedbackResponse;
import com.vocabularyai.speaking.service.SpeakingFeedbackService;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SpeakingFeedbackController Tests")
public class SpeakingFeedbackControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpeakingFeedbackService feedbackService;

    @BeforeEach
    public void setUp() {
        User testUser = User.builder().id(1L).username("testuser").email("test@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser, "password", Collections.emptyList())
        );
    }
    
    @Test
    @DisplayName("Should get speaking feedback")
    public void testGetFeedback_Success() throws Exception {
        SpeakingFeedbackResponse response = SpeakingFeedbackResponse.builder()
            .overallScore(80.0)
            .build();
        when(feedbackService.getAttemptFeedback(anyLong(), anyLong()))
            .thenReturn(response);

        mockMvc.perform(get("/api/ai/speaking/feedback/attempts/1"))
            .andExpect(status().isOk());
    }
}
