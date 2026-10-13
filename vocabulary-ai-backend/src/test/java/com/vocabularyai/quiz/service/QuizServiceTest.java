package com.vocabularyai.quiz.service;

import com.vocabularyai.quiz.repository.QuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@DisplayName("QuizService Tests")
public class QuizServiceTest {
    
    @MockBean
    private QuizRepository quizRepository;
}
