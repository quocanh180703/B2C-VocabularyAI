package com.vocabularyai.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.document.dto.DocumentAnalysisResponse;
import com.vocabularyai.document.dto.DocumentChatRequest;
import com.vocabularyai.document.dto.DocumentChatResponse;
import com.vocabularyai.document.dto.GenerateQuizFromDocumentRequest;
import com.vocabularyai.document.entity.DocumentFileType;
import com.vocabularyai.document.entity.DocumentStatus;
import com.vocabularyai.document.entity.UserDocument;
import com.vocabularyai.document.repository.UserDocumentRepository;
import com.vocabularyai.document.service.DocumentExerciseAiServiceImpl;
import com.vocabularyai.document.service.DocumentQuizPersistenceService;
import com.vocabularyai.payment.entity.FeatureType;
import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentExerciseAiUnitTest {

        @Mock
        private GeminiAIClientService geminiAIClientService;

        @Mock
        private FeatureUsageService featureUsageService;

        @Mock
        private UserDocumentRepository userDocumentRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private DocumentQuizPersistenceService documentQuizPersistenceService;

        @Spy
        private ObjectMapper objectMapper = new ObjectMapper();

        @InjectMocks
        private DocumentExerciseAiServiceImpl aiExerciseService;

        private User testUser;
        private UserDocument testDoc;

        @BeforeEach
        void setUp() {
                testUser = User.builder()
                                .id(1L)
                                .username("student1")
                                .email("student1@example.com")
                                .role(User.UserRole.STUDENT)
                                .build();

                testDoc = UserDocument.builder()
                                .id(10L)
                                .user(testUser)
                                .originalName("technology_lesson.docx")
                                .fileType(DocumentFileType.DOCX)
                                .fileSize(2048L)
                                .extractedText("Artificial intelligence is transforming education and daily life...")
                                .charCount(68)
                                .status(DocumentStatus.READY)
                                .build();
        }

        @Test
        @DisplayName("Analyze document không trừ quota và trả về tóm tắt cùng level gợi ý")
        void testAnalyzeDocumentSuccessfully() {
                when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testDoc));

                String mockAiResponse = """
                                {
                                  "summary": "Tài liệu giới thiệu về ứng dụng của trí tuệ nhân tạo.",
                                  "suggestedLevel": "B2",
                                  "suggestedQuestionTypes": "MULTIPLE_CHOICE",
                                  "keyTopics": ["Trí tuệ nhân tạo", "Giáo dục hiện đại"],
                                  "keyVocabulary": ["transforming", "artificial intelligence"],
                                  "suggestedQuestionsCount": 10
                                }
                                """;

                when(geminiAIClientService.generateResponse(anyString(), eq(1L), eq(false))).thenReturn(mockAiResponse);

                DocumentAnalysisResponse resp = aiExerciseService.analyzeDocument(10L, 1L);
                assertNotNull(resp);
                assertEquals(10L, resp.getDocumentId());
                assertEquals("B2", resp.getSuggestedLevel());
                assertEquals(2, resp.getKeyTopics().size());
                verify(featureUsageService, never()).checkAndConsumeQuota(any(), any());
        }

        @Test
        @DisplayName("Analyze document lỗi ném BadRequestException (không fallback âm thầm)")
        void testAnalyzeDocumentThrowsBadRequestExceptionOnAiError() {
                when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testDoc));
                when(geminiAIClientService.generateResponse(anyString(), eq(1L), eq(false)))
                                .thenThrow(new RuntimeException("Gemini quota exceeded"));

                assertThrows(BadRequestException.class, () -> aiExerciseService.analyzeDocument(10L, 1L));
        }

        @Test
        @DisplayName("Chat with document trả về phản hồi kèm draftQuestions khi AI sinh câu hỏi")
        void testChatWithDocumentWithDraftQuestions() {
                when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testDoc));

                String mockChatJson = """
                                {
                                  "reply": "Dưới đây là 1 câu hỏi mẫu về thì quá khứ:",
                                  "draftQuestions": [
                                    {
                                      "content": "What did AI do yesterday?",
                                      "options": ["Helped", "Nothing", "Left", "Failed"],
                                      "correctAnswer": 0,
                                      "explanation": "Helped is the correct past simple form."
                                    }
                                  ]
                                }
                                """;
                when(geminiAIClientService.generateResponse(anyString(), eq(1L), eq(false)))
                                .thenReturn(mockChatJson);

                DocumentChatRequest request = DocumentChatRequest.builder()
                                .messages(List.of(
                                                DocumentChatRequest.ChatMessageItem.builder().role("user")
                                                                .content("Tạo 1 câu hỏi mẫu").build()))
                                .build();

                DocumentChatResponse resp = aiExerciseService.chatWithDocument(10L, 1L, request);
                assertNotNull(resp);
                assertTrue(resp.getReply().contains("Dưới đây là"));
                assertNotNull(resp.getDraftQuestions());
                assertEquals(1, resp.getDraftQuestions().size());
                assertEquals("What did AI do yesterday?", resp.getDraftQuestions().get(0).getContent());
                assertEquals(0, resp.getDraftQuestions().get(0).getCorrectAnswer());
        }

        @Test
        @DisplayName("Tạo bài tập thành công: trừ 1 lượt EXERCISE_GENERATION, gọi persistence service")
        void testGenerateQuizSuccessfully() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testDoc));

                String rawQuestionsJson = """
                                [
                                  {
                                    "content": "What is AI doing according to the text?",
                                    "options": ["Transforming education", "Nothing", "Sleeping", "Playing"],
                                    "correctAnswer": 0,
                                    "explanation": "AI is transforming education as mentioned in the lesson."
                                  }
                                ]
                                """;

                when(geminiAIClientService.generateResponse(anyString(), eq(1L), eq(false)))
                                .thenReturn(rawQuestionsJson);

                QuizResponse mockSavedQuiz = QuizResponse.builder()
                                .id(500L)
                                .title("Bài tập AI công nghệ")
                                .visibility("PRIVATE")
                                .isAiGenerated(true)
                                .questionCount(1)
                                .build();

                when(documentQuizPersistenceService.persistQuizTransaction(eq(testUser), eq(testDoc), any(), anyList()))
                                .thenReturn(mockSavedQuiz);

                GenerateQuizFromDocumentRequest req = GenerateQuizFromDocumentRequest.builder()
                                .title("Bài tập AI công nghệ")
                                .level("B2")
                                .questionCount(5)
                                .build();

                QuizResponse resp = aiExerciseService.generateQuiz(10L, 1L, req);
                assertNotNull(resp);
                assertEquals(500L, resp.getId());
                assertEquals("PRIVATE", resp.getVisibility());
                assertTrue(resp.getIsAiGenerated());

                verify(featureUsageService, times(1)).checkAndConsumeQuota(1L, FeatureType.EXERCISE_GENERATION);
                verify(featureUsageService, never()).rollbackQuota(any(), any());
                verify(documentQuizPersistenceService, times(1)).persistQuizTransaction(eq(testUser), eq(testDoc),
                                any(), anyList());
        }

        @Test
        @DisplayName("Tạo bài tập thất bại do AI lỗi: rollback 1 lượt quota")
        void testGenerateQuizFailsRollbackQuota() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testDoc));
                when(geminiAIClientService.generateResponse(anyString(), eq(1L), eq(false)))
                                .thenThrow(new RuntimeException("Gemini timeout"));

                GenerateQuizFromDocumentRequest req = GenerateQuizFromDocumentRequest.builder()
                                .level("B2")
                                .questionCount(5)
                                .build();

                assertThrows(Exception.class, () -> aiExerciseService.generateQuiz(10L, 1L, req));
                verify(featureUsageService, times(1)).checkAndConsumeQuota(1L, FeatureType.EXERCISE_GENERATION);
                verify(featureUsageService, times(1)).rollbackQuota(1L, FeatureType.EXERCISE_GENERATION);
        }
}
