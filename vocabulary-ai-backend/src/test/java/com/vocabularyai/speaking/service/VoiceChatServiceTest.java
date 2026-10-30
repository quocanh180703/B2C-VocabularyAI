package com.vocabularyai.speaking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.whisper.WhisperService;
import com.vocabularyai.payment.entity.FeatureType;
import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.speaking.dto.voicechat.*;
import com.vocabularyai.speaking.service.voicechat.VoiceChatServiceImpl;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoiceChatService Unit Tests")
class VoiceChatServiceTest {

    @Mock
    private WhisperService whisperService;

    @Mock
    private GeminiAIClientService geminiAIClientService;

    @Mock
    private FeatureUsageService featureUsageService;

    @Mock
    private CefrWordRepository cefrWordRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private VoiceChatServiceImpl voiceChatService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(100L);
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Transcribe returns empty transcript if audio is null or empty")
    void testTranscribeEmptyAudio() {
        VoiceChatTranscribeResponse res = voiceChatService.transcribe(null);
        assertNotNull(res);
        assertEquals("", res.getTranscript());
        verifyNoInteractions(whisperService);
    }

    @Test
    @DisplayName("Transcribe calls whisperService and returns transcript")
    void testTranscribeSuccess() {
        byte[] content = new byte[2000];
        MockMultipartFile file = new MockMultipartFile("audioFile", "test.webm", "audio/webm", content);
        when(whisperService.transcribeAudio(any(org.springframework.web.multipart.MultipartFile.class), eq("en"))).thenReturn("Hello world");

        VoiceChatTranscribeResponse res = voiceChatService.transcribe(file);
        assertNotNull(res);
        assertEquals("Hello world", res.getTranscript());
        verify(whisperService).transcribeAudio(file, "en");
    }

    @Test
    @DisplayName("Reply does not consume quota if opening is true")
    void testReplyOpeningDoesNotConsumeQuota() {
        VoiceChatReplyRequest req = VoiceChatReplyRequest.builder()
                .topic("Hobbies")
                .level("B1")
                .opening(true)
                .build();

        VoiceChatReplyResponse res = voiceChatService.reply(req, testUser);
        assertNotNull(res);
        assertNotNull(res.getReply());
        assertTrue(res.getReply().contains("Vi"));
        verify(featureUsageService, never()).checkAndConsumeQuota(anyLong(), any());
        verifyNoInteractions(geminiAIClientService);
    }

    @Test
    @DisplayName("Reply consumes quota if opening is false and trims leading Vi prefix")
    void testReplyNormalTurnConsumesQuota() {
        VoiceChatReplyRequest req = VoiceChatReplyRequest.builder()
                .topic("Hobbies")
                .level("B1")
                .opening(false)
                .messages(List.of(
                        new VoiceChatMessageDto("user", "I like reading books.")
                ))
                .build();

        when(geminiAIClientService.generateResponse(anyString(), eq(100L)))
                .thenReturn("That is awesome. What genre of books do you read?");

        VoiceChatReplyResponse res = voiceChatService.reply(req, testUser);
        assertNotNull(res);
        assertEquals("That is awesome. What genre of books do you read?", res.getReply());
        verify(featureUsageService).checkAndConsumeQuota(100L, FeatureType.VOICE_CHAT);
    }

    @Test
    @DisplayName("Score parses LLM response and queries CefrWordRepository for new words")
    void testScoreSuccess() {
        VoiceChatScoreRequest req = VoiceChatScoreRequest.builder()
                .topic("Tech")
                .level("B1")
                .turns(List.of(new VoiceChatTurnDto("user", "AI is very powerful.")))
                .build();

        String mockLlmJson = "{\n" +
                "  \"grammar\": {\"score\": 80, \"comment\": \"Good grammar\"},\n" +
                "  \"vocabulary\": {\"score\": 85, \"comment\": \"Nice words\"},\n" +
                "  \"fluency\": {\"score\": 75, \"comment\": \"Smooth\"},\n" +
                "  \"content\": {\"score\": 90, \"comment\": \"Well argued\"},\n" +
                "  \"pronunciation\": {\"score\": 80, \"comment\": \"Clear\"},\n" +
                "  \"overall\": 82,\n" +
                "  \"summary\": \"Great job!\",\n" +
                "  \"newWords\": [\n" +
                "    {\"word\": \"resilient\", \"meaning\": \"kiên cường\", \"example\": \"He is resilient.\"}\n" +
                "  ]\n" +
                "}";

        when(geminiAIClientService.generateResponse(anyString(), eq(100L), eq(false)))
                .thenReturn(mockLlmJson);

        CefrWord mockWord = new CefrWord();
        mockWord.setId("word-uuid-123");
        mockWord.setLemma("resilient");

        when(cefrWordRepository.findFirstByLemmaIgnoreCase("resilient"))
                .thenReturn(Optional.of(mockWord));

        VoiceChatScoreResponse res = voiceChatService.score(null, req, testUser);

        assertNotNull(res);
        assertEquals(80, res.getGrammar().getScore());
        assertEquals(85, res.getVocabulary().getScore());
        assertEquals(75, res.getFluency().getScore());
        assertEquals(90, res.getContent().getScore());
        assertEquals(80, res.getPronunciation().getScore());
        assertTrue(res.getPronunciation().getEstimatedFromTranscript());

        assertNotNull(res.getNewWords());
        assertEquals(1, res.getNewWords().size());
        VoiceChatNewWordDto w = res.getNewWords().get(0);
        assertEquals("resilient", w.getWord());
        assertTrue(w.isInDictionary());
        assertEquals("word-uuid-123", w.getWordId());
    }
}
