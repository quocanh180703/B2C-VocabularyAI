package com.vocabularyai.vocabulary.custom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomWordRequest {

    @NotBlank(message = "Nghĩa tiếng Việt không được để trống")
    @Size(max = 500, message = "Nghĩa tiếng Việt tối đa 500 ký tự")
    private String vietnameseMeaning;

    @Size(max = 50, message = "Từ loại tối đa 50 ký tự")
    private String partOfSpeech;

    private String example;
    private String exampleTranslation;

    @NotBlank(message = "Cấp độ CEFR không được để trống")
    private String cefrLevel;

    @NotBlank(message = "Chủ đề không được để trống")
    private String topic;
}
