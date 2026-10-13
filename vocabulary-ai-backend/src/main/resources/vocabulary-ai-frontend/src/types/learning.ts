/**
 * Learning & Flashcard TypeScript Types
 * 
 * Comprehensive type definitions for the Learning module
 * Includes DTOs, responses, requests, and hook return types
 */

// ============================================
// Status & Enum Types
// ============================================

/**
 * Learning Status Enum
 * Represents the current status of a word in user's learning journey
 */
export type LearningStatus = 'NEW' | 'LEARNING' | 'MASTERED';

/**
 * Activity Type Enum
 * Represents the type of learning activity recorded in history
 */
export type ActivityType = 'LEARN' | 'REVIEW' | 'QUIZ' | 'SPEAKING';

/**
 * Part of Speech Enum
 */
export type PartOfSpeech = 'NOUN' | 'VERB' | 'ADJECTIVE' | 'ADVERB' | 'PREPOSITION';

/**
 * Proficiency Level Enum (CEFR)
 */
export type ProficiencyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

// ============================================
// Word Types
// ============================================

/**
 * Word Object (embedded in responses)
 * Contains vocabulary word details
 */
export interface Word {
  id: number;
  english: string;
  vietnamese: string;
  pronunciation: string;
  partOfSpeech: PartOfSpeech;
  definition: string;
  exampleSentence?: string;
  imageUrl?: string;
  audioUrl?: string;
  topicId?: number;
  proficiencyLevel?: ProficiencyLevel;
  createdAt: string; // ISO datetime
  updatedAt: string; // ISO datetime
}

// ============================================
// Learning Progress Types
// ============================================

/**
 * Learning Progress Response DTO
 * 
 * Represents a user's learning progress on a single word
 * Includes:
 * - Word details
 * - Learning status (NEW/LEARNING/MASTERED)
 * - Review counts and accuracy
 * - SRS next review date
 * 
 * Backend is source of truth - all calculations (accuracy, nextReviewAt, status)
 * are computed by backend, NOT frontend
 */
export interface LearningProgressResponse {
  id: number;
  word: Word;
  status: LearningStatus;
  correctCount: number; // Number of correct reviews
  wrongCount: number; // Number of incorrect reviews
  reviewCount: number; // Total review attempts
  accuracy: number; // 0-100, calculated as (correctCount / (correctCount + wrongCount)) * 100
  needsReview: boolean; // true if nextReviewAt <= now
  lastReviewedAt?: string; // ISO datetime, null if never reviewed
  nextReviewAt?: string; // ISO datetime, null when status=MASTERED
  createdAt: string; // ISO datetime when learning started
  updatedAt: string; // ISO datetime of last update
}

// ============================================
// Learning History Types
// ============================================

/**
 * Learning History Response DTO
 * 
 * Represents a single learning activity entry in audit log
 * Records LEARN, REVIEW, QUIZ, and SPEAKING activities
 */
export interface LearningHistoryResponse {
  id: number;
  word: Word;
  activityType: ActivityType;
  // LEARN: isCorrect = null (not applicable)
  // REVIEW: isCorrect = true/false (user answered correctly or incorrectly)
  // QUIZ: isCorrect = true/false (quiz question answered)
  // SPEAKING: isCorrect = null/true/false (speaking practice)
  isCorrect?: boolean;
  createdAt: string; // ISO datetime
}

// ============================================
// Statistics Types
// ============================================

/**
 * Progress Statistics Response DTO
 * 
 * Summary statistics about user's learning progress
 * Backend-computed (not cached, computed on-demand)
 * 
 * All calculations are performed by backend:
 * - learningPercentage = ((learningWords + masteredWords) / totalWords) * 100
 * - masteryPercentage = (masteredWords / totalWords) * 100
 * - overallAccuracyRate = (correctAnswers / totalActivities) * 100
 * 
 * Frontend should display these values as-is, NOT recalculate
 */
export interface ProgressStatisticsResponse {
  totalWords: number; // Total unique words started learning
  newWords: number; // Count of words with status='NEW'
  learningWords: number; // Count of words with status='LEARNING'
  masteredWords: number; // Count of words with status='MASTERED'
  wordsNeedingReview: number; // Count of words where nextReviewAt <= now
  totalActivities: number; // Total count of LEARN/REVIEW/QUIZ/SPEAKING records
  correctAnswers: number; // Count of activities where isCorrect=true
  overallAccuracyRate: number; // Percentage: (correctAnswers / totalActivities) * 100
  learningPercentage: number; // Percentage: ((learningWords + masteredWords) / totalWords) * 100
  masteryPercentage: number; // Percentage: (masteredWords / totalWords) * 100
}

// ============================================
// Request DTOs
// ============================================

/**
 * Review Word Request DTO
 * 
 * Request body for POST /learning/words/{wordId}/review
 * Simple boolean flag indicating if user answered correctly
 * 
 * CRITICAL: Backend has no rate limiting
 * Frontend MUST prevent duplicate submissions:
 * - Disable button on click
 * - Implement mutation lock
 * - Only allow 1 request per action
 */
export interface ReviewWordRequest {
  correct: boolean; // true if user answered correctly
}

// ============================================
// Pagination Types
// ============================================

/**
 * Paginated Response Wrapper
 * 
 * Generic wrapper for paginated API responses
 * Used for progress, review-queue, and history endpoints
 */
export interface PaginatedResponse<T> {
  content: T[]; // Array of items for current page
  totalElements: number; // Total count of all items
  totalPages: number; // Total number of pages
  currentPage: number; // Current page number (0-indexed)
  pageSize: number; // Items per page
  hasNext?: boolean; // true if more pages exist
  hasPrevious?: boolean; // true if previous pages exist
  empty?: boolean; // true if no items
}

// ============================================
// React Hook Types
// ============================================

/**
 * Learning Hook Return Type
 * 
 * Complete interface for useLearning hook
 * Includes queries, mutations, pagination, and state management
 */
export interface UseLearningReturn {
  // ==================== Query States ====================

  /**
   * Learning Progress Query State
   * Current page of user's learning progress
   * Sorted by createdAt descending (newest first)
   */
  progress: LearningProgressResponse[];
  progressLoading: boolean;
  progressError?: Error;

  /**
   * Review Queue Query State
   * Words currently due for review (SRS queue)
   * Sorted by nextReviewAt ascending (due soonest first)
   */
  reviewQueue: LearningProgressResponse[];
  reviewQueueLoading: boolean;
  reviewQueueError?: Error;

  /**
   * Learning History Query State
   * Activity log of all learning activities
   * Sorted by createdAt descending (newest first)
   */
  history: LearningHistoryResponse[];
  historyLoading: boolean;
  historyError?: Error;

  /**
   * Statistics Query State
   * Summary statistics about user's learning progress
   * Single object (not paginated)
   */
  statistics: ProgressStatisticsResponse | null;
  statsLoading: boolean;
  statsError?: Error;

  // ==================== Mutations ====================

  /**
   * Start Learning Mutation
   * Initiates learning for a new word
   * Returns LearningProgressResponse with status='LEARNING' and nextReviewAt=+1 day
   * 
   * Side effects:
   * - Invalidates progress query
   * - Invalidates statistics query
   * 
   * @param wordId - ID of word to start learning
   * @returns Promise<LearningProgressResponse>
   * @throws Error if already learning or word not found
   */
  startLearning: (wordId: number) => Promise<LearningProgressResponse>;

  /**
   * Review Word Mutation
   * Records a review attempt (correct or incorrect)
   * Automatically updates SRS schedule and status
   * 
   * CRITICAL: Must prevent duplicate submissions
   * - Implement button disable during mutation
   * - Use mutation lock
   * - Verify only 1 API request via network tab
   * 
   * Side effects:
   * - Invalidates progress query (refetch on next use)
   * - Invalidates reviewQueue query (refetch on next use)
   * - Invalidates statistics query (refetch on next use)
   * 
   * @param wordId - ID of word being reviewed
   * @param isCorrect - true if user answered correctly
   * @returns Promise<LearningProgressResponse> - Updated progress with new accuracy/nextReviewAt
   * @throws Error if not learning this word
   */
  reviewWord: (wordId: number, isCorrect: boolean) => Promise<LearningProgressResponse>;

  // ==================== Pagination Helpers ====================

  /**
   * Load more progress items
   * Increments progress page and fetches next batch
   * Results appended to existing progress array
   */
  loadMoreProgress: () => void;

  /**
   * Load more review queue items
   * Increments reviewQueue page and fetches next batch
   * Results appended to existing reviewQueue array
   */
  loadMoreReviewQueue: () => void;

  /**
   * Load more history items
   * Increments history page and fetches next batch
   * Results appended to existing history array
   */
  loadMoreHistory: () => void;

  // ==================== State Management ====================

  /**
   * Manual refetch all queries
   * Invalidates all caches and refetches from backend
   * Useful for manual refresh button
   * 
   * @returns Promise<void>
   */
  refetch: () => Promise<void>;

  // ==================== Pagination State (optional) ====================

  /**
   * Current page number for progress query (0-indexed)
   */
  progressPage?: number;

  /**
   * Current page number for review queue query (0-indexed)
   */
  reviewQueuePage?: number;

  /**
   * Current page number for history query (0-indexed)
   */
  historyPage?: number;

  /**
   * Total pages available for progress
   */
  progressTotalPages?: number;

  /**
   * Total pages available for review queue
   */
  reviewQueueTotalPages?: number;

  /**
   * Total pages available for history
   */
  historyTotalPages?: number;
}

// ============================================
// UI Component Props Types (Optional but helpful)
// ============================================

/**
 * LearningCard Component Props
 * Displays a single word with progress information
 */
export interface LearningCardProps {
  word: LearningProgressResponse;
  onReview?: (wordId: number, isCorrect: boolean) => Promise<void>;
  isReviewing?: boolean;
  onStartLearning?: (wordId: number) => Promise<void>;
  isStarting?: boolean;
}

/**
 * ReviewForm Component Props
 * Interactive form for reviewing a word
 * CRITICAL: Must have duplicate submission prevention
 */
export interface ReviewFormProps {
  word: LearningProgressResponse;
  onSubmit: (wordId: number, isCorrect: boolean) => Promise<void>;
  isLoading: boolean;
  onSuccess?: (updated: LearningProgressResponse) => void;
  onError?: (error: Error) => void;
}

/**
 * Statistics Component Props
 * Displays summary statistics
 */
export interface StatisticsProps {
  statistics: ProgressStatisticsResponse | null;
  isLoading: boolean;
  error?: Error;
}

/**
 * Learning History Item Props
 * Displays a single history entry
 */
export interface HistoryItemProps {
  entry: LearningHistoryResponse;
}

// ============================================
// SRS (Spaced Repetition System) Types
// ============================================

/**
 * SRS Interval Configuration
 * Defines review intervals based on review count
 * 
 * Backend uses this algorithm:
 * - Review 0 (first attempt): +1 day
 * - Review 1: +3 days
 * - Review 2: +7 days
 * - Review 3: +14 days
 * - Review 4+: +30 days
 */
export interface SRSInterval {
  reviewCount: number;
  daysUntilNextReview: number;
  description: string;
}

/**
 * Mastery Criteria
 * Conditions for marking a word as MASTERED
 * 
 * Backend criteria:
 * - Accuracy >= 80% (80+ correct out of total reviews)
 * - ReviewCount >= 5 (at least 5 review attempts)
 */
export interface MasteryCriteria {
  minAccuracy: number; // 80
  minReviewCount: number; // 5
}

// ============================================
// Error & Status Types
// ============================================

/**
 * Learning Error Types
 * Specific error scenarios for learning module
 */
export type LearningErrorCode =
  | 'WORD_NOT_FOUND' // 404: Word doesn't exist
  | 'ALREADY_LEARNING' // 400: Already learning this word
  | 'NOT_LEARNING_YET' // 400: Haven't started learning this word
  | 'UNAUTHORIZED' // 401: JWT token invalid/expired
  | 'FORBIDDEN' // 403: No permission
  | 'SERVER_ERROR' // 500: Server error
  | 'UNKNOWN'; // Unknown error

/**
 * Learning Error Response
 */
export interface LearningError {
  code: LearningErrorCode;
  message: string;
  statusCode: number;
  field?: string;
}

// ============================================
// Display/Format Helper Types
// ============================================

/**
 * Status Display Configuration
 * Maps LearningStatus to display text and styling
 */
export interface StatusDisplay {
  status: LearningStatus;
  label: string;
  color: string; // Tailwind color class
  icon: string; // Emoji or icon name
}

/**
 * Activity Type Display Configuration
 * Maps ActivityType to display text and styling
 */
export interface ActivityTypeDisplay {
  type: ActivityType;
  label: string;
  icon: string;
}

/**
 * Learning Progress Display Helper
 * Calculated display values for a word
 */
export interface LearningProgressDisplay extends LearningProgressResponse {
  // Display-friendly values
  accuracyPercentage: string; // e.g., "85.5%"
  nextReviewDisplay: string; // e.g., "Tomorrow" or "3 days" or "Mastered"
  reviewCountDisplay: string; // e.g., "3/5" (current/mastery target)
  statusLabel: string; // e.g., "🎓 Learning"
  isNearMastery: boolean; // true if accuracy >= 80% but reviewCount < 5
  isMastered: boolean; // true if status === MASTERED
}

// ============================================
// Export Summary
// ============================================

/**
 * EXPORT SUMMARY
 * 
 * Status Types:
 * - LearningStatus
 * - ActivityType
 * - PartOfSpeech
 * - ProficiencyLevel
 * 
 * DTOs:
 * - LearningProgressResponse
 * - LearningHistoryResponse
 * - ProgressStatisticsResponse
 * - ReviewWordRequest
 * 
 * Pagination:
 * - PaginatedResponse<T>
 * 
 * React Hook:
 * - UseLearningReturn
 * 
 * Component Props:
 * - LearningCardProps
 * - ReviewFormProps
 * - StatisticsProps
 * - HistoryItemProps
 * 
 * SRS:
 * - SRSInterval
 * - MasteryCriteria
 * 
 * Display:
 * - StatusDisplay
 * - ActivityTypeDisplay
 * - LearningProgressDisplay
 * 
 * Errors:
 * - LearningErrorCode
 * - LearningError
 */
