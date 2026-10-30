package com.vocabularyai.vocabulary.service;

import com.vocabularyai.vocabulary.dto.CefrWordResponse;
import com.vocabularyai.vocabulary.dto.CefrWordSearchRequest;
import com.vocabularyai.vocabulary.dto.TopicCountResponse;
import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.entity.PartOfSpeech;
import com.vocabularyai.vocabulary.entity.WordTopic;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CefrWordServiceTest {

    @Mock
    private CefrWordRepository cefrWordRepository;

    @InjectMocks
    private CefrWordService cefrWordService;

    @Test
    @DisplayName("WordTopic.fromString should parse valid topics and return null for invalid")
    void testWordTopicFromString() {
        assertEquals(WordTopic.WORK, WordTopic.fromString("WORK"));
        assertEquals(WordTopic.WORK, WordTopic.fromString("work"));
        assertEquals(WordTopic.TRAVEL, WordTopic.fromString("TRAVEL"));
        assertNull(WordTopic.fromString("INVALID_TOPIC"));
        assertNull(WordTopic.fromString(null));
        assertNull(WordTopic.fromString(""));
    }

    @Test
    @DisplayName("searchWords should pass query, level and topic to searchAdvanced")
    void testSearchWordsWithTopicAndLevel() {
        CefrWord word = CefrWord.builder()
                .id("test-id-1")
                .lemma("office")
                .cefrLevel(CEFRLevel.B1)
                .partOfSpeech(PartOfSpeech.NOUN)
                .vietnameseMeaning("văn phòng")
                .topic(WordTopic.WORK)
                .build();

        Page<CefrWord> mockPage = new PageImpl<>(List.of(word), PageRequest.of(0, 20), 1);
        when(cefrWordRepository.searchAdvanced(eq("office"), eq(CEFRLevel.B1), eq(WordTopic.WORK), any(Pageable.class)))
                .thenReturn(mockPage);

        CefrWordSearchRequest request = CefrWordSearchRequest.builder()
                .query("office")
                .level(CEFRLevel.B1)
                .topic(WordTopic.WORK)
                .page(0)
                .size(20)
                .build();

        Page<CefrWordResponse> result = cefrWordService.searchWords(request);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("office", result.getContent().get(0).getLemma());
        assertEquals(WordTopic.WORK, result.getContent().get(0).getTopic());
        verify(cefrWordRepository, times(1)).searchAdvanced(eq("office"), eq(CEFRLevel.B1), eq(WordTopic.WORK), any(Pageable.class));
    }

    @Test
    @DisplayName("getTopicCounts should map count of all 12 topics correctly")
    void testGetTopicCounts() {
        List<Object[]> mockRows = new ArrayList<>();
        mockRows.add(new Object[]{WordTopic.WORK, 150L});
        mockRows.add(new Object[]{WordTopic.TRAVEL, 85L});

        when(cefrWordRepository.countWordsGroupedByTopic(CEFRLevel.B1)).thenReturn(mockRows);

        List<TopicCountResponse> counts = cefrWordService.getTopicCounts(CEFRLevel.B1);

        assertNotNull(counts);
        assertEquals(12, counts.size()); // all 12 topics present

        TopicCountResponse workTopic = counts.stream()
                .filter(t -> t.getTopic() == WordTopic.WORK)
                .findFirst()
                .orElse(null);
        assertNotNull(workTopic);
        assertEquals(150L, workTopic.getCount());

        TopicCountResponse travelTopic = counts.stream()
                .filter(t -> t.getTopic() == WordTopic.TRAVEL)
                .findFirst()
                .orElse(null);
        assertNotNull(travelTopic);
        assertEquals(85L, travelTopic.getCount());

        TopicCountResponse techTopic = counts.stream()
                .filter(t -> t.getTopic() == WordTopic.TECHNOLOGY)
                .findFirst()
                .orElse(null);
        assertNotNull(techTopic);
        assertEquals(0L, techTopic.getCount());
    }
}
