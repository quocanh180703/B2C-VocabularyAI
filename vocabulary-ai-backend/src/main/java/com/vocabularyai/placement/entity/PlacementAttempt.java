package com.vocabularyai.placement.entity;

import com.vocabularyai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * PlacementAttempt Entity
 * Stores user entrance level test attempts and results.
 */
@Entity
@Table(name = "placement_attempts", indexes = {
    @Index(name = "idx_placement_user_completed", columnList = "user_id, completed_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "level_result", nullable = false, length = 5)
    private String levelResult; // A1, A2, B1, B2, C1

    @Column(name = "score_by_level", nullable = false, length = 255)
    private String scoreByLevel; // JSON format e.g. {"A1":4,"A2":3,"B1":2,"B2":1,"C1":0}

    @Column(name = "completed_at", nullable = false)
    @Builder.Default
    private LocalDateTime completedAt = LocalDateTime.now();
}
