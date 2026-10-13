/**
 * Quiz Module TypeScript Types
 * 
 * Complete TypeScript interfaces matching backend DTOs
 * Source: Backend Quiz API (QuizController, QuizService)
 * 
 * Key Principles:
 * - correctAnswer is hidden during quiz (null)
 * - correctAnswer is shown after submit
 * - score/correctAnswers calculated by backend
 * - Frontend displays only, does not recalculate
 */

// ============================================
// ENUMS
// ============================================

export enum QuizLevel {
  A1 = 'A1',
  A2 = 'A2',
  B1 = 'B1',
  B2 = 'B2',
  C1 = 'C1',
  C2 = 'C2',
}

export enum QuizStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED',
}

// ============================================
// REQUEST TYPES
// ============================================

/**
 * Request payload for submitting quiz answers
 * POST /quizzes/{attemptId}/submit
 */
export interface SubmitQuizRequest {
  answers: QuestionAnswer[];
}

/**
 * Single question answer in submission
 */
export interface QuestionAnswer {
  questionId: number;
  answer: number | null;  // 0-based index or null for unanswered
}

// ============================================
// RESPONSE TYPES (DTO)
// ============================================

/**
 * Quiz metadata response
 * Used in quiz list and quiz detail
 */
export interface QuizResponse {
  id: number;
  categoryId?: number;
  title: string;
  description?: string;
  level?: string;           // 'A1', 'A2', 'B1', 'B2', 'C1', 'C2'
  timeLimit?: number;       // in seconds (e.g., 600 = 10 minutes)
  status: string;           // 'DRAFT', 'PUBLISHED', 'ARCHIVED'
  createdAt: string;        // ISO 8601 format
  updatedAt: string;        // ISO 8601 format
}

/**
 * Question response (from quiz)
 * 
 * During quiz (GET /quizzes/{id}):
 *   - correctAnswer: null
 *   - explanation: null
 * 
 * After submit (GET /quizzes/attempts/{id}):
 *   - correctAnswer: number (shown)
 *   - explanation: string (shown)
 */
export interface QuestionResponse {
  id: number;
  content: string;          // Question text
  options: string[];        // Answer options ["A", "B", "C", "D"]
  correctAnswer: number | null;    // null during quiz, number after submit
  explanation: string | null;      // null during quiz, string after submit
  createdAt: string;        // ISO 8601 format
}

/**
 * Quiz with questions (for taking quiz)
 * GET /quizzes/{id}
 */
export interface QuizWithQuestionsResponse {
  quiz: QuizResponse;
  questions: QuestionResponse[];
}

/**
 * Quiz attempt response
 * Returned when:
 * - POST /quizzes/{quizId}/start (201 CREATED)
 * - POST /quizzes/{attemptId}/submit (200 OK)
 * - GET /quizzes/attempts/{attemptId} (200 OK) - in 'attempt' field
 * - GET /quizzes/history (200 OK) - in 'content' array
 */
export interface QuizAttemptResponse {
  id: number;
  quizId: number;
  score: number;              // 0-100 percentage (calculated by backend)
  totalQuestions: number;
  correctAnswers: number;     // count (calculated by backend)
  completed: boolean;
  timeTaken?: number;         // in seconds (calculated by backend) or null if not submitted
  startedAt: string;          // ISO 8601 format
  submittedAt?: string;       // ISO 8601 format or null if not completed
  createdAt: string;          // ISO 8601 format
}

/**
 * Quiz answer detail (for review after submit)
 * Part of QuizAttemptWithAnswersResponse
 */
export interface QuizAnswerDetailResponse {
  questionId: number;
  content: string;            // Question text
  selectedAnswer: number | null;    // What user selected (0-based or null)
  correctAnswer: number;            // Correct answer (0-based) - shown after submit
  isCorrect: boolean;                // Backend determined
  explanation: string;              // Question explanation - shown after submit
}

/**
 * Quiz attempt with answer details (for review)
 * GET /quizzes/attempts/{attemptId}
 */
export interface QuizAttemptWithAnswersResponse {
  attempt: QuizAttemptResponse;
  answers: QuizAnswerDetailResponse[];
}

/**
 * Generic paginated response wrapper
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

/**
 * Generic API response wrapper
 * All backend endpoints return this format
 */
export interface ApiResponse<T> {
  status: string;
  message: string;
  data: T;
}

// ============================================
// MAPPED RESPONSE TYPES
// ============================================

export type QuizListResponse = ApiResponse<PaginatedResponse<QuizResponse>>;

export type QuizDetailResponse = ApiResponse<QuizWithQuestionsResponse>;

export type StartQuizResponse = ApiResponse<QuizAttemptResponse>;

export type SubmitQuizResponse = ApiResponse<QuizAttemptResponse>;

export type GetAttemptDetailsResponse = ApiResponse<QuizAttemptWithAnswersResponse>;

export type QuizHistoryResponse = ApiResponse<PaginatedResponse<QuizAttemptResponse>>;

// ============================================
// HOOK RETURN TYPES
// ============================================

/**
 * Return type for useQuiz hook
 * Manages queries, mutations, and state for quiz operations
 */
export interface UseQuizReturn {
  // Quiz List State
  quizzes: QuizResponse[];
  quizzesLoading: boolean;
  quizzesError?: Error;
  quizzesPageSize: number;
  quizzesTotalPages: number;
  quizzesCurrentPage: number;

  // Quiz Detail State
  currentQuiz?: QuizWithQuestionsResponse;
  quizLoading: boolean;
  quizError?: Error;

  // Current Attempt State
  currentAttempt?: QuizAttemptResponse;
  attemptLoading: boolean;
  attemptError?: Error;

  // Attempt Details (Review) State
  attemptDetails?: QuizAttemptWithAnswersResponse;
  detailsLoading: boolean;
  detailsError?: Error;

  // Quiz History State
  quizHistory: QuizAttemptResponse[];
  historyLoading: boolean;
  historyError?: Error;
  historyPageSize: number;
  historyTotalPages: number;
  historyCurrentPage: number;

  // Mutations
  startQuiz: (quizId: number) => Promise<QuizAttemptResponse>;
  isStartingQuiz: boolean;

  submitQuiz: (attemptId: number, answers: SubmitQuizRequest) => Promise<QuizAttemptResponse>;
  isSubmittingQuiz: boolean;

  // Pagination
  loadMoreQuizzes: () => void;
  loadMoreHistory: () => void;

  // Helpers
  refetch: () => Promise<void>;
}

// ============================================
// COMPONENT PROP TYPES
// ============================================

/**
 * Props for QuizCard component
 * Displays single quiz in list
 */
export interface QuizCardProps {
  quiz: QuizResponse;
  onStart: (quizId: number) => void;
  isLoading?: boolean;
}

/**
 * Props for QuestionCard component
 * Displays question + options
 */
export interface QuestionCardProps {
  question: QuestionResponse;
  currentQuestionIndex: number;
  totalQuestions: number;
  selectedAnswer: number | null;
  onSelectAnswer: (index: number) => void;
  isLoading?: boolean;
}

/**
 * Props for QuizResults component
 * Displays quiz result after submit
 */
export interface QuizResultsProps {
  attempt: QuizAttemptResponse;
  answers?: QuizAnswerDetailResponse[];
  onRetry: () => void;
  onExit: () => void;
  onReview?: () => void;
  onRetryQuiz?: () => void;
}

/**
 * Props for AnswerReview component
 * Displays correct answer + explanation
 */
export interface AnswerReviewProps {
  answer: QuizAnswerDetailResponse;
  index: number;
}

/**
 * Props for QuizHistory component
 * Displays paginated quiz attempts
 */
export interface QuizHistoryProps {
  attempts: QuizAttemptResponse[];
  isLoading: boolean;
  hasMore: boolean;
  onLoadMore: () => void;
  onSelectAttempt: (attemptId: number) => void;
}

// ============================================
// STATE TYPES
// ============================================

/**
 * State for quiz-taking session
 * Tracks current quiz progress
 */
export interface QuizSessionState {
  quizId: number;
  attemptId: number;
  currentQuestionIndex: number;
  answers: Map<number, number | null>;  // questionId -> selectedAnswer
  startedAt: Date;
  isSubmitting: boolean;
  error?: string;
}

/**
 * Quiz error with status code
 */
export interface QuizError {
  code: QuizErrorCode;
  message: string;
  statusCode: number;
  details?: Record<string, unknown>;
}

export enum QuizErrorCode {
  QUIZ_NOT_PUBLISHED = 'QUIZ_NOT_PUBLISHED',
  QUIZ_ALREADY_SUBMITTED = 'QUIZ_ALREADY_SUBMITTED',
  QUIZ_NO_QUESTIONS = 'QUIZ_NO_QUESTIONS',
  QUIZ_NOT_FOUND = 'QUIZ_NOT_FOUND',
  ATTEMPT_NOT_FOUND = 'ATTEMPT_NOT_FOUND',
  QUESTION_NOT_FOUND = 'QUESTION_NOT_FOUND',
  UNAUTHORIZED = 'UNAUTHORIZED',
  FORBIDDEN = 'FORBIDDEN',
  BAD_REQUEST = 'BAD_REQUEST',
  SERVER_ERROR = 'SERVER_ERROR',
  NETWORK_ERROR = 'NETWORK_ERROR',
}

// ============================================
// UTILITY TYPES
// ============================================

/**
 * Pagination parameters for API requests
 */
export interface PaginationParams {
  page: number;    // 0-indexed
  size: number;    // items per page
}

/**
 * Query key factory for React Query
 * Prevents stale cache and ensures proper invalidation
 */
export const QUIZ_QUERY_KEYS = {
  all: ['quiz'] as const,
  lists: () => [...QUIZ_QUERY_KEYS.all, 'list'] as const,
  list: (page: number, size: number) => [...QUIZ_QUERY_KEYS.lists(), { page, size }] as const,
  details: () => [...QUIZ_QUERY_KEYS.all, 'detail'] as const,
  detail: (quizId: number) => [...QUIZ_QUERY_KEYS.details(), quizId] as const,
  attempts: () => [...QUIZ_QUERY_KEYS.all, 'attempt'] as const,
  attempt: (attemptId: number) => [...QUIZ_QUERY_KEYS.attempts(), attemptId] as const,
  history: () => [...QUIZ_QUERY_KEYS.all, 'history'] as const,
  historyPaginated: (page: number, size: number) => [...QUIZ_QUERY_KEYS.history(), { page, size }] as const,
} as const;

// ============================================
// TYPE GUARDS
// ============================================

/**
 * Check if quiz is published and available
 */
export function isQuizAvailable(quiz: QuizResponse): boolean {
  return quiz.status === QuizStatus.PUBLISHED;
}

/**
 * Check if answer is correct
 */
export function isAnswerCorrect(answer: QuizAnswerDetailResponse): boolean {
  return answer.isCorrect;
}

/**
 * Check if attempt is completed
 */
export function isAttemptCompleted(attempt: QuizAttemptResponse): boolean {
  return attempt.completed && attempt.submittedAt !== null;
}

/**
 * Calculate accuracy percentage from attempt
 */
export function calculateAccuracy(attempt: QuizAttemptResponse): number {
  if (attempt.totalQuestions === 0) return 0;
  return Math.round((attempt.correctAnswers / attempt.totalQuestions) * 100);
}

/**
 * Format time in seconds to readable string
 */
export function formatTimeTaken(seconds?: number): string {
  if (!seconds) return '--';
  const minutes = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${minutes}:${secs.toString().padStart(2, '0')}`;
}

/**
 * Format quiz level for display
 */
export function formatQuizLevel(level?: string): string {
  if (!level) return 'Unknown';
  return `${level} Level`;
}

/**
 * Format time limit to readable string
 */
export function formatTimeLimit(seconds?: number): string {
  if (!seconds) return 'No time limit';
  if (seconds < 60) return `${seconds}s`;
  const minutes = Math.ceil(seconds / 60);
  return `${minutes}min`;
}
