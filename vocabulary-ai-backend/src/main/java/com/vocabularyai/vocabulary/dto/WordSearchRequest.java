package com.vocabularyai.vocabulary.dto;

import com.vocabularyai.vocabulary.entity.Word;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Word Search Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordSearchRequest {
    
    private String keyword;
    private Long categoryId;
    private Word.WordLevel level;
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
}
