/**
 * Learning & Flashcard Constants
 * 
 * Centralized configuration for the Learning module
 * Includes enums, SRS intervals, display mappings, and constants
 */

import type {
  LearningStatus,
  ActivityType,
  PartOfSpeech,
  ProficiencyLevel,
  StatusDisplay,
  ActivityTypeDisplay,
  SRSInterval,
  MasteryCriteria,
} from '@/types/learning';

// ============================================
// Enum Values
// ============================================

/**
 * Learning Status Enum Values
 * Represents the current state of a word in learning journey
 */
export const LEARNING_STATUS = {
  NEW: 'NEW' as LearningStatus, // Never learned / never reviewed
  LEARNING: 'LEARNING' as LearningStatus, // Currently learning
  MASTERED: 'MASTERED' as LearningStatus, // Fully mastered
} as const;

/**
 * Activity Type Enum Values
 * Types of learning activities recorded in history
 */
export const ACTIVITY_TYPE = {
  LEARN: 'LEARN' as ActivityType, // First time learning a word
  REVIEW: 'REVIEW' as ActivityType, // Review session
  QUIZ: 'QUIZ' as ActivityType, // Quiz activity (Phase 9+)
  SPEAKING: 'SPEAKING' as ActivityType, // Speaking practice (Phase 10+)
} as const;

/**
 * Part of Speech Enum Values
 */
export const PART_OF_SPEECH = {
  NOUN: 'NOUN' as PartOfSpeech,
  VERB: 'VERB' as PartOfSpeech,
  ADJECTIVE: 'ADJECTIVE' as PartOfSpeech,
  ADVERB: 'ADVERB' as PartOfSpeech,
  PREPOSITION: 'PREPOSITION' as PartOfSpeech,
} as const;

/**
 * CEFR Proficiency Levels
 */
export const PROFICIENCY_LEVEL = {
  A1: 'A1' as ProficiencyLevel,
  A2: 'A2' as ProficiencyLevel,
  B1: 'B1' as ProficiencyLevel,
  B2: 'B2' as ProficiencyLevel,
  C1: 'C1' as ProficiencyLevel,
  C2: 'C2' as ProficiencyLevel,
} as const;

// ============================================
// SRS (Spaced Repetition System) Configuration
// ============================================

/**
 * SRS Intervals
 * 
 * Defines the review schedule based on review count
 * Backend implements this algorithm automatically
 * 
 * Interval Sequence:
 * - 1st review (after 0 attempts): +1 day
 * - 2nd review (after 1 attempt): +3 days
 * - 3rd review (after 2 attempts): +7 days
 * - 4th review (after 3 attempts): +14 days
 * - 5th+ review (after 4+ attempts): +30 days
 * - After mastery: nextReviewAt = null (no more reviews)
 */
export const SRS_INTERVALS: SRSInterval[] = [
  {
    reviewCount: 0,
    daysUntilNextReview: 1,
    description: 'First review tomorrow',
  },
  {
    reviewCount: 1,
    daysUntilNextReview: 3,
    description: 'Review in 3 days',
  },
  {
    reviewCount: 2,
    daysUntilNextReview: 7,
    description: 'Review in 1 week',
  },
  {
    reviewCount: 3,
    daysUntilNextReview: 14,
    description: 'Review in 2 weeks',
  },
  {
    reviewCount: 4,
    daysUntilNextReview: 30,
    description: 'Review in 1 month',
  },
];

/**
 * Get SRS interval for review count
 * @param reviewCount - Number of reviews completed
 * @returns SRSInterval configuration
 */
export function getSRSInterval(reviewCount: number): SRSInterval {
  if (reviewCount >= 4) {
    return SRS_INTERVALS[4]; // 30 days for all subsequent reviews
  }
  return SRS_INTERVALS[reviewCount] || SRS_INTERVALS[0];
}

// ============================================
// Mastery Configuration
// ============================================

/**
 * Mastery Criteria
 * 
 * Conditions required for a word to be marked as MASTERED:
 * - Minimum Accuracy: 80% (80 correct out of 100 attempts)
 * - Minimum Review Count: 5 (at least 5 review attempts)
 * 
 * Backend checks: accuracy >= 80% AND reviewCount >= 5
 * When met, status transitions from LEARNING to MASTERED
 * and nextReviewAt is set to null (no more reviews)
 */
export const MASTERY_CRITERIA: MasteryCriteria = {
  minAccuracy: 80, // 80%
  minReviewCount: 5, // 5 reviews minimum
};

/**
 * Check if a word meets mastery criteria
 * @param accuracy - Accuracy percentage (0-100)
 * @param reviewCount - Number of reviews completed
 * @returns true if word should be mastered
 */
export function isMastered(accuracy: number, reviewCount: number): boolean {
  return accuracy >= MASTERY_CRITERIA.minAccuracy && reviewCount >= MASTERY_CRITERIA.minReviewCount;
}

// ============================================
// Display Mappings
// ============================================

/**
 * Learning Status Display Configuration
 * Maps status values to display text, colors, and icons
 */
export const STATUS_DISPLAY_MAP: Record<LearningStatus, StatusDisplay> = {
  NEW: {
    status: 'NEW',
    label: 'Not Started',
    color: 'bg-gray-100 text-gray-800',
    icon: '📚',
  },
  LEARNING: {
    status: 'LEARNING',
    label: 'Learning',
    color: 'bg-blue-100 text-blue-800',
    icon: '🎓',
  },
  MASTERED: {
    status: 'MASTERED',
    label: 'Mastered',
    color: 'bg-green-100 text-green-800',
    icon: '⭐',
  },
};

/**
 * Activity Type Display Configuration
 * Maps activity types to display labels and icons
 */
export const ACTIVITY_TYPE_DISPLAY_MAP: Record<ActivityType, ActivityTypeDisplay> = {
  LEARN: {
    type: 'LEARN',
    label: 'Started Learning',
    icon: '📖',
  },
  REVIEW: {
    type: 'REVIEW',
    label: 'Review',
    icon: '🔄',
  },
  QUIZ: {
    type: 'QUIZ',
    label: 'Quiz',
    icon: '❓',
  },
  SPEAKING: {
    type: 'SPEAKING',
    label: 'Speaking Practice',
    icon: '🎤',
  },
};

/**
 * Part of Speech Display Map
 */
export const PART_OF_SPEECH_DISPLAY_MAP: Record<PartOfSpeech, string> = {
  NOUN: 'Noun',
  VERB: 'Verb',
  ADJECTIVE: 'Adjective',
  ADVERB: 'Adverb',
  PREPOSITION: 'Preposition',
};

/**
 * Proficiency Level Display Map
 */
export const PROFICIENCY_LEVEL_DISPLAY_MAP: Record<ProficiencyLevel, string> = {
  A1: 'Beginner',
  A2: 'Elementary',
  B1: 'Intermediate',
  B2: 'Upper Intermediate',
  C1: 'Advanced',
  C2: 'Mastery',
};

// ============================================
// Pagination Configuration
// ============================================

/**
 * Default page size for paginated queries
 */
export const DEFAULT_PAGE_SIZE = 20;

/**
 * Maximum page size allowed
 */
export const MAX_PAGE_SIZE = 100;

/**
 * Initial page number (0-indexed)
 */
export const INITIAL_PAGE = 0;

// ============================================
// API Configuration
// ============================================

/**
 * Learning API Base Path
 */
export const LEARNING_API_BASE = '/learning';

/**
 * API Endpoints
 */
export const LEARNING_ENDPOINTS = {
  START_WORD: `${LEARNING_API_BASE}/words/:wordId/start`,
  REVIEW_WORD: `${LEARNING_API_BASE}/words/:wordId/review`,
  GET_PROGRESS: `${LEARNING_API_BASE}/progress`,
  GET_REVIEW_QUEUE: `${LEARNING_API_BASE}/review-queue`,
  GET_HISTORY: `${LEARNING_API_BASE}/history`,
  GET_STATISTICS: `${LEARNING_API_BASE}/statistics`,
} as const;

// ============================================
// Query Configuration
// ============================================

/**
 * React Query stale times (in milliseconds)
 */
export const STALE_TIME = {
  PROGRESS: 30 * 1000, // 30 seconds
  REVIEW_QUEUE: 30 * 1000, // 30 seconds
  HISTORY: 30 * 1000, // 30 seconds
  STATISTICS: 60 * 1000, // 60 seconds (computed on-demand, higher stale time)
} as const;

/**
 * React Query cache times (in milliseconds)
 * How long to keep cached data before garbage collection
 */
export const CACHE_TIME = {
  PROGRESS: 5 * 60 * 1000, // 5 minutes
  REVIEW_QUEUE: 5 * 60 * 1000, // 5 minutes
  HISTORY: 5 * 60 * 1000, // 5 minutes
  STATISTICS: 10 * 60 * 1000, // 10 minutes
} as const;

// ============================================
// Error Messages
// ============================================

/**
 * Learning Error Messages
 * User-friendly error messages for different scenarios
 */
export const LEARNING_ERROR_MESSAGES: Record<string, string> = {
  WORD_NOT_FOUND: 'Word not found. It may have been deleted.',
  ALREADY_LEARNING: 'You are already learning this word.',
  NOT_LEARNING_YET: 'Please start learning this word first.',
  UNAUTHORIZED: 'Please log in to continue.',
  FORBIDDEN: 'You do not have permission to perform this action.',
  SERVER_ERROR: 'Server error. Please try again later.',
  DUPLICATE_SUBMISSION: 'Please wait. Your review is being submitted.',
  NETWORK_ERROR: 'Network error. Please check your connection.',
  UNKNOWN: 'An unexpected error occurred. Please try again.',
};

// ============================================
// UI Configuration
// ============================================

/**
 * Learning Card Configuration
 */
export const LEARNING_CARD_CONFIG = {
  ACCURACY_THRESHOLD_WARNING: 50, // Show warning below 50%
  ACCURACY_THRESHOLD_SUCCESS: 80, // Show success at 80%+
  SHOW_PROGRESS_BAR: true,
  SHOW_ACCURACY: true,
  SHOW_NEXT_REVIEW: true,
  SHOW_ACTIVITY_COUNT: true,
} as const;

/**
 * Review Form Configuration
 */
export const REVIEW_FORM_CONFIG = {
  DISABLE_BUTTON_ON_SUBMIT: true, // Prevent duplicate submissions
  SHOW_LOADING_STATE: true,
  SHOW_SUCCESS_MESSAGE: true,
  SUCCESS_MESSAGE_DURATION: 2000, // 2 seconds
  SHOW_NEXT_REVIEW_DATE: true,
  SHOW_SRS_EXPLANATION: true,
} as const;

/**
 * Statistics Display Configuration
 */
export const STATISTICS_CONFIG = {
  SHOW_PROGRESS_RING: true,
  SHOW_LEARNING_PERCENTAGE: true,
  SHOW_MASTERY_PERCENTAGE: true,
  SHOW_ACTIVITY_COUNT: true,
  SHOW_ACCURACY_RATE: true,
  SHOW_WORDS_NEEDING_REVIEW: true,
  REFRESH_INTERVAL: 60 * 1000, // 60 seconds
} as const;

/**
 * Skeleton Loading Configuration
 */
export const SKELETON_CONFIG = {
  ANIMATION_DURATION: 1000, // 1 second
  SHOW_PULSE_EFFECT: true,
} as const;

// ============================================
// UI Labels & Text
// ============================================

/**
 * Button Labels
 */
export const BUTTON_LABELS = {
  I_GOT_IT_RIGHT: 'I got it right ✓',
  I_GOT_IT_WRONG: 'I got it wrong ✗',
  START_LEARNING: 'Start Learning',
  REVIEW: 'Review',
  LOAD_MORE: 'Load More',
  REFRESH: 'Refresh',
  SUBMIT: 'Submit',
  CANCEL: 'Cancel',
} as const;

/**
 * Page Titles & Headings
 */
export const PAGE_TITLES = {
  LEARNING: 'Learning',
  REVIEW_QUEUE: 'Words to Review',
  LEARNING_HISTORY: 'Learning History',
  STATISTICS: 'Statistics',
} as const;

/**
 * Empty State Messages
 */
export const EMPTY_MESSAGES = {
  NO_WORDS_LEARNING: 'Start learning new words to see them here.',
  NO_WORDS_TO_REVIEW: 'Great job! No words need review right now.',
  NO_HISTORY: 'No learning history yet. Start learning words to see activity here.',
  NO_FAVORITES: 'No favorite words yet. Mark words as favorites to see them here.',
} as const;

/**
 * No Data / Empty State Messages
 */
export const NO_DATA_MESSAGES = {
  STATISTICS: 'Start learning to see your statistics.',
  PROGRESS: 'No learning progress yet.',
  REVIEW_QUEUE: 'No words need review.',
  HISTORY: 'No learning history.',
} as const;

// ============================================
// Format Helpers
// ============================================

/**
 * Format accuracy percentage for display
 * @param accuracy - Accuracy value (0-100)
 * @returns Formatted string like "85.5%"
 */
export function formatAccuracy(accuracy: number): string {
  return `${accuracy.toFixed(1)}%`;
}

/**
 * Format review count for display
 * @param correctCount - Number of correct reviews
 * @param wrongCount - Number of wrong reviews
 * @returns Formatted string like "3/5"
 */
export function formatReviewCount(correctCount: number, wrongCount: number): string {
  const total = correctCount + wrongCount;
  return `${correctCount}/${total}`;
}

/**
 * Format next review date relative to today
 * @param nextReviewAt - ISO datetime string or undefined
 * @returns Formatted string like "Tomorrow" or "3 days" or "Mastered"
 */
export function formatNextReviewDate(nextReviewAt?: string): string {
  if (!nextReviewAt) {
    return '✓ Mastered';
  }

  const now = new Date();
  const reviewDate = new Date(nextReviewAt);
  const daysUntil = Math.ceil((reviewDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));

  if (daysUntil === 0) {
    return 'Today';
  }
  if (daysUntil === 1) {
    return 'Tomorrow';
  }
  if (daysUntil < 0) {
    return 'Overdue';
  }
  if (daysUntil === 7) {
    return '1 week';
  }
  if (daysUntil === 14) {
    return '2 weeks';
  }
  if (daysUntil === 30) {
    return '1 month';
  }
  return `${daysUntil} days`;
}

/**
 * Format total minutes to readable duration
 * @param minutes - Number of minutes
 * @returns Formatted string like "2h 30m" or "45m"
 */
export function formatDuration(minutes: number): string {
  if (minutes < 60) {
    return `${minutes}m`;
  }
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  if (remainingMinutes === 0) {
    return `${hours}h`;
  }
  return `${hours}h ${remainingMinutes}m`;
}

// ============================================
// Validation Helpers
// ============================================

/**
 * Validate learning status value
 * @param status - Value to validate
 * @returns true if valid LearningStatus
 */
export function isValidLearningStatus(status: any): status is LearningStatus {
  return Object.values(LEARNING_STATUS).includes(status);
}

/**
 * Validate activity type value
 * @param type - Value to validate
 * @returns true if valid ActivityType
 */
export function isValidActivityType(type: any): type is ActivityType {
  return Object.values(ACTIVITY_TYPE).includes(type);
}

// ============================================
// Array Constants
// ============================================

/**
 * All learning status values
 */
export const ALL_LEARNING_STATUSES: LearningStatus[] = [
  LEARNING_STATUS.NEW,
  LEARNING_STATUS.LEARNING,
  LEARNING_STATUS.MASTERED,
];

/**
 * All activity type values
 */
export const ALL_ACTIVITY_TYPES: ActivityType[] = [
  ACTIVITY_TYPE.LEARN,
  ACTIVITY_TYPE.REVIEW,
  ACTIVITY_TYPE.QUIZ,
  ACTIVITY_TYPE.SPEAKING,
];

/**
 * All proficiency levels
 */
export const ALL_PROFICIENCY_LEVELS: ProficiencyLevel[] = [
  PROFICIENCY_LEVEL.A1,
  PROFICIENCY_LEVEL.A2,
  PROFICIENCY_LEVEL.B1,
  PROFICIENCY_LEVEL.B2,
  PROFICIENCY_LEVEL.C1,
  PROFICIENCY_LEVEL.C2,
];

/**
 * All part of speech values
 */
export const ALL_PARTS_OF_SPEECH: PartOfSpeech[] = [
  PART_OF_SPEECH.NOUN,
  PART_OF_SPEECH.VERB,
  PART_OF_SPEECH.ADJECTIVE,
  PART_OF_SPEECH.ADVERB,
  PART_OF_SPEECH.PREPOSITION,
];
