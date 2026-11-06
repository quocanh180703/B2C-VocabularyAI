package com.vocabularyai.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftQuestionDto {
    private String content;
    private List<String> options;
    private Integer correctAnswer;
    private String explanation;
}
