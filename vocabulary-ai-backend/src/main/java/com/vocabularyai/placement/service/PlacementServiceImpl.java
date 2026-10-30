package com.vocabularyai.placement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.placement.dto.PlacementQuestionResponse;
import com.vocabularyai.placement.dto.PlacementStatusResponse;
import com.vocabularyai.placement.dto.PlacementSubmitRequest;
import com.vocabularyai.placement.dto.PlacementSubmitResponse;
import com.vocabularyai.placement.entity.PlacementAttempt;
import com.vocabularyai.placement.repository.PlacementAttemptRepository;
import com.vocabularyai.quiz.entity.Question;
import com.vocabularyai.quiz.repository.QuestionRepository;
import com.vocabularyai.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementServiceImpl implements PlacementService {

    private final PlacementAttemptRepository placementAttemptRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> CEFR_LEVELS = List.of("A1", "A2", "B1", "B2", "C1");

    @Override
    @Transactional(readOnly = true)
    public PlacementStatusResponse getStatus(Long userId) {
        log.debug("Getting placement status for user {}", userId);
        Optional<PlacementAttempt> latestOpt = placementAttemptRepository.findTopByUserId(userId);
        int totalAttempts = (int) placementAttemptRepository.countByUserId(userId);

        if (latestOpt.isEmpty()) {
            return PlacementStatusResponse.builder()
                .completed(false)
                .placementLevel(null)
                .attemptCount(0)
                .scoreByLevel(null)
                .build();
        }

        PlacementAttempt latest = latestOpt.get();
        Map<String, Integer> scoreMap = parseScoreMap(latest.getScoreByLevel());

        return PlacementStatusResponse.builder()
            .completed(true)
            .placementLevel(latest.getLevelResult())
            .attemptCount(totalAttempts)
            .scoreByLevel(scoreMap)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlacementQuestionResponse> generateTest() {
        log.info("Generating placement test questions from published quiz bank");
        List<Question> allPublished = questionRepository.findAllPublishedQuestions();

        Map<String, List<Question>> questionsByLevel = new HashMap<>();
        for (Question q : allPublished) {
            if (q.getQuiz() != null && q.getQuiz().getLevel() != null) {
                String lvl = q.getQuiz().getLevel().name().toUpperCase();
                questionsByLevel.computeIfAbsent(lvl, k -> new ArrayList<>()).add(q);
            }
        }

        List<PlacementQuestionResponse> result = new ArrayList<>();

        // Maintain pedagogical ascending difficulty: A1 -> A2 -> B1 -> B2 -> C1
        for (String lvl : CEFR_LEVELS) {
            List<Question> list = questionsByLevel.getOrDefault(lvl, new ArrayList<>());
            List<Question> shuffled = new ArrayList<>(list);
            Collections.shuffle(shuffled);

            // Select up to 4 questions per level
            int pickCount = Math.min(4, shuffled.size());
            for (int i = 0; i < pickCount; i++) {
                Question q = shuffled.get(i);
                result.add(PlacementQuestionResponse.builder()
                    .id(q.getId())
                    .level(lvl)
                    .content(q.getContent())
                    .options(q.getOptionsList())
                    .build());
            }
        }

        log.info("Generated placement test with {} questions across {} levels", result.size(), CEFR_LEVELS.size());
        return result;
    }

    @Override
    @Transactional
    public PlacementSubmitResponse submitTest(User user, PlacementSubmitRequest request) {
        log.info("Submitting placement test for user {}", user.getId());

        Map<String, Integer> scoreByLevel = new LinkedHashMap<>();
        for (String lvl : CEFR_LEVELS) {
            scoreByLevel.put(lvl, 0);
        }

        if (request != null && request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            List<Long> questionIds = request.getAnswers().stream()
                .map(PlacementSubmitRequest.AnswerItem::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            Map<Long, Question> questionMap = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

            for (PlacementSubmitRequest.AnswerItem item : request.getAnswers()) {
                if (item.getQuestionId() == null || item.getSelectedIndex() == null) continue;
                Question q = questionMap.get(item.getQuestionId());
                if (q != null && q.getQuiz() != null && q.getQuiz().getLevel() != null) {
                    String lvl = q.getQuiz().getLevel().name().toUpperCase();
                    if (item.getSelectedIndex().equals(q.getCorrectAnswer())) {
                        scoreByLevel.put(lvl, scoreByLevel.getOrDefault(lvl, 0) + 1);
                    }
                }
            }
        }

        // Evaluate level with continuous mastery gate (>= 3/4 continuous)
        String finalLevel = "A1";
        for (String lvl : CEFR_LEVELS) {
            int score = scoreByLevel.getOrDefault(lvl, 0);
            if (score >= 3) {
                finalLevel = lvl;
            } else {
                break; // Stop at first failed level
            }
        }

        String feedback = buildFeedbackMessage(finalLevel);

        String scoreJson;
        try {
            scoreJson = objectMapper.writeValueAsString(scoreByLevel);
        } catch (Exception e) {
            scoreJson = "{}";
        }

        PlacementAttempt attempt = PlacementAttempt.builder()
            .user(user)
            .levelResult(finalLevel)
            .scoreByLevel(scoreJson)
            .completedAt(LocalDateTime.now())
            .build();

        placementAttemptRepository.save(attempt);
        log.info("User {} completed placement test: finalLevel={}, scores={}", user.getId(), finalLevel, scoreByLevel);

        return PlacementSubmitResponse.builder()
            .level(finalLevel)
            .scoreByLevel(scoreByLevel)
            .feedback(feedback)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getLatestPlacementLevel(Long userId) {
        return placementAttemptRepository.findTopByUserId(userId)
            .map(PlacementAttempt::getLevelResult);
    }

    private Map<String, Integer> parseScoreMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String buildFeedbackMessage(String level) {
        return switch (level) {
            case "A1" -> "Vi sẽ cùng bạn xây dựng nền tảng từ vựng cơ bản A1 thật vững chắc!";
            case "A2" -> "Bạn có nền tảng tốt! Vi sẽ đồng hành giúp bạn mở rộng vốn từ và phản xạ ở mức A2.";
            case "B1" -> "Tuyệt vời! Bạn đạt mức B1 — các bài quiz, flashcard và lộ trình học sẽ được thiết lập theo cấp độ này.";
            case "B2" -> "Ấn tượng lắm! Bạn có vốn tiếng Anh B2 rất tốt. Hãy cùng Vi chinh phục các chủ đề chuyên sâu!";
            case "C1" -> "Xuất sắc! Bạn đạt cấp độ C1 cao cấp. Vi sẽ chuẩn bị các thử thách từ vựng và luyện nói học thuật cho bạn!";
            default -> "Vi đã xếp lớp cho bạn ở cấp độ " + level + ". Bắt đầu học ngay nhé!";
        };
    }
}
