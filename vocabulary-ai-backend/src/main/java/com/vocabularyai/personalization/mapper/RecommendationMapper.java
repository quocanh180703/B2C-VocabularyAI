package com.vocabularyai.personalization.mapper;

import com.vocabularyai.personalization.dto.RecommendationDto;
import com.vocabularyai.personalization.dto.RecommendationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper cho RecommendationDto → Response DTOs
 * 
 * Chuyển đổi DTOs nội bộ sang định dạng API response
 */
@Mapper(componentModel = "spring")
public interface RecommendationMapper {
    
    /**
     * Ánh xạ RecommendationDto sang API response
     */
    @Mapping(target = "statusCode", constant = "200")
    @Mapping(target = "message", constant = "Gợi ý được tạo thành công")
    @Mapping(target = "timestamp", expression = "java(System.currentTimeMillis())")
    RecommendationResponse toResponse(RecommendationDto dto);
}
