package com.vocabularyai.quiz.service;

import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.quiz.dto.QuizWithQuestionsResponse;
import com.vocabularyai.quiz.dto.ReviewExerciseRequest;
import com.vocabularyai.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExerciseReviewService {

    Page<QuizResponse> getPendingQuizzes(Pageable pageable);

    QuizWithQuestionsResponse getPendingQuizDetail(Long quizId);

    QuizResponse reviewQuiz(Long quizId, User reviewer, ReviewExerciseRequest request);
}

