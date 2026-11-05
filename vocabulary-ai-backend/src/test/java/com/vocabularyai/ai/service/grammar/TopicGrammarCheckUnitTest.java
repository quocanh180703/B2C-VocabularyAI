package com.vocabularyai.ai.service.grammar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.dto.request.TopicGrammarCheckRequest;
import com.vocabularyai.ai.dto.response.TopicGrammarCheckResponse;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TopicGrammarCheck Unit Tests")
public class TopicGrammarCheckUnitTest {

    @Mock
    private GeminiAIClientService geminiService;

    @Mock
    private PromptEngineeringService promptService;

    @Mock
    private CacheService cacheService;

    @Mock
    private com.vocabularyai.ai.repository.GrammarPracticePromptRepository promptRepository;

    @Mock
    private com.vocabularyai.vocabulary.repository.WordRepository wordRepository;

    @Mock
    private com.vocabularyai.vocabulary.repository.FavoriteWordRepository favoriteWordRepository;

    @Mock
    private com.vocabularyai.ai.repository.GrammarPracticeHistoryRepository historyRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GrammarCheckingServiceImpl grammarService;

    @Test
    @DisplayName("Should detect used words and parse AI topic check response correctly")
    void testCheckTopicGrammar_Success() {
        TopicGrammarCheckRequest request = TopicGrammarCheckRequest.builder()
            .text("Yesterday I booked a flight to Paris and unpacked my luggage.")
            .topic("travel")
            .level("B1")
            .suggestedWords(Arrays.asList("flight", "hotel", "luggage", "passport", "ticket"))
            .structures(Arrays.asList("Past Simple: S + V-ed / V2", "First Conditional: If + Present, S + will + V"))
            .build();

        String aiMockResponse = """
            {
              "correctedText": "Yesterday I booked a flight to Paris and unpacked my luggage.",
              "score": 92,
              "errors": [],
              "suggestions": [],
              "usedStructures": ["Past Simple: S + V-ed / V2"],
              "unusedStructures": ["First Conditional: If + Present, S + will + V"],
              "outlineFeedback": "Rất tốt! Bạn đã sử dụng chính xác thì quá khứ đơn và 2 từ vựng chủ đề du lịch."
            }
            """;

        when(geminiService.generateResponse(anyString(), eq(1L))).thenReturn(aiMockResponse);

        TopicGrammarCheckResponse response = grammarService.checkTopicGrammar(request, 1L);

        assertNotNull(response);
        assertEquals(92, response.getScore());
        assertEquals(request.getText(), response.getOriginalText());
        assertNotNull(response.getOutlineReport());
        assertEquals(5, response.getOutlineReport().getTargetWordCount());
        assertEquals(2, response.getOutlineReport().getUsedWordCount());
        assertTrue(response.getOutlineReport().getUsedWords().contains("flight"));
        assertTrue(response.getOutlineReport().getUsedWords().contains("luggage"));
        assertTrue(response.getOutlineReport().getUnusedWords().contains("hotel"));
        assertTrue(response.getOutlineReport().getUnusedWords().contains("passport"));
        assertTrue(response.getOutlineReport().getUnusedWords().contains("ticket"));
        assertEquals(1, response.getOutlineReport().getUsedStructures().size());
        assertEquals("Past Simple: S + V-ed / V2", response.getOutlineReport().getUsedStructures().get(0));
        assertNotNull(response.getOutlineReport().getOutlineFeedback());
    }

    @Test
    @DisplayName("Should fallback gracefully when AI response is not valid JSON")
    void testCheckTopicGrammar_JsonFallback() {
        TopicGrammarCheckRequest request = TopicGrammarCheckRequest.builder()
            .text("I like playing football.")
            .topic("sports")
            .level("A1")
            .suggestedWords(Arrays.asList("football", "stadium"))
            .structures(List.of("Subject + like/love + V-ing"))
            .build();

        when(geminiService.generateResponse(anyString(), eq(1L))).thenReturn("Sorry, I cannot format this as JSON right now.");

        TopicGrammarCheckResponse response = grammarService.checkTopicGrammar(request, 1L);

        assertNotNull(response);
        assertNotNull(response.getOutlineReport());
        assertEquals(2, response.getOutlineReport().getTargetWordCount());
        assertEquals(1, response.getOutlineReport().getUsedWordCount());
        assertTrue(response.getOutlineReport().getUsedWords().contains("football"));
        assertTrue(response.getOutlineReport().getUnusedWords().contains("stadium"));
    }
}

