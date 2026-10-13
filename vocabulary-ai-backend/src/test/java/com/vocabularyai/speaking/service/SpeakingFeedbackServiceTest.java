package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.repository.SpeakingFeedbackRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@DisplayName("SpeakingFeedbackService Tests")
public class SpeakingFeedbackServiceTest {
    
    @MockBean
    private SpeakingFeedbackRepository feedbackRepository;
}
