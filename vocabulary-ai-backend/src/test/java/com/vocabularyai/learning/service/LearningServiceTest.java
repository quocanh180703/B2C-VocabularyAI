package com.vocabularyai.learning.service;

import com.vocabularyai.learning.repository.LearningProgressRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@DisplayName("LearningService Tests")
public class LearningServiceTest {
    
    @MockBean
    private LearningProgressRepository learningProgressRepository;
}
