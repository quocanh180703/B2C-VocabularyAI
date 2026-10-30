package com.vocabularyai.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSpeakingSessionRequest {

    private String topic;

    @Pattern(regexp = "A1|A2|B1|B2|C1", message = "Level must be A1-C1")
    private String level;

    private String description;

    @Pattern(regexp = "IN_PROGRESS|COMPLETED|CANCELLED", message = "Status must be IN_PROGRESS, COMPLETED, or CANCELLED")
    private String status;

    private Double overallScore;
}
