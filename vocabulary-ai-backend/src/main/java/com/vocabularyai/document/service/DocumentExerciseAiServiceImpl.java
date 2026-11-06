package com.vocabularyai.document.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.document.dto.DocumentAnalysisResponse;
import com.vocabularyai.document.dto.DocumentChatRequest;
import com.vocabularyai.document.dto.DocumentChatResponse;
import com.vocabularyai.document.dto.GenerateQuizFromDocumentRequest;
import com.vocabularyai.document.entity.DocumentStatus;
import com.vocabularyai.document.entity.UserDocument;
import com.vocabularyai.document.repository.UserDocumentRepository;
import com.vocabularyai.payment.entity.FeatureType;
import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.quiz.entity.Question;
import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.entity.QuizVisibility;
import com.vocabularyai.quiz.repository.QuestionRepository;
import com.vocabularyai.quiz.repository.QuizRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class DocumentExerciseAiServiceImpl implements DocumentExerciseAiService {

    private final GeminiAIClientService geminiAIClientService;
    private final FeatureUsageService featureUsageService;
    private final UserDocumentRepository userDocumentRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public DocumentAnalysisResponse analyzeDocument(Long documentId, Long userId) {
        UserDocument doc = getValidDocument(documentId, userId);
        String textSnippet = getTextSnippet(doc.getExtractedText(), 8000);

        String prompt = """
            Bạn là một chuyên gia ngôn ngữ tiếng Anh và sư phạm. Hãy phân tích nội dung tài liệu sau và trả về kết quả dưới định dạng JSON duy nhất:
            
            NỘI DUNG TÀI LIỆU:
            \"\"\"
            %s
            \"\"\"
            
            YÊU CẦU:
            Trả về đúng JSON theo cấu trúc sau (không kèm markdown ngoài JSON):
            {
              "summary": "Tóm tắt ngắn gọn 2-3 câu về nội dung chính của tài liệu bằng tiếng Việt",
              "suggestedLevel": "Cấp độ CEFR phù hợp nhất (chỉ chọn 1 trong: A1, A2, B1, B2, C1)",
              "suggestedQuestionTypes": "MULTIPLE_CHOICE",
              "keyTopics": ["Chủ đề 1", "Chủ đề 2", "Chủ đề 3"],
              "keyVocabulary": ["từ/cụm từ 1", "từ/cụm từ 2", "từ/cụm từ 3", "từ/cụm từ 4", "từ/cụm từ 5"],
              "suggestedQuestionsCount": 10
            }
            """.formatted(textSnippet);

        try {
            String aiRaw = geminiAIClientService.generateResponse(prompt, userId, false);
            String cleaned = cleanJson(aiRaw);
            JsonNode root = objectMapper.readTree(cleaned);

            List<String> keyTopics = new ArrayList<>();
            if (root.has("keyTopics") && root.get("keyTopics").isArray()) {
                root.get("keyTopics").forEach(node -> keyTopics.add(node.asText()));
            }

            List<String> keyVocab = new ArrayList<>();
            if (root.has("keyVocabulary") && root.get("keyVocabulary").isArray()) {
                root.get("keyVocabulary").forEach(node -> keyVocab.add(node.asText()));
            }

            return DocumentAnalysisResponse.builder()
                    .documentId(doc.getId())
                    .summary(root.path("summary").asText("Tài liệu gồm các nội dung kiến thức tiếng Anh trọng tâm."))
                    .suggestedLevel(normalizeLevel(root.path("suggestedLevel").asText("B1")))
                    .suggestedQuestionTypes(root.path("suggestedQuestionTypes").asText("MULTIPLE_CHOICE"))
                    .keyTopics(keyTopics.isEmpty() ? List.of("Từ vựng trọng tâm", "Đọc hiểu", "Ngữ pháp") : keyTopics)
                    .keyVocabulary(keyVocab)
                    .suggestedQuestionsCount(root.path("suggestedQuestionsCount").asInt(10))
                    .build();
        } catch (Exception e) {
            log.error("Failed to analyze document id={} with Gemini", documentId, e);
            return DocumentAnalysisResponse.builder()
                    .documentId(doc.getId())
                    .summary("Tài liệu đã được trích xuất thành công và sẵn sàng để tạo câu hỏi trắc nghiệm.")
                    .suggestedLevel("B1")
                    .suggestedQuestionTypes("MULTIPLE_CHOICE")
                    .keyTopics(List.of("Từ vựng chính", "Ngữ cảnh bài học"))
                    .keyVocabulary(List.of())
                    .suggestedQuestionsCount(10)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentChatResponse chatWithDocument(Long documentId, Long userId, DocumentChatRequest request) {
        UserDocument doc = getValidDocument(documentId, userId);
        String textSnippet = getTextSnippet(doc.getExtractedText(), 10000);

        StringBuilder chatHistory = new StringBuilder();
        if (request.getMessages() != null) {
            for (var msg : request.getMessages()) {
                String role = "user".equalsIgnoreCase(msg.getRole()) ? "Người dùng" : "Trợ lý AI";
                chatHistory.append(role).append(": ").append(msg.getContent()).append("\n");
            }
        }

        String prompt = """
            Bạn là trợ lý AI thông minh hỗ trợ giáo viên và học viên tinh chỉnh bài tập tiếng Anh từ tài liệu học tập.
            
            TÀI LIỆU NGUỒN:
            \"\"\"
            %s
            \"\"\"
            
            LỊCH SỬ TRAO ĐỔI VỚI NGƯỜI DÙNG:
            %s
            
            HƯỚNG DẪN:
            - Trả lời thân thiện, súc tích bằng tiếng Việt.
            - Nếu người dùng yêu cầu tập trung vào phần nào (ví dụ: chỉ hỏi về thì quá khứ, chỉ hỏi từ vựng chương 1, giảm độ khó...), hãy gợi ý các câu hỏi mẫu phù hợp.
            - Hướng dẫn người dùng có thể bấm 'Tạo bài tập' bất cứ lúc nào khi đã hài lòng với định hướng.
            """.formatted(textSnippet, chatHistory.toString());

        try {
            String reply = geminiAIClientService.generateResponse(prompt, userId, false);
            return DocumentChatResponse.builder()
                    .reply(reply != null ? reply.trim() : "Tôi đã hiểu yêu cầu của bạn. Bạn có thể tiến hành tạo bài tập bất cứ lúc nào!")
                    .build();
        } catch (Exception e) {
            log.error("Failed to chat with document id={}", documentId, e);
            throw new BadRequestException("Không thể kết nối với AI lúc này: " + e.getMessage());
        }
    }

    @Override
    public QuizResponse generateQuiz(Long documentId, Long userId, GenerateQuizFromDocumentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Người dùng", "id", userId));
        UserDocument doc = getValidDocument(documentId, userId);

        // 1. Check & consume quota
        featureUsageService.checkAndConsumeQuota(userId, FeatureType.EXERCISE_GENERATION);

        try {
            // 2. Generate questions via AI
            List<RawAiQuestion> rawQuestions = generateRawQuestionsWithRetry(doc, request, userId);

            // 3. AI Self-Verification step
            List<RawAiQuestion> verifiedQuestions = verifyQuestionsWithAi(rawQuestions, doc, userId);
            if (verifiedQuestions.isEmpty()) {
                verifiedQuestions = rawQuestions;
            }

            // 4. Save Quiz and Questions in database transaction
            return persistQuizTransaction(user, doc, request, verifiedQuestions);
        } catch (Exception e) {
            log.error("Failed to generate quiz for document id={}, rolling back quota for user {}", documentId, userId, e);
            featureUsageService.rollbackQuota(userId, FeatureType.EXERCISE_GENERATION);
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new BadRequestException("Không thể tạo bài tập từ tài liệu: " + e.getMessage());
        }
    }

    private List<RawAiQuestion> generateRawQuestionsWithRetry(UserDocument doc, GenerateQuizFromDocumentRequest req, Long userId) {
        String textSnippet = getTextSnippet(doc.getExtractedText(), 15000);
        int targetCount = req.getQuestionCount() != null ? Math.min(Math.max(req.getQuestionCount(), 5), 20) : 10;
        String level = normalizeLevel(req.getLevel());
        String extra = req.getExtraPrompt() != null && !req.getExtraPrompt().isBlank() ? "Ghi chú thêm từ người dùng: " + req.getExtraPrompt().trim() : "";

        String prompt = """
            Bạn là chuyên gia soạn đề thi tiếng Anh chuẩn quốc tế.
            Dựa trên nội dung tài liệu sau, hãy soạn chính xác %d câu hỏi trắc nghiệm (Multiple Choice Questions) cấp độ CEFR %s.
            
            TÀI LIỆU NGUỒN:
            \"\"\"
            %s
            \"\"\"
            %s
            
            QUY ĐỊNH BẮT BUỘC:
            1. Mỗi câu hỏi PHẢI có đúng 4 lựa chọn (options) là A, B, C, D (mảng 4 phần tử chuỗi).
            2. Chỉ có ĐÚNG 1 đáp án chính xác. Ba lựa chọn còn lại phải hợp lý nhưng sai rõ ràng, không được gây tranh cãi.
            3. correctAnswer là chỉ số số nguyên từ 0 đến 3 (0 ứng với lựa chọn đầu tiên, 1 ứng với lựa chọn thứ hai,...).
            4. explanation: Phần giải thích chi tiết bằng tiếng Việt, nêu rõ tại sao đáp án đó đúng và giải thích các lựa chọn khác.
            5. Nội dung câu hỏi và các lựa chọn bằng tiếng Anh.
            
            ĐỊNH DẠNG ĐẦU RA:
            Trả về DUY NHẤT một mảng JSON các đối tượng câu hỏi, không kèm markdown code block hoặc lời dẫn:
            [
              {
                "content": "Câu hỏi tiếng Anh...",
                "options": ["Lựa chọn 1", "Lựa chọn 2", "Lựa chọn 3", "Lựa chọn 4"],
                "correctAnswer": 0,
                "explanation": "Giải thích chi tiết bằng tiếng Việt..."
              }
            ]
            """.formatted(targetCount, level, textSnippet, extra);

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                String aiRaw = geminiAIClientService.generateResponse(prompt, userId, false);
                String cleaned = cleanJson(aiRaw);
                List<RawAiQuestion> list = objectMapper.readValue(cleaned, new TypeReference<List<RawAiQuestion>>() {});
                if (list != null && !list.isEmpty()) {
                    List<RawAiQuestion> valid = new ArrayList<>();
                    for (var q : list) {
                        if (q.getContent() != null && !q.getContent().isBlank()
                                && q.getOptions() != null && q.getOptions().size() == 4
                                && q.getCorrectAnswer() != null && q.getCorrectAnswer() >= 0 && q.getCorrectAnswer() <= 3) {
                            valid.add(q);
                        }
                    }
                    if (!valid.isEmpty()) {
                        return valid;
                    }
                }
            } catch (Exception e) {
                log.warn("Attempt {} to generate quiz questions failed: {}", attempt, e.getMessage());
                if (attempt == 2) {
                    throw new BadRequestException("AI không thể tạo cấu trúc câu hỏi trắc nghiệm hợp lệ. Vui lòng thử lại.");
                }
            }
        }
        throw new BadRequestException("Không thể tạo câu hỏi từ tài liệu.");
    }

    private List<RawAiQuestion> verifyQuestionsWithAi(List<RawAiQuestion> questions, UserDocument doc, Long userId) {
        if (questions == null || questions.isEmpty()) return List.of();

        try {
            String questionsJson = objectMapper.writeValueAsString(questions);
            String prompt = """
                Bạn là một chuyên gia thẩm định đề thi tiếng Anh (Quality Assurance Examiner).
                Hãy kiểm tra danh sách câu hỏi trắc nghiệm sau đây:
                %s
                
                TIÊU CHÍ KIỂM TRA:
                1. Đáp án đúng (correctAnswer) có thực sự chính xác không? Có đáp án nào khác cũng đúng không?
                2. Câu hỏi có rõ ràng, không gây nhầm lẫn không?
                3. Nếu câu nào bị lỗi 2 đáp án cùng đúng hoặc câu hỏi tối nghĩa, hãy chỉnh sửa lại câu đó hoặc thay thế bằng 1 câu hỏi chất lượng tương đương.
                
                ĐỊNH DẠNG ĐẦU RA:
                Trả về DUY NHẤT một mảng JSON các câu hỏi đã được thẩm định và chỉnh sửa hoàn thiện:
                [
                  {
                    "content": "...",
                    "options": ["A", "B", "C", "D"],
                    "correctAnswer": 0,
                    "explanation": "..."
                  }
                ]
                """.formatted(questionsJson);

            String aiRaw = geminiAIClientService.generateResponse(prompt, userId, false);
            String cleaned = cleanJson(aiRaw);
            List<RawAiQuestion> verified = objectMapper.readValue(cleaned, new TypeReference<List<RawAiQuestion>>() {});
            if (verified != null && !verified.isEmpty()) {
                return verified;
            }
        } catch (Exception e) {
            log.warn("Self-verification step failed, fallback to original questions: {}", e.getMessage());
        }
        return questions;
    }

    @Transactional
    protected QuizResponse persistQuizTransaction(User user, UserDocument doc, GenerateQuizFromDocumentRequest req, List<RawAiQuestion> questions) {
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

    private UserDocument getValidDocument(Long documentId, Long userId) {
        UserDocument doc = userDocumentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Tài liệu", "id", documentId));

        if (doc.getStatus() == DocumentStatus.FAILED || doc.getExtractedText() == null || doc.getExtractedText().isBlank()) {
            throw new BadRequestException("Tài liệu chưa sẵn sàng hoặc trích xuất nội dung thất bại: " + doc.getErrorMessage());
        }
        return doc;
    }

    private String getTextSnippet(String text, int maxChars) {
        if (text == null) return "";
        return text.length() <= maxChars ? text : text.substring(0, maxChars);
    }

    private String normalizeLevel(String lvl) {
        if (lvl == null) return "B1";
        String clean = lvl.trim().toUpperCase();
        if (List.of("A1", "A2", "B1", "B2", "C1", "C2").contains(clean)) {
            return clean;
        }
        return "B1";
    }

    private String cleanJson(String raw) {
        if (raw == null) return "[]";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RawAiQuestion {
        private String content;
        private List<String> options;
        private Integer correctAnswer;
        private String explanation;
    }
}
