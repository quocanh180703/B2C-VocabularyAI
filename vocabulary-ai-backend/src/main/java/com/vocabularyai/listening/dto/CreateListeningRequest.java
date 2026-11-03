package com.vocabularyai.listening.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateListeningRequest {

    @NotBlank(message = "Chủ đề không được để trống")
    private String topic;

    private String level; // A1, A2, B1, B2, C1

    private String voice; // alloy, echo, fable, onyx, nova, coral
}

