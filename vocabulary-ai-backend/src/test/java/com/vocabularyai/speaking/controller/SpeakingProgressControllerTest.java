package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.dto.response.SpeakingProgressResponse;
import com.vocabularyai.speaking.service.SpeakingProgressService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SpeakingProgressController Tests")
public class SpeakingProgressControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpeakingProgressService progressService;

    @BeforeEach
    public void setUp() {
        User testUser = User.builder().id(1L).username("testuser").email("test@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser, "password", Collections.emptyList())
        );
    }
    
    @Test
    @DisplayName("Should get speaking progress")
    public void testGetProgress_Success() throws Exception {
        SpeakingProgressResponse mockResponse = SpeakingProgressResponse.builder()
            .statistics(SpeakingProgressResponse.StatisticsData.builder().totalSessions(10L).build())
            .averageScores(SpeakingProgressResponse.ScoresData.builder().overallScore(85.0).build())
            .build();
        when(progressService.getSpeakingProgress(anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/ai/speaking/progress"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should calculate score trend")
    public void testCalculateScoreTrend_Success() throws Exception {
        when(progressService.calculateScoreTrend(anyLong(), eq(7))).thenReturn("Improving");

        mockMvc.perform(get("/api/ai/speaking/progress/trend/7"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should get milestones")
    public void testGetMilestones_Success() throws Exception {
        SpeakingProgressResponse mockResponse = SpeakingProgressResponse.builder()
            .achievements(List.of("First session completed"))
            .build();
        when(progressService.getSpeakingProgress(anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/ai/speaking/progress/milestones"))
            .andExpect(status().isOk());
    }
}
