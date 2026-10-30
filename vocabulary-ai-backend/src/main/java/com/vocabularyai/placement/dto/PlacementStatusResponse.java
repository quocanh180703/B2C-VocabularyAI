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
public class PlacementStatusResponse {
    private boolean completed;
    private String placementLevel; // A1, A2, B1, B2, C1 or null
    private int attemptCount;
    private Map<String, Integer> scoreByLevel; // Optional breakdown
}
