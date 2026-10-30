package com.vocabularyai.placement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PlacementQuestionResponse DTO
 * Returns question details with answers stripped for test integrity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementQuestionResponse {
    private Long id;
    private String level; // A1, A2, B1, B2, C1
    private String content;
    private List<String> options;
}
