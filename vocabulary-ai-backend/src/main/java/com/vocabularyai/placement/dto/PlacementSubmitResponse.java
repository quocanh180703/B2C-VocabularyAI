package com.vocabularyai.placement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementSubmitResponse {
    private String level; // A1, A2, B1, B2, C1
    private Map<String, Integer> scoreByLevel; // e.g. {"A1": 4, "A2": 3, "B1": 2, "B2": 1, "C1": 0}
    private String feedback; // Feedback message from Vi
}
