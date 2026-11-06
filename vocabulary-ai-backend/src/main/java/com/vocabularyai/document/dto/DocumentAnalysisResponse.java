package com.vocabularyai.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentAnalysisResponse {

    private Long documentId;
    private String summary;
    private String suggestedLevel;
    private String suggestedQuestionTypes;
    private List<String> keyTopics;
    private List<String> keyVocabulary;
    private Integer suggestedQuestionsCount;
}
