/**
 * API Request/Response Types
 * Định nghĩa tất cả types cho API communication
 */

// ============================================
// Generic Response Types
// ============================================

export interface ApiResponse<T = any> {
  success: boolean;
  statusCode: number;
  message: string;
  data?: T;
  timestamp?: string;
  path?: string;
}

export interface ApiListResponse<T = any> {
  success: boolean;
  statusCode: number;
  message: string;
  data: T[];
  pagination?: PaginationInfo;
  timestamp?: string;
}

export interface PaginationInfo {
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}

export interface ApiError {
  success: false;
  statusCode: number;
  message: string;
  errors?: ErrorDetail[];
  timestamp?: string;
  path?: string;
}

export interface ErrorDetail {
  field?: string;
  message: string;
  code?: string;
}

// ============================================
// Auth Types
// ============================================

/**
 * Login Request DTO
 * Supports both email and username
 */
export interface LoginRequest {
  emailOrUsername: string;
  password: string;
}

/**
 * Register Step 1: Validate Registration
 * Checks email/username validity and sends OTP
 */
export interface ValidateRegisterRequest {
  email: string;
  username: string;
  password: string;
  fullName: string;
}

/**
 * Register Step 1 Response
 */
export interface ValidateRegisterResponse {
  message: string;
  emailMasked: string;
}

/**
 * Register Step 2: Verify OTP
 * Verifies OTP and creates account
 */
export interface VerifyOtpRegisterRequest {
  email: string;
  otp: string;
}

/**
 * Register Step 2 Response
 * Note: Does NOT return JWT - user must login manually
 */
export interface RegisterSuccessResponse {
  message: string;
  email: string;
  username: string;
}

/**
 * User Response DTO
 * Returned in auth response
 */
export interface UserResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  avatarUrl: string | null;
  role: "STUDENT" | "ADMIN";
  status: "ACTIVE" | "INACTIVE" | "BANNED";
  createdAt: string;
  updatedAt: string;
}

/**
 * Auth Response DTO
 * Returned after successful login or password reset
 * Note: access_token and refresh_token use snake_case (backend convention)
 */
export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  token_type: "Bearer";
  expires_in: number; // In milliseconds (3600000 for 1 hour)
  user: UserResponse;
}

/**
 * Forgot Password Step 1: Send OTP
 */
export interface SendOtpRequest {
  email: string;
}

/**
 * Forgot Password Step 1 Response
 */
export interface SendOtpResponse {
  message: string;
  emailMasked: string;
}

/**
 * Forgot Password Step 2: Verify OTP
 */
export interface VerifyOtpForgotPasswordRequest {
  email: string;
  otp: string;
}

/**
 * Forgot Password Step 2 Response
 * Returns reset token for step 3
 */
export interface VerifyOtpForgotPasswordResponse {
  message: string;
  resetToken: string;
}

/**
 * Forgot Password Step 3: Reset Password
 * Note: This endpoint returns AuthResponse with auto-login (JWT tokens)
 */
export interface ResetPasswordRequest {
  email: string;
  newPassword: string;
  resetToken: string;
}

/**
 * Refresh Token Request
 */
export interface RefreshTokenRequest {
  refreshToken: string;
}

/**
 * Logout Request
 */
export interface LogoutRequest {
  refreshToken: string;
}

// ============================================
// User Types
// ============================================

export interface User {
  id: string;
  email: string;
  fullName: string;
  englishLevel: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  profilePicture?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserUpdateRequest {
  fullName?: string;
  englishLevel?: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  profilePicture?: string;
}

// ============================================
// Vocabulary Types
// ============================================

export interface Word {
  id: string;
  word: string;
  phonetic: string;
  meaning: string;
  translation: string;
  level: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  type: "Noun" | "Verb" | "Adjective" | "Adverb" | "Preposition" | "Other";
  category: string;
  examples?: string[];
  synonyms?: string[];
  antonyms?: string[];
  imageUrl?: string;
  audioUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface WordDetailResponse extends Word {
  relatedWords?: Word[];
  userProgress?: {
    learned: boolean;
    familiar: boolean;
    masterCount: number;
    reviewCount: number;
    lastReviewedAt?: string;
  };
}

export interface CreateWordRequest {
  word: string;
  phonetic: string;
  meaning: string;
  translation: string;
  level: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  type: "Noun" | "Verb" | "Adjective" | "Adverb" | "Preposition" | "Other";
  category: string;
  examples?: string[];
  synonyms?: string[];
  antonyms?: string[];
}

export interface GetWordsRequest {
  page?: number;
  pageSize?: number;
  level?: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  category?: string;
  search?: string;
  sortBy?: "createdAt" | "word" | "level";
  sortDirection?: "asc" | "desc";
}

// ============================================
// CEFR Vocabulary Types (from backend)
// ============================================

/**
 * CEFR Level enum (A1-C1)
 */
export type CEFRLevel = "A1" | "A2" | "B1" | "B2" | "C1";

/**
 * Part of Speech enum
 */
export type PartOfSpeech = "NOUN" | "VERB" | "ADJECTIVE" | "ADVERB" | "PREPOSITION" | "CONJUNCTION" | "INTERJECTION" | "OTHER";

/**
 * CEFR Word Response DTO
 * Represents a word from the CEFR vocabulary dataset
 */
export interface CefrWordResponse {
  id: string; // UUID
  lemma: string; // Word text
  cefrLevel: CEFRLevel;
  partOfSpeech: PartOfSpeech;
  pronunciation: string; // IPA
  definition: string;
  source: string; // oxford_3000_5000, etc.
  language: string; // "en"
  createdAt: string; // ISO datetime
  updatedAt: string; // ISO datetime
}

/**
 * Search request for CEFR words
 */
export interface CefrWordSearchRequest {
  query?: string;
  level?: CEFRLevel;
  page?: number;
  size?: number;
}

/**
 * Explanation Response from AI (for word explanations)
 */
export interface ExplanationResponse {
  wordId: number;
  word: string;
  explanation: string;
  examples: string[];
  etymology: string;
  relatedWords: string[];
  partOfSpeech: string;
}

// ============================================
// Learning/Flashcard Types
// ============================================

export interface FlashcardSession {
  id: string;
  userId: string;
  wordCount: number;
  correctCount: number;
  accuracy: number;
  duration: number;
  startedAt: string;
  completedAt: string;
}

export interface ReviewResult {
  wordId: string;
  remembered: boolean;
  difficulty?: "easy" | "medium" | "hard";
  timeSpent: number;
}

export interface StartSessionRequest {
  wordIds?: string[];
  level?: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  category?: string;
}

export interface CompleteSessionRequest {
  sessionId: string;
  results: ReviewResult[];
  totalDuration: number;
}

// ============================================
// Quiz Types
// ============================================

export interface Quiz {
  id: string;
  title: string;
  description: string;
  level: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  category: string;
  questionCount: number;
  duration: number;
  passingScore: number;
  createdAt: string;
}

export interface QuizQuestion {
  id: string;
  question: string;
  options: string[];
  correctAnswerIndex: number;
  explanation: string;
  difficulty: "easy" | "medium" | "hard";
}

export interface QuizAttempt {
  id: string;
  quizId: string;
  userId: string;
  score: number;
  accuracy: number;
  passed: boolean;
  duration: number;
  startedAt: string;
  completedAt: string;
  answers: QuizAnswerRecord[];
}

export interface QuizAnswerRecord {
  questionId: string;
  selectedAnswerIndex: number;
  isCorrect: boolean;
  timeSpent: number;
}

export interface StartQuizRequest {
  quizId: string;
}

export interface CompleteQuizRequest {
  quizId: string;
  answers: QuizAnswerRecord[];
  totalDuration: number;
}

// ============================================
// AI Types
// ============================================

export interface ExplainWordRequest {
  word: string;
  level?: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  context?: string;
}

export interface WordExplanation {
  word: string;
  phonetic?: string;
  partOfSpeech: string;
  definition: string;
  vietnameseMeaning: string;
  examples: { english: string; vietnamese: string }[];
  synonyms?: string[];
  usageTips?: string;
  commonMistakes?: string;
}

export interface GrammarCheckRequest {
  text: string;
  level?: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
}

export interface GrammarError {
  startOffset: number;
  endOffset: number;
  errorType: string;
  suggestion: string;
  explanation: string;
}

export interface GrammarCheckResponse {
  originalText: string;
  correctedText: string;
  score: number;
  errors: GrammarError[];
  suggestions: string[];
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  timestamp: string;
}

export interface ChatRequest {
  message: string;
  conversationId?: string;
  level?: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  context?: string;
}

export interface ChatResponse {
  conversationId: string;
  assistantMessage: string;
  suggestions?: string[];
  corrections?: { original: string; corrected: string }[];
}

// ============================================
// Speaking Types
// ============================================

export interface SpeakingSession {
  id: string;
  userId: string;
  topic: string;
  questionCount: number;
  startedAt: string;
  completedAt?: string;
  totalDuration?: number;
}

export interface SpeakingQuestion {
  id: string;
  question: string;
  hint?: string;
  expectedDuration: number;
}

export interface SpeakingFeedback {
  sessionId: string;
  questionId: string;
  transcript: string;
  grammarScore: number;
  vocabularyScore: number;
  fluencyScore: number;
  naturalness: number;
  overallScore: number;
  corrections: { original: string; corrected: string }[];
  suggestions: string[];
  strengths: string[];
  analysis?: string;
}

export interface SubmitSpeakingRequest {
  sessionId: string;
  questionId: string;
  audioUrl: string;
  transcript: string;
  duration: number;
}

export interface CompleteSpeakingSessionRequest {
  sessionId: string;
  totalDuration: number;
}

// ============================================
// Payment Types
// ============================================

export interface PricingPlan {
  id: string;
  name: string;
  price: number;
  currency: string;
  billingPeriod: "monthly" | "yearly";
  features: string[];
  description: string;
  isPopular?: boolean;
  createdAt: string;
}

export interface CreatePaymentRequest {
  planId: string;
  billingPeriod?: "monthly" | "yearly";
}

export interface PaymentResponse {
  paymentId: string;
  status: "pending" | "completed" | "failed";
  checkoutUrl?: string;
  amount: number;
  planId: string;
  expiresAt?: string;
}

export interface VerifyPaymentRequest {
  paymentId: string;
  transactionId: string;
}

export interface Subscription {
  id: string;
  userId: string;
  planId: string;
  status: "active" | "cancelled" | "expired";
  startDate: string;
  endDate: string;
  autoRenew: boolean;
  createdAt: string;
}

// ============================================
// Statistics Types
// ============================================

export interface UserStatistics {
  totalWordsLearned: number;
  totalWordsMastered: number;
  currentStreak: number;
  longestStreak: number;
  totalLearningTime: number;
  quizzesCompleted: number;
  averageAccuracy: number;
  lastActiveAt: string;
}

export interface LearningProgress {
  date: string;
  wordsLearned: number;
  sessionCount: number;
  totalMinutes: number;
}

// ============================================
// Pagination Request
// ============================================

export interface PaginationRequest {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortDirection?: "asc" | "desc";
}


// ============================================
// DASHBOARD TYPES
// ============================================

/**
 * Learning Progress Statistics
 * Contains aggregated statistics about user's learning progress
 */
export interface ProgressStatisticsResponse {
  totalWordsLearned: number;
  wordsInProgress: number;
  learningStreak: number;
  currentStreak: number;
  longestStreak: number;
  totalLearningTime: number; // in minutes
  averageAccuracy: number; // percentage
  dailyGoalProgress: number;
  dailyGoalTarget: number;
}

/**
 * Learning Progress for a single word
 * Used in review queue and progress list
 */
export interface LearningProgressResponse {
  id: number;
  wordId: number;
  word: string;
  definition: string;
  nextReviewAt: string; // ISO datetime
  reviewCount: number;
  correctCount: number;
  difficulty: "EASY" | "MEDIUM" | "HARD";
  createdAt: string;
  updatedAt: string;
}

/**
 * Favorite Word
 * User's marked favorite vocabulary words
 */
export interface FavoriteWordResponse {
  id: number;
  wordId: number;
  word: string;
  definition: string;
  category: string;
  level: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
  createdAt: string;
}

/**
 * Quiz Attempt Result
 * Summary of a quiz attempt by user
 */
export interface QuizAttemptResponse {
  id: number;
  quizId: number;
  quizTitle: string;
  score: number; // percentage
  totalQuestions: number;
  correctAnswers: number;
  timeSpent: number; // in seconds
  createdAt: string;
  attemptedAt: string;
}

/**
 * Personalized Recommendation
 * AI-generated recommendation for learning
 */
export interface RecommendationResponse {
  id: number;
  title: string;
  description: string;
  priority: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";
  actionItems: string[];
  expectedImpact: number; // percentage improvement
  timeRequired: number; // in minutes
  tags: string[];
}

/**
 * Paginated Response Wrapper
 * Generic type for paginated API responses
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
  empty: boolean;
}
