package com.vocabularyai.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.quiz.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Question Response DTO
 * Note: correctAnswer is hidden from students during quiz
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {
    
    private Long id;
    private String content;
    private List<String> options;
    private Integer correctAnswer; // Only shown after quiz submission
    private String explanation; // Only shown after quiz submission
    private LocalDateTime createdAt;
    
    public static QuestionResponse fromEntity(Question question) {
        return fromEntity(question, false);
    }
    
    public static QuestionResponse fromEntity(Question question, boolean showAnswers) {
        if (question == null) {
            return null;
        }
        
        QuestionResponse response = QuestionResponse.builder()
            .id(question.getId())
            .content(question.getContent())
            .options(parseOptions(question.getOptions()))
            .createdAt(question.getCreatedAt())
            .build();
        
        if (showAnswers) {
            response.setCorrectAnswer(question.getCorrectAnswer());
            response.setExplanation(question.getExplanation());
        }
        
        return response;
    }
    
    private static List<String> parseOptions(String optionsJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(optionsJson, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
