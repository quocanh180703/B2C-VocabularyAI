package com.vocabularyai.personalization.mapper;

import com.vocabularyai.personalization.dto.WeaknessStrengthDto;
import com.vocabularyai.personalization.dto.WeaknessStrengthResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for WeaknessStrengthDto to Response DTOs
 * 
 * Converts internal DTOs to API response format
 */
@Mapper(componentModel = "spring")
public interface WeaknessStrengthMapper {
    
    /**
     * Map WeaknessStrengthDto to API response
     */
    @Mapping(target = "data", source = "dto")
    @Mapping(target = "statusCode", constant = "200")
    @Mapping(target = "message", constant = "Analysis completed successfully")
    @Mapping(target = "timestamp", expression = "java(System.currentTimeMillis())")
    WeaknessStrengthResponse toResponse(WeaknessStrengthDto dto);
}
