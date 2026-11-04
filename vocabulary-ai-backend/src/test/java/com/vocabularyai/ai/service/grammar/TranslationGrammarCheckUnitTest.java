package com.vocabularyai.ai.service.grammar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.dto.request.CheckTranslationRequest;
import com.vocabularyai.ai.dto.request.GeneratePracticePromptRequest;
import com.vocabularyai.ai.dto.response.PracticePromptResponse;
import com.vocabularyai.ai.dto.response.TranslationCheckResponse;
import com.vocabularyai.ai.entity.GrammarPracticePrompt;
import com.vocabularyai.ai.repository.GrammarPracticePromptRepository;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranslationGrammarCheck Unit Tests")
public class TranslationGrammarCheckUnitTest {

    @Mock
    private GeminiAIClientService geminiService;

    @Mock
    private PromptEngineeringService promptService;

    @Mock
    private CacheService cacheService;

    @Mock
    private GrammarPracticePromptRepository promptRepository;

    @Mock
    private WordRepository wordRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GrammarCheckingServiceImpl grammarService;

    @Test
    @DisplayName("Should return existing prompt from DB without calling AI if word already has at least 1 prompt and forceNew is false")
    void testGeneratePracticePrompt_UsesCachedWhenAtLeast1() {
        Word word = Word.builder().id(10L).word("essential").meaning("thiết yếu").level("B1").build();
        when(wordRepository.findById(10L)).thenReturn(Optional.of(word));

        List<GrammarPracticePrompt> existing = new ArrayList<>();
        existing.add(GrammarPracticePrompt.builder().id(1L).wordId(10L).mode("SENTENCE").viText("Nước là điều thiết yếu đối với sự sống.").build());

        when(promptRepository.findByWordIdAndMode(10L, "SENTENCE")).thenReturn(existing);

        GeneratePracticePromptRequest req = GeneratePracticePromptRequest.builder().wordId(10L).mode("SENTENCE").build();
        PracticePromptResponse resp = grammarService.generatePracticePrompt(req, 1L);

        assertNotNull(resp);
        assertEquals(10L, resp.getWordId());
        assertEquals("essential", resp.getTargetWord());
        assertTrue(resp.getViText().contains("thiết yếu"));
        // Gemini was NOT called!
        verify(geminiService, never()).generateResponse(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should return existing prompt via getExistingPracticePrompt without calling AI")
    void testGetExistingPracticePrompt() {
        Word word = Word.builder().id(10L).word("essential").meaning("thiết yếu").level("B1").build();
        when(wordRepository.findById(10L)).thenReturn(Optional.of(word));

        List<GrammarPracticePrompt> existing = new ArrayList<>();
        existing.add(GrammarPracticePrompt.builder().id(1L).wordId(10L).mode("SENTENCE").viText("Nước là điều thiết yếu đối với sự sống.").build());

        when(promptRepository.findByWordIdAndMode(10L, "SENTENCE")).thenReturn(existing);

        PracticePromptResponse resp = grammarService.getExistingPracticePrompt(10L, "SENTENCE");
        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("essential", resp.getTargetWord());
        verify(geminiService, never()).generateResponse(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should generate new prompt with AI and save to DB if 0 exist")
    void testGeneratePracticePrompt_GeneratesWithAIWhenEmpty() {
        Word word = Word.builder().id(20L).word("abundant").meaning("dồi dào").level("B2").build();
        when(wordRepository.findById(20L)).thenReturn(Optional.of(word));
        when(promptRepository.findByWordIdAndMode(20L, "SENTENCE")).thenReturn(Collections.emptyList());

        when(geminiService.generateResponse(anyString(), eq(1L))).thenReturn("Khu vực này có nguồn tài nguyên khoáng sản dồi dào.");
        when(promptRepository.save(any(GrammarPracticePrompt.class))).thenAnswer(invocation -> {
            GrammarPracticePrompt p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });

        GeneratePracticePromptRequest req = GeneratePracticePromptRequest.builder().wordId(20L).mode("SENTENCE").build();
        PracticePromptResponse resp = grammarService.generatePracticePrompt(req, 1L);

        assertNotNull(resp);
        assertEquals(99L, resp.getId());
        assertEquals(20L, resp.getWordId());
        assertEquals("abundant", resp.getTargetWord());
        assertEquals("Khu vực này có nguồn tài nguyên khoáng sản dồi dào.", resp.getViText());
        verify(geminiService, times(1)).generateResponse(anyString(), eq(1L));
        verify(promptRepository, times(1)).save(any(GrammarPracticePrompt.class));
    }

    @Test
    @DisplayName("Should check translation and accurately detect target word usage")
    void testCheckTranslation_Success() {
        CheckTranslationRequest req = CheckTranslationRequest.builder()
            .text("This region has abundant natural resources.")
            .viPrompt("Khu vực này có nguồn tài nguyên khoáng sản dồi dào.")
            .targetWord("abundant")
            .targetMeaning("dồi dào")
            .mode("SENTENCE")
            .build();

        String aiMockResponse = """
            {
              "correctedText": "This region has abundant natural resources.",
              "score": 95,
              "errors": [],
              "suggestions": [],
              "feedback": "Bản dịch rất xuất sắc và sử dụng đúng từ mục tiêu abundant."
            }
            """;

        when(geminiService.generateResponse(anyString(), eq(1L))).thenReturn(aiMockResponse);

        TranslationCheckResponse resp = grammarService.checkTranslation(req, 1L);

        assertNotNull(resp);
        assertEquals(95, resp.getScore());
        assertEquals(true, resp.getIsTargetWordUsed());
        assertEquals("abundant", resp.getTargetWord());
        assertEquals("This region has abundant natural resources.", resp.getCorrectedText());
        assertNotNull(resp.getFeedback());
    }
}

