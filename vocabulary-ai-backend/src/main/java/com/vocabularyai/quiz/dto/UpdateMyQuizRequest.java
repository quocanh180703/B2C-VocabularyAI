package com.vocabularyai.quiz.dto;

import com.vocabularyai.quiz.entity.Quiz;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMyQuizRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 2, max = 255, message = "Tiêu đề phải từ 2 đến 255 ký tự")
    private String title;

    private String description;

    private Quiz.QuizLevel level;

    private Integer timeLimit; // In seconds or minutes

    private List<QuestionDto> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDto {
        private Long id;
        @NotBlank(message = "Nội dung câu hỏi không được để trống")
        private String content;
        private List<String> options;
        private Integer correctAnswer;
        private String explanation;
    }
}
