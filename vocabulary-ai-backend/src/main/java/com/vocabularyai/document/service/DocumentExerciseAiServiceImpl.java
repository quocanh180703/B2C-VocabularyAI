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
import com.vocabularyai.document.dto.DraftQuestionDto;
import com.vocabularyai.document.dto.GenerateQuizFromDocumentRequest;
import com.vocabularyai.document.entity.DocumentStatus;
import com.vocabularyai.document.entity.UserDocument;
import com.vocabularyai.document.repository.UserDocumentRepository;
import com.vocabularyai.payment.entity.FeatureType;
import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.quiz.dto.QuizResponse;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExerciseAiServiceImpl implements DocumentExerciseAiService {

    private final GeminiAIClientService geminiAIClientService;
    private final FeatureUsageService featureUsageService;
    private final UserDocumentRepository userDocumentRepository;
    private final UserRepository userRepository;
    private final DocumentQuizPersistenceService documentQuizPersistenceService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public DocumentAnalysisResponse analyzeDocument(Long documentId, Long userId) {
        UserDocument doc = getValidDocument(documentId, userId);
        String textSnippet = getTextSnippet(doc.getExtractedText(), 100000);

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
            throw new BadRequestException("Không thể phân tích tài liệu bằng AI: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentChatResponse chatWithDocument(Long documentId, Long userId, DocumentChatRequest request) {
        UserDocument doc = getValidDocument(documentId, userId);
        String textSnippet = getTextSnippet(doc.getExtractedText(), 100000);

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
            - Trả lời thân thiện, súc tích bằng tiếng Việt trong trường "reply".
            - Nếu người dùng yêu cầu soạn câu hỏi, đưa ra ví dụ bài tập hoặc gợi ý câu hỏi mẫu, hãy tạo các câu hỏi trắc nghiệm tiếng Anh hoàn chỉnh trong danh sách "draftQuestions". Nếu không có yêu cầu câu hỏi hoặc chỉ hỏi đáp thông thường, để "draftQuestions" là mảng rỗng [].
            - Mỗi câu hỏi trong "draftQuestions" gồm: "content" (tiếng Anh), "options" (mảng 4 lựa chọn tiếng Anh), "correctAnswer" (chỉ số 0-3), "explanation" (giải thích chi tiết bằng tiếng Việt).
            - Trả về kết quả dưới định dạng JSON duy nhất (không kèm markdown ngoài JSON):
            {
              "reply": "Câu trả lời thân thiện của bạn bằng tiếng Việt...",
              "draftQuestions": [
                {
                  "content": "Câu hỏi trắc nghiệm tiếng Anh...",
                  "options": ["Lựa chọn A", "Lựa chọn B", "Lựa chọn C", "Lựa chọn D"],
                  "correctAnswer": 0,
                  "explanation": "Giải thích chi tiết tại sao đúng..."
                }
              ]
            }
            """.formatted(textSnippet, chatHistory.toString());

        try {
            String aiRaw = geminiAIClientService.generateResponse(prompt, userId, false);
            String cleaned = cleanJson(aiRaw);
            String replyText = null;
            List<DraftQuestionDto> draftQuestions = new ArrayList<>();

            try {
                JsonNode root = objectMapper.readTree(cleaned);
                if (root.has("reply") && !root.get("reply").asText().isBlank()) {
                    replyText = root.get("reply").asText();
                }
                if (root.has("draftQuestions") && root.get("draftQuestions").isArray()) {
                    for (JsonNode qNode : root.get("draftQuestions")) {
                        String content = qNode.path("content").asText(null);
                        JsonNode optsNode = qNode.path("options");
                        List<String> options = new ArrayList<>();
                        if (optsNode.isArray()) {
                            optsNode.forEach(o -> options.add(o.asText()));
                        }
                        int correct = qNode.path("correctAnswer").asInt(0);
                        String explanation = qNode.path("explanation").asText("");
                        if (content != null && !content.isBlank() && options.size() == 4) {
                            draftQuestions.add(DraftQuestionDto.builder()
                                    .content(content.trim())
                                    .options(options)
                                    .correctAnswer(Math.max(0, Math.min(3, correct)))
                                    .explanation(explanation)
                                    .build());
                        }
                    }
                }
            } catch (Exception parseEx) {
                log.debug("Chat response parsed as fallback text: {}", parseEx.getMessage());
            }

            if (replyText == null || replyText.isBlank()) {
                replyText = (aiRaw != null && !aiRaw.isBlank()) ? aiRaw.trim() : "Tôi đã hiểu yêu cầu của bạn. Bạn có thể tiến hành tạo bài tập bất cứ lúc nào!";
            }

            return DocumentChatResponse.builder()
                    .reply(replyText)
                    .draftQuestions(draftQuestions)
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
            int targetCount = request.getQuestionCount() != null ? Math.min(Math.max(request.getQuestionCount(), 5), 20) : 10;

            // 2. Generate questions via AI
            List<RawAiQuestion> rawQuestions = generateRawQuestionsWithRetry(doc, request, userId, targetCount);

            // 3. AI Self-Verification step
            List<RawAiQuestion> verifiedQuestions = verifyQuestionsWithAi(rawQuestions, doc, userId);
            List<RawAiQuestion> validQuestions = filterAndDeduplicate(verifiedQuestions);
            if (validQuestions.isEmpty()) {
                validQuestions = filterAndDeduplicate(rawQuestions);
            }

            // 4. Gate logic: If valid questions are less than target, generate 1 round of supplementary questions
            if (validQuestions.size() < targetCount) {
                int missing = targetCount - validQuestions.size();
                log.info("Quiz has {}/{} valid questions, generating {} supplementary questions", validQuestions.size(), targetCount, missing);
                try {
                    List<RawAiQuestion> supplement = generateSupplementaryQuestions(doc, request, userId, missing, validQuestions);
                    for (var q : supplement) {
                        if (validQuestions.size() < targetCount) {
                            validQuestions.add(q);
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Supplementary generation error: {}", ex.getMessage());
                }
            }

            // 5. Final Gate Check: Must have at least 1 valid question
            if (validQuestions.isEmpty()) {
                throw new BadRequestException("AI không thể tạo câu hỏi trắc nghiệm hợp lệ đạt chuẩn chất lượng.");
            }

            // 6. Save Quiz and Questions in database transaction via separate service
            return documentQuizPersistenceService.persistQuizTransaction(user, doc, request, validQuestions);
        } catch (Exception e) {
            log.error("Failed to generate quiz for document id={}, rolling back quota for user {}", documentId, userId, e);
            featureUsageService.rollbackQuota(userId, FeatureType.EXERCISE_GENERATION);
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new BadRequestException("Không thể tạo bài tập từ tài liệu: " + e.getMessage());
        }
    }

    private List<RawAiQuestion> generateRawQuestionsWithRetry(UserDocument doc, GenerateQuizFromDocumentRequest req, Long userId, int targetCount) {
        String textSnippet = getTextSnippet(doc.getExtractedText(), 100000);
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
                List<RawAiQuestion> valid = filterAndDeduplicate(list);
                if (!valid.isEmpty()) {
                    return valid;
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

    private List<RawAiQuestion> generateSupplementaryQuestions(UserDocument doc, GenerateQuizFromDocumentRequest req, Long userId, int count, List<RawAiQuestion> existing) {
        String textSnippet = getTextSnippet(doc.getExtractedText(), 100000);
        String level = normalizeLevel(req.getLevel());
        StringBuilder existingText = new StringBuilder("Không được trùng lặp các câu hỏi đã có sau:\n");
        for (var q : existing) {
            existingText.append("- ").append(q.getContent()).append("\n");
        }

        String prompt = """
            Hãy bổ sung chính xác %d câu hỏi trắc nghiệm tiếng Anh cấp độ %s từ tài liệu sau.
            %s
            
            TÀI LIỆU NGUỒN:
            \"\"\"
            %s
            \"\"\"
            
            Trả về DUY NHẤT một mảng JSON %d câu hỏi:
            [
              {
                "content": "...",
                "options": ["A", "B", "C", "D"],
                "correctAnswer": 0,
                "explanation": "..."
              }
            ]
            """.formatted(count, level, existingText.toString(), textSnippet, count);

        try {
            String aiRaw = geminiAIClientService.generateResponse(prompt, userId, false);
            String cleaned = cleanJson(aiRaw);
            List<RawAiQuestion> list = objectMapper.readValue(cleaned, new TypeReference<List<RawAiQuestion>>() {});
            List<RawAiQuestion> valid = filterAndDeduplicate(list);
            Set<String> existingContents = new HashSet<>();
            existing.forEach(e -> existingContents.add(e.getContent().trim().toLowerCase()));

            List<RawAiQuestion> result = new ArrayList<>();
            for (var q : valid) {
                if (!existingContents.contains(q.getContent().trim().toLowerCase())) {
                    result.add(q);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to generate supplementary questions: {}", e.getMessage());
            return List.of();
        }
    }

    private List<RawAiQuestion> filterAndDeduplicate(List<RawAiQuestion> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<RawAiQuestion> valid = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (var q : list) {
            if (q != null && q.getContent() != null && !q.getContent().isBlank()
                    && q.getOptions() != null && q.getOptions().size() == 4
                    && q.getOptions().stream().noneMatch(o -> o == null || o.isBlank())
                    && q.getCorrectAnswer() != null && q.getCorrectAnswer() >= 0 && q.getCorrectAnswer() <= 3) {
                String norm = q.getContent().trim().toLowerCase();
                if (seen.add(norm)) {
                    valid.add(q);
                }
            }
        }
        return valid;
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
