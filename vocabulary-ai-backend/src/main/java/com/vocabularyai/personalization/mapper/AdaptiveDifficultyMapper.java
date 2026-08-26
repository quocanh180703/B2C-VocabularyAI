package com.vocabularyai.personalization.mapper;

import com.vocabularyai.personalization.dto.AdaptiveDifficultyDto;
import com.vocabularyai.personalization.dto.AdaptiveDifficultyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper cho AdaptiveDifficultyDto → Response DTOs
 */
@Mapper(componentModel = "spring")
public interface AdaptiveDifficultyMapper {
    
    /**
     * Ánh xạ AdaptiveDifficultyDto sang API response
     */
    @Mapping(target = "statusCode", constant = "200")
    @Mapping(target = "message", constant = "Phân tích độ khó được tạo thành công")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "fromCache", constant = "false")
    @Mapping(target = "cacheTtlSeconds", constant = "2592000L")
    AdaptiveDifficultyResponse toResponse(AdaptiveDifficultyDto dto);
}
