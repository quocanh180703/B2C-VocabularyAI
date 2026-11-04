package com.vocabularyai.listening.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.listening.dto.CreateListeningRequest;
import com.vocabularyai.listening.dto.CreateListeningResponse;
import com.vocabularyai.listening.service.ListeningExerciseService;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListeningExerciseController Tests")
class ListeningExerciseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ListeningExerciseService listeningExerciseService;

    @InjectMocks
    private ListeningExerciseController listeningExerciseController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Long userId = 100L;

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver userResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(User.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return User.builder().id(userId).email("test@example.com").build();
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(listeningExerciseController)
                .setCustomArgumentResolvers(userResolver)
                .build();
    }

    @Test
    @DisplayName("Create exercise returns 200 and never exposes passageText or transcript")
    void testCreateExercise_NeverExposesPassageTextOrTranscript() throws Exception {
        CreateListeningRequest request = new CreateListeningRequest();
        request.setTopic("Introduce Yourself");
        request.setLevel("A2");
        request.setVoice("coral");

        CreateListeningResponse response = CreateListeningResponse.builder()
                .exerciseId(1L)
                .audioUrl("/api/ai/listening/audio/1/stream")
                .topic("Introduce Yourself")
                .level("A2")
                .voice("coral")
                .build();

        when(listeningExerciseService.createExercise(eq(userId), any(CreateListeningRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/ai/listening/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseId").value(1))
                .andExpect(jsonPath("$.audioUrl").value("/api/ai/listening/audio/1/stream"))
                .andExpect(jsonPath("$.topic").value("Introduce Yourself"))
                .andExpect(jsonPath("$.level").value("A2"))
                .andExpect(jsonPath("$.voice").value("coral"))
                .andExpect(jsonPath("$.passageText").doesNotExist())
                .andExpect(jsonPath("$.passage_text").doesNotExist())
                .andExpect(jsonPath("$.transcript").doesNotExist());
    }
}

