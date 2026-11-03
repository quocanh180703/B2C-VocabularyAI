package com.vocabularyai.listening.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordMatchResult {
    private String originalWord;
    private String userWord;
    private String status; // CORRECT, NEAR_MISS, INCORRECT, MISSING, EXTRA
}

