package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGeneratedWordDto {
    private String existingWordId;
    private String word;
    private String pronunciation;
    private String partOfSpeech;
    private String meaning;
    private String definition;
    private String example;
    private String exampleTranslation;
    private String level;
    private List<String> synonyms;
    private List<String> antonyms;
    private boolean isDuplicate;
}
