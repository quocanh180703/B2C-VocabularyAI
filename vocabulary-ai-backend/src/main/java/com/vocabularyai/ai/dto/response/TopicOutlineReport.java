package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicOutlineReport {
    private Integer targetWordCount;
    private Integer usedWordCount;
    private List<String> usedWords;
    private List<String> unusedWords;
    private List<String> usedStructures;
    private List<String> unusedStructures;
    private String outlineFeedback;
}

