package com.vocabularyai.quiz.entity;

/**
 * Visibility status for a Quiz:
 * - PUBLIC: Visible to everyone, published in public quiz list
 * - PRIVATE: Visible only to creator
 * - PENDING: Submitted for instructor approval
 * - REJECTED: Rejected by instructor, editable and resubmittable by creator
 */
public enum QuizVisibility {
    PUBLIC,
    PRIVATE,
    PENDING,
    REJECTED
}
