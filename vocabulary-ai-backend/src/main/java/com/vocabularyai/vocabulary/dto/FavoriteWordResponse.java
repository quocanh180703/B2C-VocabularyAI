package com.vocabularyai.vocabulary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.vocabulary.entity.FavoriteWord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Favorite Word Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavoriteWordResponse {
    
    private Long id;
    private WordResponse word;
    private LocalDateTime createdAt;
    
    public static FavoriteWordResponse fromEntity(FavoriteWord favoriteWord) {
        if (favoriteWord == null) {
            return null;
        }
        return FavoriteWordResponse.builder()
            .id(favoriteWord.getId())
            .word(WordResponse.fromEntity(favoriteWord.getWord()))
            .createdAt(favoriteWord.getCreatedAt())
            .build();
    }
}
