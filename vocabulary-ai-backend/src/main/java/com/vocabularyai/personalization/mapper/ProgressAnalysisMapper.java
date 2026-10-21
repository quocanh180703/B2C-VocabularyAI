package com.vocabularyai.personalization.mapper;

import com.vocabularyai.personalization.dto.ProgressAnalysisDto;
import com.vocabularyai.personalization.dto.ProgressAnalysisResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper cho ProgressAnalysisDto → Response DTOs
 */
@Mapper(componentModel = "spring")
public interface ProgressAnalysisMapper {
    
    /**
     * Ánh xạ ProgressAnalysisDto sang API response
     */
    @Mapping(target = "data", source = "dto")
    @Mapping(target = "statusCode", constant = "200")
    @Mapping(target = "message", constant = "Phân tích tiến độ được tạo thành công")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "fromCache", constant = "false")
    @Mapping(target = "cacheTtlSeconds", constant = "86400L")
    ProgressAnalysisResponse toResponse(ProgressAnalysisDto dto);
}
