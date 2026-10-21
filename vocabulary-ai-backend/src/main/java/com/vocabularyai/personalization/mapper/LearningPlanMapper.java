package com.vocabularyai.personalization.mapper;

import com.vocabularyai.personalization.dto.LearningPlanDto;
import com.vocabularyai.personalization.dto.LearningPlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper cho LearningPlanDto → Response DTOs
 * 
 * Chuyển đổi DTOs nội bộ sang định dạng API response
 */
@Mapper(componentModel = "spring")
public interface LearningPlanMapper {
    
    /**
     * Ánh xạ LearningPlanDto sang API response
     */
    @Mapping(target = "data", source = "dto")
    @Mapping(target = "statusCode", constant = "200")
    @Mapping(target = "message", constant = "Kế hoạch được tạo thành công")
    @Mapping(target = "timestamp", expression = "java(System.currentTimeMillis())")
    LearningPlanResponse toResponse(LearningPlanDto dto);
}
