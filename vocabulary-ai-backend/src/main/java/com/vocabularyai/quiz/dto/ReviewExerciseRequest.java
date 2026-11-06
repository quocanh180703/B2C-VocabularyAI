package com.vocabularyai.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewExerciseRequest {

    @NotBlank(message = "Quyết định không được để trống")
    @Pattern(regexp = "APPROVED|REJECTED", message = "Quyết định phải là APPROVED hoặc REJECTED")
    private String decision;

    private String note;
}

