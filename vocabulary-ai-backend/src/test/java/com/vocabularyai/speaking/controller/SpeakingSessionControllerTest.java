package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.dto.response.SpeakingSessionResponse;
import com.vocabularyai.speaking.dto.response.SpeakingSessionSummaryResponse;
import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import com.vocabularyai.speaking.service.SpeakingSessionService;
import com.vocabularyai.user.entity.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SpeakingSessionController Tests")
public class SpeakingSessionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SpeakingSessionService sessionService;

    @BeforeEach
    public void setUp() {
        User testUser = User.builder().id(1L).username("testuser").email("test@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser, "password", Collections.emptyList())
        );
    }
    
    @Test
    @DisplayName("Should create speaking session")
    public void testCreateSession_Success() throws Exception {
        SpeakingSessionResponse response = SpeakingSessionResponse.builder()
            .id(1L)
            .topic("Self Introduction")
            .level(SpeakingLevel.A1)
            .status(SpeakingStatus.IN_PROGRESS)
            .build();

        when(sessionService.createSession(anyLong(), any())).thenReturn(response);

        String requestJson = "{\"topic\":\"Self Introduction\",\"level\":\"A1\",\"description\":\"Testing speaking\"}";

        mockMvc.perform(post("/api/ai/speaking/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("Should get session details")
    public void testGetSession_Success() throws Exception {
        SpeakingSessionSummaryResponse response = SpeakingSessionSummaryResponse.builder()
            .id(1L)
            .topic("Self Introduction")
            .build();

        when(sessionService.getSessionDetail(eq(1L), anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/ai/speaking/sessions/1"))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should list sessions")
    public void testListSessions_Success() throws Exception {
        when(sessionService.listSessions(anyLong(), any()))
            .thenReturn(new PageImpl<>(List.of(SpeakingSessionResponse.builder().id(1L).build()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/ai/speaking/sessions"))
            .andExpect(status().isOk());
    }
}
