package com.vocabularyai.personalization.controller;

import com.vocabularyai.personalization.dto.*;
import com.vocabularyai.personalization.service.*;
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
@DisplayName("PersonalizedLearningController Tests")
public class PersonalizedLearningControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private LearningProfileService profileService;
    
    @MockBean
    private LearningPlanService planService;
    
    @MockBean
    private ProgressAnalysisService progressService;
    
    @MockBean
    private RecommendationService recommendationService;
    
    @MockBean
    private WeaknessStrengthService weaknessService;
    
    @MockBean
    private DailyPracticeService dailyPracticeService;
    
    @MockBean
    private AdaptiveDifficultyService difficultyService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser, "password", Collections.emptyList())
        );
    }
    
    @Test
    @DisplayName("Should get learning profile")
    public void testGetLearningProfile_Success() throws Exception {
        LearningProfileDto profile = LearningProfileDto.builder()
            .userId(1L)
            .overallScore(80.0)
            .build();
        when(profileService.getLearningProfile(anyLong())).thenReturn(profile);

        mockMvc.perform(get("/ai/personalized/profile"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should get personalized learning plan")
    public void testGetLearningPlan_Success() throws Exception {
        LearningPlanDto plan = LearningPlanDto.builder()
            .userId(1L)
            .build();
        when(planService.getCachedPlan(anyLong())).thenReturn(plan);

        mockMvc.perform(get("/ai/personalized/plan"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should get daily practice schedule")
    public void testGetDailyPractice_Success() throws Exception {
        DailyPracticeDto practice = DailyPracticeDto.builder()
            .userId(1L)
            .activities(Collections.emptyList())
            .build();
        when(dailyPracticeService.getDailyPractice(anyLong())).thenReturn(practice);

        mockMvc.perform(get("/ai/personalized/daily-practice"))
            .andExpect(status().isOk());
    }
}
