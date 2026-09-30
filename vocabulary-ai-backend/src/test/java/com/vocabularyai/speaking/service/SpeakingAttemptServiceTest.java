package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.repository.SpeakingAttemptRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@DisplayName("SpeakingAttemptService Tests")
public class SpeakingAttemptServiceTest {
    
    @MockBean
    private SpeakingAttemptRepository attemptRepository;
}
