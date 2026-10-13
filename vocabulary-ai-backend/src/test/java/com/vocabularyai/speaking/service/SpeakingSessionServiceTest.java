package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@DisplayName("SpeakingSessionService Tests")
public class SpeakingSessionServiceTest {
    
    @MockBean
    private SpeakingSessionRepository sessionRepository;
}
