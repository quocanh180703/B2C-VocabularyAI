package com.vocabularyai.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.document.dto.GenerateQuizFromDocumentRequest;
import com.vocabularyai.document.entity.UserDocument;
import com.vocabularyai.document.service.DocumentExerciseAiServiceImpl.RawAiQuestion;
import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.quiz.entity.Question;
import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.entity.QuizVisibility;
import com.vocabularyai.quiz.repository.QuestionRepository;
import com.vocabularyai.quiz.repository.QuizRepository;
import com.vocabularyai.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentQuizPersistenceService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuizResponse persistQuizTransaction(User user, UserDocument doc, GenerateQuizFromDocumentRequest req, List<RawAiQuestion> questions) {
        String title = (req.getTitle() != null && !req.getTitle().isBlank())
                ? req.getTitle().trim()
                : (doc.getOriginalName().replaceFirst("[.][^.]+$", "") + " - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String levelStr = normalizeLevel(req.getLevel());
        Quiz.QuizLevel quizLevel;
        try {
            quizLevel = Quiz.QuizLevel.valueOf(levelStr);
        } catch (Exception e) {
            quizLevel = Quiz.QuizLevel.B1;
        }

        Quiz quiz = Quiz.builder()
                .title(title)
                .description("Bài tập trắc nghiệm do AI tạo từ tài liệu: " + doc.getOriginalName())
                .level(quizLevel)
                .timeLimit(Math.max(questions.size() * 60, 300))
                .status(Quiz.QuizStatus.PUBLISHED)
                .visibility(QuizVisibility.PRIVATE)
                .createdBy(user)
                .isAiGenerated(true)
                .sourceDocument(doc)
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);

        List<Question> questionEntities = new ArrayList<>();
        for (var q : questions) {
            try {
                String optionsJson = objectMapper.writeValueAsString(q.getOptions());
                Question qEntity = Question.builder()
                        .quiz(savedQuiz)
                        .content(q.getContent().trim())
                        .options(optionsJson)
                        .correctAnswer(q.getCorrectAnswer())
                        .explanation(q.getExplanation() != null ? q.getExplanation().trim() : "")
                        .build();
                questionEntities.add(qEntity);
            } catch (Exception e) {
                log.error("Failed to serialize question options", e);
            }
        }

        questionRepository.saveAll(questionEntities);
        log.info("Saved AI Quiz id={} with {} questions for user {}", savedQuiz.getId(), questionEntities.size(), user.getId());

        QuizResponse resp = QuizResponse.fromEntity(savedQuiz);
        resp.setQuestionCount(questionEntities.size());
        return resp;
    }

    private String normalizeLevel(String lvl) {
        if (lvl == null) return "B1";
        String clean = lvl.trim().toUpperCase();
        if (List.of("A1", "A2", "B1", "B2", "C1", "C2").contains(clean)) {
            return clean;
        }
        return "B1";
    }
}
