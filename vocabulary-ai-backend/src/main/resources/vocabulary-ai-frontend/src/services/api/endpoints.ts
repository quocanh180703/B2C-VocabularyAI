/**
 * API Endpoints
 * Định nghĩa tất cả endpoints trong ứng dụng
 */

// ============================================
// Auth Endpoints
// ============================================
export const AUTH_ENDPOINTS = {
  // Login
  LOGIN: "/auth/login",
  
  // Register (2-step OTP flow)
  REGISTER_VALIDATE: "/auth/register/validate",
  REGISTER_VERIFY_OTP: "/auth/register/verify-otp",
  
  // Token Management
  REFRESH: "/auth/refresh",
  LOGOUT: "/auth/logout",
  
  // Forgot Password (3-step OTP flow with auto-login)
  FORGOT_PASSWORD_SEND_OTP: "/auth/forgot-password/send-otp",
  FORGOT_PASSWORD_VERIFY_OTP: "/auth/forgot-password/verify-otp",
  FORGOT_PASSWORD_RESET: "/auth/forgot-password/reset",
} as const;

// ============================================
// User Endpoints
// ============================================
export const USER_ENDPOINTS = {
  GET_PROFILE: "/users/me",
  UPDATE_PROFILE: "/users/me",
  CHANGE_PASSWORD: "/users/change-password",
  DELETE_ACCOUNT: "/users/me",
  GET_SETTINGS: "/users/settings",
  UPDATE_SETTINGS: "/users/settings",
  GET_STATISTICS: "/users/statistics",
  GET_PROGRESS: "/users/progress",
} as const;

// ============================================
// Vocabulary Endpoints
// ============================================
export const VOCABULARY_ENDPOINTS = {
  GET_WORDS: "/vocabulary/words",
  GET_WORD_BY_ID: (id: string) => `/vocabulary/words/${id}`,
  GET_WORD_DETAIL: (id: string) => `/vocabulary/words/${id}/detail`,
  CREATE_WORD: "/vocabulary/words",
  UPDATE_WORD: (id: string) => `/vocabulary/words/${id}`,
  DELETE_WORD: (id: string) => `/vocabulary/words/${id}`,
  SEARCH_WORDS: "/vocabulary/words/search",
  GET_CATEGORIES: "/vocabulary/categories",
  GET_WORDS_BY_CATEGORY: (category: string) => `/vocabulary/categories/${category}/words`,
  ADD_TO_FAVORITES: (id: string) => `/vocabulary/words/${id}/favorite`,
  REMOVE_FROM_FAVORITES: (id: string) => `/vocabulary/words/${id}/favorite`,
  GET_FAVORITES: "/vocabulary/favorites",
} as const;

// ============================================
// Learning/Flashcard Endpoints
// ============================================
export const LEARNING_ENDPOINTS = {
  GET_SESSION: (id: string) => `/learning/sessions/${id}`,
  START_SESSION: "/learning/sessions",
  COMPLETE_SESSION: (id: string) => `/learning/sessions/${id}/complete`,
  SAVE_REVIEW_RESULT: (id: string) => `/learning/sessions/${id}/results`,
  GET_DAILY_REVIEW_WORDS: "/learning/daily-reviews",
  GET_REVIEW_STATS: "/learning/stats",
  GET_STREAK: "/learning/streak",
} as const;

// ============================================
// Quiz Endpoints
// ============================================
export const QUIZ_ENDPOINTS = {
  GET_QUIZZES: "/quizzes",
  GET_QUIZ_BY_ID: (id: string) => `/quizzes/${id}`,
  GET_QUIZ_QUESTIONS: (id: string) => `/quizzes/${id}/questions`,
  START_QUIZ: (id: string) => `/quizzes/${id}/start`,
  SUBMIT_QUIZ_ANSWER: (id: string) => `/quizzes/${id}/answers`,
  COMPLETE_QUIZ: (id: string) => `/quizzes/${id}/complete`,
  GET_QUIZ_ATTEMPTS: "/quizzes/attempts",
  GET_QUIZ_ATTEMPT: (id: string) => `/quizzes/attempts/${id}`,
  GET_QUIZ_LEADERBOARD: (id: string) => `/quizzes/${id}/leaderboard`,
} as const;

// ============================================
// AI/Learning Tools Endpoints
// ============================================
export const AI_ENDPOINTS = {
  // Vocabulary Endpoints
  VOCABULARY_EXPLAIN: "/api/v1/ai/vocabulary/explain",
  VOCABULARY_EXPLAIN_BATCH: "/api/v1/ai/vocabulary/explain-batch",
  VOCABULARY_CACHE_CLEAR: "/api/v1/ai/vocabulary/cache-clear",
  
  // Grammar Endpoints
  GRAMMAR_CHECK: "/api/v1/ai/grammar/check",
  GRAMMAR_CHECK_BATCH: "/api/v1/ai/grammar/check-batch",
  GRAMMAR_CACHE_CLEAR: "/api/v1/ai/grammar/cache-clear",
  
  // Hints Endpoints
  HINTS_GENERATE: "/api/v1/ai/hints/generate",
  HINTS_GENERATE_BATCH: "/api/v1/ai/hints/generate-batch",
  HINTS_CACHE_CLEAR: "/api/v1/ai/hints/cache-clear",
  
  // Chat Endpoints (Conversations)
  CHAT_CREATE_CONVERSATION: "/api/v1/ai/conversations",
  CHAT_GET_CONVERSATIONS: "/api/v1/ai/conversations",
  CHAT_GET_CONVERSATION: (id: string) => `/api/v1/ai/conversations/${id}`,
  CHAT_SEND_MESSAGE: (id: string) => `/api/v1/ai/conversations/${id}/messages`,
  CHAT_DELETE_CONVERSATION: (id: string) => `/api/v1/ai/conversations/${id}`,
} as const;

// ============================================
// Personalization Endpoints
// ============================================
export const PERSONALIZATION_ENDPOINTS = {
  GET_PROFILE: "/ai/personalized/profile",
  GET_LEARNING_PLAN: "/ai/personalized/plan",
  GET_PROGRESS_ANALYSIS: "/ai/personalized/progress-analysis",
  GET_RECOMMENDATIONS: "/ai/personalized/recommendations",
  GET_WEAKNESS_STRENGTH: "/ai/personalized/analysis",
  GET_ADAPTIVE_DIFFICULTY: "/ai/personalized/adaptive-difficulty",
  GET_DAILY_PRACTICE: "/ai/personalized/daily-practice",
} as const;

// ============================================
// Speaking Sessions Endpoints
// ============================================
export const SPEAKING_SESSIONS = {
  // Session Management (10 endpoints)
  CREATE_SESSION: "/api/ai/speaking/sessions",
  GET_SESSIONS: "/api/ai/speaking/sessions",
  GET_SESSIONS_BY_STATUS: (status: string) => `/api/ai/speaking/sessions/status/${status}`,
  GET_SESSIONS_BY_LEVEL: (level: string) => `/api/ai/speaking/sessions/level/${level}`,
  GET_SESSION: (id: string) => `/api/ai/speaking/sessions/${id}`,
  GET_LATEST_IN_PROGRESS: "/api/ai/speaking/sessions/latest/in-progress",
  COMPLETE_SESSION: (id: string) => `/api/ai/speaking/sessions/${id}/complete`,
  CANCEL_SESSION: (id: string) => `/api/ai/speaking/sessions/${id}/cancel`,
  DELETE_SESSION: (id: string) => `/api/ai/speaking/sessions/${id}`,
  GET_COMPLETED_SORTED_BY_SCORE: "/api/ai/speaking/sessions/completed/sorted-by-score",
} as const;

// ============================================
// Speaking Practice/Attempts Endpoints
// ============================================
export const SPEAKING_ATTEMPTS = {
  // Practice & Recording (2 endpoints)
  SUBMIT_ATTEMPT: "/api/ai/speaking/attempts",
  GET_PRACTICE_SESSION: (sessionId: string) => `/api/ai/speaking/attempts/${sessionId}`,
} as const;

// ============================================
// Speaking Feedback Endpoints
// ============================================
export const SPEAKING_FEEDBACK = {
  // Feedback Retrieval (8 endpoints)
  GET_ATTEMPT_FEEDBACK: (attemptId: string) => `/api/ai/speaking/feedback/attempts/${attemptId}`,
  GET_SESSION_FEEDBACK: (sessionId: string) => `/api/ai/speaking/feedback/sessions/${sessionId}`,
  GET_SESSION_FEEDBACK_LIST: (sessionId: string) => `/api/ai/speaking/feedback/sessions/${sessionId}/list`,
  GET_BEST_FEEDBACK: (sessionId: string) => `/api/ai/speaking/feedback/sessions/${sessionId}/best`,
  GET_FEEDBACK_SORTED_BY_SCORE: (sessionId: string) => `/api/ai/speaking/feedback/sessions/${sessionId}/sorted-by-score`,
  GET_FEEDBACK_SORTED_BY_GRAMMAR: (sessionId: string) => `/api/ai/speaking/feedback/sessions/${sessionId}/sorted-by-grammar`,
  GET_FEEDBACK_SORTED_BY_VOCABULARY: (sessionId: string) => `/api/ai/speaking/feedback/sessions/${sessionId}/sorted-by-vocabulary`,
  GET_SESSION_STATISTICS: (sessionId: string) => `/api/ai/speaking/feedback/sessions/${sessionId}/statistics`,
} as const;

// ============================================
// Speaking Progress Endpoints
// ============================================
export const SPEAKING_PROGRESS = {
  // Progress Tracking (4 endpoints)
  GET_PROGRESS: "/api/ai/speaking/progress",
  GET_SCORE_TREND: (days: number) => `/api/ai/speaking/progress/trend/${days}`,
  GET_MILESTONES: "/api/ai/speaking/progress/milestones",
  GET_PROGRESS_SUMMARY: "/api/ai/speaking/progress/summary",
} as const;

// ============================================
// Text-to-Speech Endpoints
// ============================================
export const SPEAKING_TTS = {
  // TTS Generation (3 endpoints)
  GENERATE_TTS_WORD: "/api/ai/speaking/tts/word",
  GENERATE_TTS_PHRASE: "/api/ai/speaking/tts/phrase",
  GENERATE_TTS_SESSION_REFERENCE: (sessionId: string) => `/api/ai/speaking/tts/sessions/${sessionId}/reference`,
} as const;

// ============================================
// Audio Storage Endpoints
// ============================================
export const SPEAKING_AUDIO = {
  // Audio Download & Streaming (2 endpoints)
  DOWNLOAD_AUDIO: (sessionId: string, attemptNumber: number) => `/api/ai/speaking/audio/${sessionId}/attempt-${attemptNumber}`,
  STREAM_AUDIO: (sessionId: string, attemptNumber: number) => `/api/ai/speaking/audio/${sessionId}/attempt-${attemptNumber}/stream`,
} as const;

// ============================================
// Speaking Endpoints (Consolidated Export)
// ============================================
export const SPEAKING_ENDPOINTS = {
  ...SPEAKING_SESSIONS,
  ...SPEAKING_ATTEMPTS,
  ...SPEAKING_FEEDBACK,
  ...SPEAKING_PROGRESS,
  ...SPEAKING_TTS,
  ...SPEAKING_AUDIO,
} as const;

// ============================================
// Payment Endpoints
// ============================================
export const PAYMENT_ENDPOINTS = {
  // Packages
  GET_PACKAGES: "/api/payments/packages",
  
  // Payment Initiation
  INITIATE_PAYMENT: "/api/payments/initiate",
  GET_PAYMENT_ORDER: (id: string) => `/api/payments/${id}`,
  
  // Subscription Management
  GET_SUBSCRIPTION_STATUS: "/api/subscriptions/status",
  CANCEL_SUBSCRIPTION: "/api/subscriptions/cancel",
  
  // Payment History
  GET_PAYMENT_HISTORY: "/api/payments/history",
} as const;

// ============================================
// Admin Endpoints (Future use)
// ============================================
export const ADMIN_ENDPOINTS = {
  GET_USERS: "/admin/users",
  GET_USER_BY_ID: (id: string) => `/admin/users/${id}`,
  UPDATE_USER: (id: string) => `/admin/users/${id}`,
  DELETE_USER: (id: string) => `/admin/users/${id}`,
  GET_STATISTICS: "/admin/statistics",
  GET_AI_LOGS: "/admin/ai-logs",
  GET_PAYMENT_LOGS: "/admin/payment-logs",
  EXPORT_DATA: "/admin/export",
} as const;

// ============================================
// Health/Status Endpoints
// ============================================
export const STATUS_ENDPOINTS = {
  HEALTH_CHECK: "/health",
  GET_VERSION: "/version",
  GET_API_DOCS: "/api-docs",
} as const;
