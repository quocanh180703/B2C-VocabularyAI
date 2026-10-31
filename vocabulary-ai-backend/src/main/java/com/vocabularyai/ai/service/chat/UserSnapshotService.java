package com.vocabularyai.ai.service.chat;

import com.vocabularyai.ai.dto.UserSnapshotDto;

/**
 * Service for building real-time, verified user learning and account snapshots.
 */
public interface UserSnapshotService {

    /**
     * Build real-time factual learning and account snapshot for a user.
     * Guaranteed strictly read-only and non-mutating.
     *
     * @param userId Authenticated user ID
     * @return UserSnapshotDto containing aggregate statistics
     */
    UserSnapshotDto buildSnapshot(Long userId);
}
