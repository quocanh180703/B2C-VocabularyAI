package com.vocabularyai.personalization.mapper;

import com.vocabularyai.personalization.dto.DailyPracticeDto;
import com.vocabularyai.personalization.dto.DailyPracticeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper cho DailyPracticeDto → Response DTOs
 */
@Mapper(componentModel = "spring")
public interface DailyPracticeMapper {
    
    /**
     * Ánh xạ DailyPracticeDto sang API response
     */
    @Mapping(target = "statusCode", constant = "200")
    @Mapping(target = "message", constant = "Hoạt động hôm nay được tạo thành công")
    @Mapping(target = "timestamp", expression = "java(System.currentTimeMillis())")
    DailyPracticeResponse toResponse(DailyPracticeDto dto);
}
