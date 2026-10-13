package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeakingSessionDetailResponse {
    
    private Long id;
    private Long userId;
    private String topic;
    private String level;
    private String description;
    private String status;
    private Double overallScore;
    private Integer attemptCount;
    private String startedAt;
    private String completedAt;
    private String createdAt;
    private String updatedAt;
}
