package com.vocabularyai.placement.service;

import com.vocabularyai.placement.dto.PlacementQuestionResponse;
import com.vocabularyai.placement.dto.PlacementStatusResponse;
import com.vocabularyai.placement.dto.PlacementSubmitRequest;
import com.vocabularyai.placement.dto.PlacementSubmitResponse;
import com.vocabularyai.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface PlacementService {
    PlacementStatusResponse getStatus(Long userId);
    List<PlacementQuestionResponse> generateTest();
    PlacementSubmitResponse submitTest(User user, PlacementSubmitRequest request);
    Optional<String> getLatestPlacementLevel(Long userId);
}
