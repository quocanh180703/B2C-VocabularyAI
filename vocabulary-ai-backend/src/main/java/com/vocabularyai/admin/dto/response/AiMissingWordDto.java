package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMissingWordDto implements Serializable {
    private String id;
    private String word;
    private String pronunciation;
    private String partOfSpeech;
    private String level;
    private String meaning;
    private String definition;
    private String example;
    private String categoryName;
}
