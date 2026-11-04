package com.vocabularyai.quiz.dto;

import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.entity.QuizAttempt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuizAttemptResponse Unit Tests")
class QuizAttemptResponseTest {

    @Test
    @DisplayName("fromEntity should map quizTitle and quizId when quiz is present")
    void shouldMapQuizTitleWhenQuizPresent() {
        Quiz quiz = Quiz.builder()
                .id(10L)
                .title("Test thì hiện tại")
                .build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedAt = now.minusMinutes(2); // 120 seconds ago

        QuizAttempt attempt = QuizAttempt.builder()
                .id(99L)
                .quiz(quiz)
                .score(85.0)
                .totalQuestions(10)
                .correctAnswers(8)
                .startedAt(startedAt)
                .submittedAt(now)
                .build();

        QuizAttemptResponse response = QuizAttemptResponse.fromEntity(attempt);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(10L, response.getQuizId());
        assertEquals("Test thì hiện tại", response.getQuizTitle());
        assertEquals(85.0, response.getScore());
        assertEquals(10, response.getTotalQuestions());
        assertEquals(8, response.getCorrectAnswers());
        assertTrue(response.getCompleted());
        assertEquals(120L, response.getTimeTaken());
    }

    @Test
    @DisplayName("fromEntity should handle null quiz gracefully")
    void shouldHandleNullQuiz() {
        QuizAttempt attempt = QuizAttempt.builder()
                .id(100L)
                .quiz(null)
                .score(0.0)
                .build();

        QuizAttemptResponse response = QuizAttemptResponse.fromEntity(attempt);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertNull(response.getQuizId());
        assertNull(response.getQuizTitle());
        assertFalse(response.getCompleted());
    }

    @Test
    @DisplayName("fromEntity should return null when attempt is null")
    void shouldReturnNullWhenAttemptIsNull() {
        assertNull(QuizAttemptResponse.fromEntity(null));
    }
}
