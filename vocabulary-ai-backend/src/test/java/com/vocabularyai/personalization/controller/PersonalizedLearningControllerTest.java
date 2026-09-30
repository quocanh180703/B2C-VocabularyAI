package com.vocabularyai.personalization.controller;

import com.vocabularyai.personalization.service.*;
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
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get learning profile")
    public void testGetLearningProfile_Success() throws Exception {
        mockMvc.perform(get("/api/v1/personalization/profile"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get personalized learning plan")
    public void testGetLearningPlan_Success() throws Exception {
        mockMvc.perform(get("/api/v1/personalization/plan"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get progress analysis")
    public void testGetProgressAnalysis_Success() throws Exception {
        mockMvc.perform(get("/api/v1/personalization/progress"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get personalized recommendations")
    public void testGetRecommendations_Success() throws Exception {
        mockMvc.perform(get("/api/v1/personalization/recommendations"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get weakness and strength analysis")
    public void testGetWeaknessStrength_Success() throws Exception {
        mockMvc.perform(get("/api/v1/personalization/weakness-strength"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get daily practice schedule")
    public void testGetDailyPractice_Success() throws Exception {
        mockMvc.perform(get("/api/v1/personalization/daily-practice"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should adjust difficulty level")
    public void testAdjustDifficulty_Success() throws Exception {
        String requestJson = "{\"level\":\"MEDIUM\"}";
        
        mockMvc.perform(post("/api/v1/personalization/difficulty")
            .contentType("application/json")
            .content(requestJson))
            .andExpect(status().isOk());
    }
}
