package com.vocabularyai.ai.mapper;

import com.vocabularyai.ai.dto.response.ExplanationResponse;
import com.vocabularyai.vocabulary.entity.Word;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Word entity to ExplanationResponse DTO
 */
@Mapper(componentModel = "spring")
public interface ExplanationMapper {
    
    /**
     * Map Word entity to ExplanationResponse DTO
     * Note: The explanation field needs to be populated from AI service separately
     */
    @Mapping(target = "wordId", source = "id")
    @Mapping(target = "word", source = "word")
    @Mapping(target = "partOfSpeech", source = "partOfSpeech")
    @Mapping(target = "explanation", ignore = true) // Set by service
    @Mapping(target = "examples", ignore = true) // Set by service
    @Mapping(target = "etymology", ignore = true) // Set by service
    @Mapping(target = "relatedWords", ignore = true) // Set by service
    ExplanationResponse fromWordEntity(Word word);
}
