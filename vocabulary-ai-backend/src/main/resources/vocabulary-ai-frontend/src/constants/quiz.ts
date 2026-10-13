/**
 * Quiz Constants & Enums
 * 
 * Centralized configuration for the Quiz module
 */

import type { QuizLevel, QuizStatus } from '@/types/quiz';

// ============================================
// ENUM VALUES
// ============================================

export const QUIZ_LEVEL = {
  A1: 'A1' as QuizLevel,
  A2: 'A2' as QuizLevel,
  B1: 'B1' as QuizLevel,
  B2: 'B2' as QuizLevel,
  C1: 'C1' as QuizLevel,
  C2: 'C2' as QuizLevel,
} as const;

export const QUIZ_STATUS = {
  DRAFT: 'DRAFT' as QuizStatus,
  PUBLISHED: 'PUBLISHED' as QuizStatus,
  ARCHIVED: 'ARCHIVED' as QuizStatus,
} as const;

// ============================================
// DISPLAY MAPPINGS
// ============================================

/**
 * Quiz level to display name
 */
export const QUIZ_LEVEL_DISPLAY_MAP: Record<string, string> = {
  A1: 'Beginner A1',
  A2: 'Beginner A2',
  B1: 'Intermediate B1',
  B2: 'Intermediate B2',
  C1: 'Advanced C1',
  C2: 'Advanced C2',
};

/**
 * Quiz status to display name
 */
export const QUIZ_STATUS_DISPLAY_MAP: Record<string, string> = {
  DRAFT: 'Draft',
  PUBLISHED: 'Published',
  ARCHIVED: 'Archived',
};

/**
 * Quiz status to badge tone (for UI)
 */
export const QUIZ_STATUS_TONE_MAP: Record<string, 'neutral' | 'primary' | 'success' | 'warning' | 'danger'> = {
  DRAFT: 'neutral',
  PUBLISHED: 'primary',
  ARCHIVED: 'warning',
};

// ============================================
// PAGINATION CONFIG
// ============================================

export const QUIZ_LIST_PAGE_SIZE = 20;
export const QUIZ_HISTORY_PAGE_SIZE = 20;
export const QUIZ_MAX_PAGE_SIZE = 100;

// ============================================
// API CONFIGURATION
// ============================================

export const QUIZ_API_BASE = '/quizzes';

export const QUIZ_ENDPOINTS = {
  LIST: '/quizzes',
  DETAIL: (quizId: number) => `/quizzes/${quizId}`,
  START: (quizId: number) => `/quizzes/${quizId}/start`,
  SUBMIT: (attemptId: number) => `/quizzes/${attemptId}/submit`,
  ATTEMPT_DETAILS: (attemptId: number) => `/quizzes/attempts/${attemptId}`,
  HISTORY: '/quizzes/history',
} as const;

// ============================================
// CACHE & TIMING CONFIGURATION
// ============================================

/**
 * How long to consider data fresh (not stale) in milliseconds
 */
export const QUIZ_STALE_TIME = {
  QUIZ_LIST: 30 * 1000, // 30 seconds
  QUIZ_DETAIL: 60 * 1000, // 1 minute
  ATTEMPT_DETAILS: 5 * 1000, // 5 seconds (frequently updated after review)
  QUIZ_HISTORY: 30 * 1000, // 30 seconds
} as const;

/**
 * How long to keep data in cache after it becomes stale (gcTime / cacheTime)
 */
export const QUIZ_CACHE_TIME = {
  QUIZ_LIST: 5 * 60 * 1000, // 5 minutes
  QUIZ_DETAIL: 5 * 60 * 1000, // 5 minutes
  ATTEMPT_DETAILS: 10 * 60 * 1000, // 10 minutes
  QUIZ_HISTORY: 5 * 60 * 1000, // 5 minutes
} as const;

// ============================================
// TIMER CONFIGURATION
// ============================================

/**
 * Convert seconds to milliseconds for timer
 */
export const TIMER_CONSTANTS = {
  SECOND: 1000,
  MINUTE: 60 * 1000,
  HOUR: 60 * 60 * 1000,
} as const;

/**
 * Warning thresholds for timer
 */
export const TIMER_WARNING = {
  CRITICAL: 60 * 1000, // 1 minute - show red
  WARNING: 5 * 60 * 1000, // 5 minutes - show yellow
} as const;

// ============================================
// UI CONFIGURATION
// ============================================

/**
 * Quiz card configuration
 */
export const QUIZ_CARD_CONFIG = {
  MIN_WIDTH: 280,
  MAX_WIDTH: 400,
  IMAGE_HEIGHT: 140,
} as const;

/**
 * Question card configuration
 */
export const QUESTION_CARD_CONFIG = {
  MIN_OPTIONS: 2,
  MAX_OPTIONS: 4,
  DEFAULT_OPTIONS: 4,
} as const;

/**
 * Quiz result configuration
 */
export const QUIZ_RESULT_CONFIG = {
  PASS_SCORE: 70, // 70% is passing
  EXCELLENT_SCORE: 90,
  GOOD_SCORE: 80,
  AVERAGE_SCORE: 70,
} as const;

// ============================================
// ERROR MESSAGES
// ============================================

export const QUIZ_ERROR_MESSAGES: Record<string, string> = {
  QUIZ_NOT_PUBLISHED: 'This quiz is not available yet. Please check back later.',
  QUIZ_ALREADY_SUBMITTED: 'You have already submitted this quiz. View your results instead.',
  QUIZ_NO_QUESTIONS: 'This quiz has no questions. Please try another quiz.',
  QUIZ_NOT_FOUND: 'Quiz not found. Please try again.',
  ATTEMPT_NOT_FOUND: 'Quiz attempt not found. Please try again.',
  QUESTION_NOT_FOUND: 'Question not found. Please refresh and try again.',
  UNAUTHORIZED: 'You are not authorized to perform this action. Please log in.',
  FORBIDDEN: 'You do not have permission to access this quiz attempt.',
  BAD_REQUEST: 'Invalid request. Please check your input and try again.',
  SERVER_ERROR: 'Server error occurred. Please try again later.',
  NETWORK_ERROR: 'Network error occurred. Please check your connection and try again.',
  SUBMISSION_IN_PROGRESS: 'Your submission is being processed. Please wait...',
  DUPLICATE_SUBMISSION: 'Your submission was already received. Please wait for the result...',
} as const;

// ============================================
// EMPTY STATE MESSAGES
// ============================================

export const QUIZ_EMPTY_MESSAGES = {
  NO_QUIZZES: 'No quizzes available yet.',
  NO_HISTORY: 'You haven\'t taken any quizzes yet. Start your first quiz!',
  NO_QUESTIONS: 'This quiz has no questions.',
} as const;

// ============================================
// LABELS & TITLES
// ============================================

export const QUIZ_LABELS = {
  START_QUIZ: 'Start Quiz',
  SUBMIT_QUIZ: 'Submit Quiz',
  NEXT_QUESTION: 'Next Question',
  PREVIOUS_QUESTION: 'Previous Question',
  REVIEW_ANSWERS: 'Review Answers',
  RETAKE_QUIZ: 'Retake Quiz',
  BACK_TO_QUIZZES: 'Back to Quizzes',
  VIEW_HISTORY: 'View History',
  TRY_ANOTHER: 'Try Another Quiz',
} as const;

export const QUIZ_PAGE_TITLES = {
  QUIZZES: 'Quizzes',
  QUIZ_DETAIL: 'Quiz Details',
  TAKING_QUIZ: 'Taking Quiz',
  QUIZ_RESULT: 'Quiz Result',
  QUIZ_REVIEW: 'Review Answers',
  QUIZ_HISTORY: 'Quiz History',
} as const;

// ============================================
// FORMATTING HELPERS
// ============================================

/**
 * Format time limit (seconds) to readable string
 * @param seconds - Time in seconds
 * @returns Formatted time string (e.g., "10 min", "2h 30min")
 */
export function formatTimeLimit(seconds?: number): string {
  if (!seconds) return 'No limit';

  if (seconds < 60) {
    return `${seconds}s`;
  }

  if (seconds < 3600) {
    const minutes = Math.ceil(seconds / 60);
    return `${minutes}min`;
  }

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.ceil((seconds % 3600) / 60);
  return `${hours}h ${minutes}min`;
}

/**
 * Format time taken (seconds) to readable string
 * @param seconds - Time in seconds
 * @returns Formatted time string (e.g., "5:30", "1:30:45")
 */
export function formatTimeTaken(seconds?: number): string {
  if (!seconds) return '--';

  if (seconds < 60) {
    return `0:${seconds.toString().padStart(2, '0')}`;
  }

  if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  }

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

/**
 * Format accuracy percentage
 * @param correctCount - Number of correct answers
 * @param totalCount - Total number of questions
 * @returns Formatted accuracy (e.g., "80%")
 */
export function formatAccuracy(correctCount: number, totalCount: number): string {
  if (totalCount === 0) return '0%';
  const percentage = Math.round((correctCount / totalCount) * 100);
  return `${percentage}%`;
}

/**
 * Get accuracy feedback message based on score
 * @param score - Score percentage (0-100)
 * @returns Feedback message
 */
export function getAccuracyFeedback(score: number): string {
  if (score >= QUIZ_RESULT_CONFIG.EXCELLENT_SCORE) {
    return 'Excellent! You\'ve mastered this quiz!';
  }
  if (score >= QUIZ_RESULT_CONFIG.GOOD_SCORE) {
    return 'Great job! You did very well!';
  }
  if (score >= QUIZ_RESULT_CONFIG.AVERAGE_SCORE) {
    return 'Good effort! Keep practicing!';
  }
  return 'Keep trying! Review the material and try again.';
}

// ============================================
// VALIDATION
// ============================================

/**
 * Check if quiz level is valid
 */
export function isValidQuizLevel(level: string): level is QuizLevel {
  return Object.values(QUIZ_LEVEL).includes(level as QuizLevel);
}

/**
 * Check if quiz status is valid
 */
export function isValidQuizStatus(status: string): status is QuizStatus {
  return Object.values(QUIZ_STATUS).includes(status as QuizStatus);
}

/**
 * Check if page size is valid
 */
export function isValidPageSize(size: number): boolean {
  return size > 0 && size <= QUIZ_MAX_PAGE_SIZE;
}

/**
 * Check if answer index is valid
 */
export function isValidAnswerIndex(index: number, optionsCount: number): boolean {
  return index >= 0 && index < optionsCount;
}
