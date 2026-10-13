/**
 * Speaking Module Types
 * Type definitions for speaking practice APIs
 * 
 * Updated: Phase 11B - Backend API Contract
 * Source: Backend Speaking Module (SpeakingSession, SpeakingAttempt, SpeakingFeedback entities)
 */

// ============================================
// ENUMS
// ============================================

export type SpeakingLevel = "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
export type SpeakingStatus = "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

// ============================================
// SESSION TYPES
// ============================================

/**
 * SpeakingSession Response
 * Thông tin cơ bản về một phiên luyện nói
 */
export interface SpeakingSession {
  id: number;
  topic: string;
  level: SpeakingLevel;
  description?: string;
  status: SpeakingStatus;
  startedAt: string; // ISO datetime
  completedAt?: string; // ISO datetime, null if not completed
  overallScore?: number; // 0-100, only when completed
  attemptCount: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * SpeakingSessionSummary Response
 * Chi tiết phiên kèm danh sách attempts + feedback
 */
export interface SpeakingSessionSummary extends SpeakingSession {
  attempts: SpeakingAttempt[];
  feedback?: SpeakingSessionFeedback;
}

/**
 * Create Session Request
 */
export interface CreateSpeakingSessionRequest {
  topic: string; // Required, 3-255 chars
  level: SpeakingLevel; // Required: A1-C2
  description?: string; // Optional, max 2000 chars
}

// ============================================
// ATTEMPT TYPES
// ============================================

/**
 * SpeakingAttempt Response
 * Thông tin về một lần ghi âm
 */
export interface SpeakingAttempt {
  id: number;
  sessionId: number;
  attemptNumber: number;
  audioUrl: string; // URL để download/stream audio
  audioDurationSeconds?: number;
  transcript: string; // Kết quả STT
  transcriptConfidence?: number; // 0-1.0
  createdAt: string;
  feedback?: SpeakingAttemptFeedback;
}

/**
 * Submit Attempt Request
 * Upload audio file cho một attempt
 */
export interface SubmitAttemptRequest {
  sessionId: number;
  audioFile: Blob;
}

// ============================================
// FEEDBACK TYPES
// ============================================

/**
 * Correction Info
 * Chi tiết một lỗi được sửa
 */
export interface CorrectionInfo {
  original: string;
  corrected: string;
  errorType: string; // "grammar", "vocabulary", etc.
  explanation: string;
}

/**
 * SpeakingAttemptFeedback Response
 * Feedback chi tiết cho một attempt
 */
export interface SpeakingAttemptFeedback {
  id: number;
  attemptId: number;
  sessionId: number;
  overallScore: number; // 0-100
  grammarScore: number;
  vocabularyScore: number;
  fluencyScore: number;
  contentScore: number;
  pronunciationScore?: number;
  feedback: string; // General feedback text
  corrections: CorrectionInfo[]; // Array of corrections
  suggestions: string[]; // List of suggestions
  strengths: string[]; // List of strengths
  weaknesses: string[]; // List of weaknesses
  createdAt: string;
  updatedAt: string;
}

/**
 * Session-level Feedback
 * Feedback tổng hợp cho cả phiên
 */
export interface SpeakingSessionFeedback extends SpeakingAttemptFeedback {
  // Thêm các trường nếu cần
}

// ============================================
// PRACTICE RESPONSE TYPES
// ============================================

/**
 * Scores Info
 * Bộ điểm từ AI analysis
 */
export interface ScoresInfo {
  overallScore: number;
  grammarScore: number;
  vocabularyScore: number;
  fluencyScore: number;
  contentScore: number;
  pronunciationScore?: number;
}

/**
 * Feedback Info
 * Thông tin feedback chi tiết
 */
export interface FeedbackInfo {
  feedback: string;
  corrections: CorrectionInfo[];
  suggestions: string[];
  strengths: string[];
  weaknesses: string[];
}

/**
 * Pronunciation Info
 * Phân tích phát âm (nếu provider hỗ trợ)
 */
export interface PronunciationInfo {
  pronunciationScore?: number;
  feedback?: string;
  stressFeedback?: string;
  intonationFeedback?: string;
  provider?: string;
}

/**
 * Statistics Info
 * Thống kê của phiên
 */
export interface StatisticsInfo {
  totalAttempts: number;
  averageScore: number;
  bestScore: number;
  averageGrammarScore?: number;
  averageVocabularyScore?: number;
  averageFluencyScore?: number;
  averageContentScore?: number;
  improvement?: string; // e.g., "↑ 1.5%"
}

/**
 * SpeakingPracticeResponse
 * Response từ submit attempt (POST /api/ai/speaking/attempts)
 */
export interface SpeakingPracticeResponse {
  session: SpeakingSession;
  attempt: SpeakingAttempt;
  scores: ScoresInfo;
  feedback: FeedbackInfo;
  pronunciation?: PronunciationInfo;
  statistics: StatisticsInfo;
}

// ============================================
// PROGRESS TYPES
// ============================================

/**
 * Score Breakdown
 * Bộ điểm chi tiết
 */
export interface ScoreBreakdown {
  overallScore: number;
  grammarScore: number;
  vocabularyScore: number;
  fluencyScore: number;
  contentScore: number;
  pronunciationScore?: number;
}

/**
 * Session Statistics
 * Thống kê phiên tập
 */
export interface SessionStatistics {
  totalSessions: number;
  completedSessions: number;
  totalAttempts: number;
  totalTimeSpentMinutes?: number;
  averageAttemptsPerSession?: number;
}

/**
 * Recent Performance
 * Hiệu suất gần đây
 */
export interface RecentPerformance {
  sevenDayAverage?: number;
  thirtyDayAverage?: number;
  sevenDayTrend?: "Improving" | "Stable" | "Declining";
  thirtyDayTrend?: "Improving" | "Stable" | "Declining";
}

/**
 * Streaks Info
 * Thông tin streak
 */
export interface StreaksInfo {
  currentStreak: number;
  longestStreak: number;
  lastPracticeDate?: string;
}

/**
 * SpeakingProgressResponse
 * Response từ GET /api/ai/speaking/progress
 */
export interface SpeakingProgressResponse {
  statistics: SessionStatistics;
  averageScores: ScoreBreakdown;
  bestScores: ScoreBreakdown;
  recentPerformance: RecentPerformance;
  streaks: StreaksInfo;
  achievements: string[];
  scoreTrend?: "Improving" | "Stable" | "Declining";
}

/**
 * Score Trend Response
 * Response từ GET /api/ai/speaking/progress/trend/{days}
 */
export interface ScoreTrendResponse {
  days: number;
  scoreTrend: "Improving" | "Stable" | "Declining";
  analyzedAt: string;
}

// ============================================
// TEXT-TO-SPEECH TYPES
// ============================================

/**
 * TextToSpeechRequest
 */
export interface TextToSpeechRequest {
  word?: string; // For word TTS
  phrase?: string; // For phrase TTS
  language?: string; // Default: "en-US"
}

/**
 * TextToSpeechResponse
 */
export interface TextToSpeechResponse {
  word?: string;
  phrase?: string;
  sessionId?: number;
  topic?: string;
  audioUrl: string;
  audioFormat: string;
  durationSeconds?: number;
  voice?: string;
  language?: string;
  provider?: string;
  processingTimeMs?: number;
  success: boolean;
  createdAt?: string;
}

// ============================================
// PAGINATION & LIST TYPES
// ============================================

/**
 * Paginated Response Wrapper
 */
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// ============================================
// RECORDING TYPES
// ============================================

/**
 * Audio Recording State
 * Trạng thái recording hiện tại
 */
export interface AudioRecordingState {
  isRecording: boolean;
  isPaused: boolean;
  duration: number; // in seconds
  error?: string;
}

/**
 * Audio Blob Data
 * Dữ liệu blob audio ghi được
 */
export interface AudioBlobData {
  blob: Blob;
  duration: number;
  mimeType: string;
}
