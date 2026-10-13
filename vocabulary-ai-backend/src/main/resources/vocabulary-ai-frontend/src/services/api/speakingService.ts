/**
 * Speaking Service
 * Unified service for all speaking practice APIs
 * 
 * Handles:
 * - Session creation, management, completion
 * - Audio attempt submission with FormData
 * - Feedback retrieval (8 endpoints)
 * - Progress tracking (4 endpoints)
 * - Text-to-speech generation (3 endpoints)
 * - Audio storage/streaming (2 endpoints)
 * 
 * Total: 23 endpoints
 */

import { apiClient } from "./client";
import {
  SPEAKING_SESSIONS,
  SPEAKING_ATTEMPTS,
  SPEAKING_FEEDBACK,
  SPEAKING_PROGRESS,
  SPEAKING_TTS,
  SPEAKING_AUDIO,
} from "./endpoints";

import {
  SpeakingSession,
  SpeakingSessionSummary,
  SpeakingAttempt,
  CreateSpeakingSessionRequest,
  SubmitAttemptRequest,
  SpeakingPracticeResponse,
  SpeakingAttemptFeedback,
  SpeakingProgressResponse,
  ScoreTrendResponse,
  TextToSpeechResponse,
  PaginatedResponse,
} from "./speaking.types";

export class SpeakingService {
  // ============================================
  // SESSION MANAGEMENT (10 endpoints)
  // ============================================

  /**
   * POST /api/ai/speaking/sessions
   * Tạo phiên luyện nói mới
   */
  static async createSession(
    request: CreateSpeakingSessionRequest
  ): Promise<SpeakingSession> {
    const response = await apiClient.post<{ data: SpeakingSession }>(
      SPEAKING_SESSIONS.CREATE_SESSION,
      request
    );
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/sessions
   * Lấy danh sách tất cả phiên của user (paginated)
   */
  static async getSessions(params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<PaginatedResponse<SpeakingSession>> {
    const response = await apiClient.get<{
      data: PaginatedResponse<SpeakingSession>;
    }>(SPEAKING_SESSIONS.GET_SESSIONS, { params });
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/sessions/status/{status}
   * Lọc phiên theo status (IN_PROGRESS, COMPLETED, CANCELLED)
   */
  static async getSessionsByStatus(
    status: string,
    params?: { page?: number; size?: number }
  ): Promise<PaginatedResponse<SpeakingSession>> {
    const response = await apiClient.get<{
      data: PaginatedResponse<SpeakingSession>;
    }>(SPEAKING_SESSIONS.GET_SESSIONS_BY_STATUS(status), { params });
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/sessions/level/{level}
   * Lọc phiên theo level (A1-C2)
   */
  static async getSessionsByLevel(
    level: string,
    params?: { page?: number; size?: number }
  ): Promise<PaginatedResponse<SpeakingSession>> {
    const response = await apiClient.get<{
      data: PaginatedResponse<SpeakingSession>;
    }>(SPEAKING_SESSIONS.GET_SESSIONS_BY_LEVEL(level), { params });
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/sessions/{sessionId}
   * Lấy chi tiết phiên kèm attempts + feedback
   */
  static async getSessionDetail(
    sessionId: number
  ): Promise<SpeakingSessionSummary> {
    const response = await apiClient.get<{
      data: SpeakingSessionSummary;
    }>(SPEAKING_SESSIONS.GET_SESSION(String(sessionId)));
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/sessions/latest/in-progress
   * Lấy phiên IN_PROGRESS gần nhất
   */
  static async getLatestInProgressSession(): Promise<SpeakingSession | null> {
    try {
      const response = await apiClient.get<{
        data: SpeakingSession;
      }>(SPEAKING_SESSIONS.GET_LATEST_IN_PROGRESS);
      return response.data.data!;
    } catch (error) {
      // 404 nếu không có phiên nào
      return null;
    }
  }

  /**
   * PUT /api/ai/speaking/sessions/{sessionId}/complete
   * Đánh dấu phiên là đã hoàn thành
   */
  static async completeSession(sessionId: number): Promise<SpeakingSession> {
    const response = await apiClient.put<{ data: SpeakingSession }>(
      SPEAKING_SESSIONS.COMPLETE_SESSION(String(sessionId)),
      {}
    );
    return response.data.data!;
  }

  /**
   * PUT /api/ai/speaking/sessions/{sessionId}/cancel
   * Hủy phiên luyện nói
   */
  static async cancelSession(sessionId: number): Promise<SpeakingSession> {
    const response = await apiClient.put<{ data: SpeakingSession }>(
      SPEAKING_SESSIONS.CANCEL_SESSION(String(sessionId)),
      {}
    );
    return response.data.data!;
  }

  /**
   * DELETE /api/ai/speaking/sessions/{sessionId}
   * Xóa phiên (xóa tất cả attempts + feedback)
   */
  static async deleteSession(sessionId: number): Promise<void> {
    await apiClient.delete(SPEAKING_SESSIONS.DELETE_SESSION(String(sessionId)));
  }

  /**
   * GET /api/ai/speaking/sessions/completed/sorted-by-score
   * Lấy danh sách phiên hoàn thành, sắp xếp theo điểm
   */
  static async getCompletedSessionsSortedByScore(params?: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<SpeakingSession>> {
    const response = await apiClient.get<{
      data: PaginatedResponse<SpeakingSession>;
    }>(SPEAKING_SESSIONS.GET_COMPLETED_SORTED_BY_SCORE, { params });
    return response.data.data!;
  }

  // ============================================
  // ATTEMPT SUBMISSION (2 endpoints)
  // ============================================

  /**
   * POST /api/ai/speaking/attempts
   * Gửi bài nói: upload audio + nhận feedback
   * 
   * Params:
   * - sessionId (form)
   * - audioFile (multipart file)
   * 
   * Response: SpeakingPracticeResponse (201 Created)
   */
  static async submitAttempt(
    sessionId: number,
    audioBlob: Blob
  ): Promise<SpeakingPracticeResponse> {
    const formData = new FormData();
    formData.append("sessionId", String(sessionId));
    formData.append("audioFile", audioBlob, "recording.wav");

    const response = await apiClient.post<{
      data: SpeakingPracticeResponse;
    }>(SPEAKING_ATTEMPTS.SUBMIT_ATTEMPT, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/attempts/{sessionId}
   * Lấy thông tin phiên practice (tất cả attempts + statistics)
   */
  static async getPracticeSession(
    sessionId: number
  ): Promise<SpeakingPracticeResponse> {
    const response = await apiClient.get<{
      data: SpeakingPracticeResponse;
    }>(SPEAKING_ATTEMPTS.GET_PRACTICE_SESSION(String(sessionId)));
    return response.data.data!;
  }

  // ============================================
  // FEEDBACK RETRIEVAL (8 endpoints)
  // ============================================

  /**
   * GET /api/ai/speaking/feedback/attempts/{attemptId}
   * Lấy feedback chi tiết của một attempt
   */
  static async getAttemptFeedback(
    attemptId: number
  ): Promise<SpeakingAttemptFeedback> {
    const response = await apiClient.get<{
      data: SpeakingAttemptFeedback;
    }>(SPEAKING_FEEDBACK.GET_ATTEMPT_FEEDBACK(String(attemptId)));
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/feedback/sessions/{sessionId}
   * Lấy feedback tổng hợp của phiên
   */
  static async getSessionFeedback(
    sessionId: number
  ): Promise<SpeakingAttemptFeedback | null> {
    try {
      const response = await apiClient.get<{
        data: SpeakingAttemptFeedback;
      }>(SPEAKING_FEEDBACK.GET_SESSION_FEEDBACK(String(sessionId)));
      return response.data.data!;
    } catch (error) {
      // 404 nếu không có feedback
      return null;
    }
  }

  /**
   * GET /api/ai/speaking/feedback/sessions/{sessionId}/list
   * Lấy danh sách feedback của tất cả attempts trong phiên
   */
  static async getSessionFeedbackList(
    sessionId: number,
    params?: { page?: number; size?: number }
  ): Promise<PaginatedResponse<SpeakingAttemptFeedback>> {
    const response = await apiClient.get<{
      data: PaginatedResponse<SpeakingAttemptFeedback>;
    }>(SPEAKING_FEEDBACK.GET_SESSION_FEEDBACK_LIST(String(sessionId)), {
      params,
    });
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/feedback/sessions/{sessionId}/best
   * Lấy feedback của attempt tốt nhất (điểm cao nhất)
   */
  static async getBestFeedback(
    sessionId: number
  ): Promise<SpeakingAttemptFeedback | null> {
    try {
      const response = await apiClient.get<{
        data: SpeakingAttemptFeedback;
      }>(SPEAKING_FEEDBACK.GET_BEST_FEEDBACK(String(sessionId)));
      return response.data.data!;
    } catch (error) {
      return null;
    }
  }

  /**
   * GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-score
   * Lấy danh sách feedback sắp xếp theo điểm tổng
   */
  static async getFeedbackSortedByScore(
    sessionId: number,
    params?: { page?: number; size?: number }
  ): Promise<PaginatedResponse<SpeakingAttemptFeedback>> {
    const response = await apiClient.get<{
      data: PaginatedResponse<SpeakingAttemptFeedback>;
    }>(SPEAKING_FEEDBACK.GET_FEEDBACK_SORTED_BY_SCORE(String(sessionId)), {
      params,
    });
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-grammar
   * Lấy danh sách feedback sắp xếp theo điểm ngữ pháp
   */
  static async getFeedbackSortedByGrammar(
    sessionId: number,
    params?: { page?: number; size?: number }
  ): Promise<PaginatedResponse<SpeakingAttemptFeedback>> {
    const response = await apiClient.get<{
      data: PaginatedResponse<SpeakingAttemptFeedback>;
    }>(SPEAKING_FEEDBACK.GET_FEEDBACK_SORTED_BY_GRAMMAR(String(sessionId)), {
      params,
    });
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-vocabulary
   * Lấy danh sách feedback sắp xếp theo điểm từ vựng
   */
  static async getFeedbackSortedByVocabulary(
    sessionId: number,
    params?: { page?: number; size?: number }
  ): Promise<PaginatedResponse<SpeakingAttemptFeedback>> {
    const response = await apiClient.get<{
      data: PaginatedResponse<SpeakingAttemptFeedback>;
    }>(
      SPEAKING_FEEDBACK.GET_FEEDBACK_SORTED_BY_VOCABULARY(String(sessionId)),
      { params }
    );
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/feedback/sessions/{sessionId}/statistics
   * Lấy thống kê điểm trung bình của phiên
   */
  static async getSessionStatistics(sessionId: number): Promise<{
    overallScore: number;
    grammarScore: number;
    vocabularyScore: number;
    fluencyScore: number;
    contentScore: number;
    attemptCount: number;
  }> {
    const response = await apiClient.get<{
      data: {
        overallScore: number;
        grammarScore: number;
        vocabularyScore: number;
        fluencyScore: number;
        contentScore: number;
        attemptCount: number;
      };
    }>(SPEAKING_FEEDBACK.GET_SESSION_STATISTICS(String(sessionId)));
    return response.data.data!;
  }

  // ============================================
  // PROGRESS TRACKING (4 endpoints)
  // ============================================

  /**
   * GET /api/ai/speaking/progress
   * Lấy tiến độ tổng hợp của user
   */
  static async getProgress(): Promise<SpeakingProgressResponse> {
    const response = await apiClient.get<{
      data: SpeakingProgressResponse;
    }>(SPEAKING_PROGRESS.GET_PROGRESS);
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/progress/trend/{days}
   * Phân tích xu hướng điểm trong N ngày (7 hoặc 30)
   */
  static async getScoreTrend(days: 7 | 30): Promise<ScoreTrendResponse> {
    const response = await apiClient.get<{
      data: ScoreTrendResponse;
    }>(SPEAKING_PROGRESS.GET_SCORE_TREND(days));
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/progress/milestones
   * Lấy danh sách thành tựu/achievements
   */
  static async getMilestones(): Promise<{
    milestones: string[];
    totalMilestones: number;
  }> {
    const response = await apiClient.get<{
      data: { milestones: string[]; totalMilestones: number };
    }>(SPEAKING_PROGRESS.GET_MILESTONES);
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/progress/summary
   * Lấy tóm tắt tiến độ cho dashboard
   */
  static async getProgressSummary(): Promise<{
    totalSessions: number;
    completedSessions: number;
    totalAttempts: number;
    averageScore: number;
    bestScore: number;
    currentStreak: number;
    scoreTrend: string;
    nextMilestone: string;
  }> {
    const response = await apiClient.get<{
      data: {
        totalSessions: number;
        completedSessions: number;
        totalAttempts: number;
        averageScore: number;
        bestScore: number;
        currentStreak: number;
        scoreTrend: string;
        nextMilestone: string;
      };
    }>(SPEAKING_PROGRESS.GET_PROGRESS_SUMMARY);
    return response.data.data!;
  }

  // ============================================
  // TEXT-TO-SPEECH (3 endpoints)
  // ============================================

  /**
   * POST /api/ai/speaking/tts/word
   * Tạo audio pronunciation cho một từ
   */
  static async generateTtsForWord(
    word: string,
    language: string = "en-US"
  ): Promise<TextToSpeechResponse> {
    const response = await apiClient.post<{
      data: TextToSpeechResponse;
    }>(SPEAKING_TTS.GENERATE_TTS_WORD, { word, language });
    return response.data.data!;
  }

  /**
   * POST /api/ai/speaking/tts/phrase
   * Tạo audio pronunciation cho một cụm từ
   */
  static async generateTtsForPhrase(
    phrase: string,
    language: string = "en-US"
  ): Promise<TextToSpeechResponse> {
    const response = await apiClient.post<{
      data: TextToSpeechResponse;
    }>(SPEAKING_TTS.GENERATE_TTS_PHRASE, { phrase, language });
    return response.data.data!;
  }

  /**
   * GET /api/ai/speaking/tts/sessions/{sessionId}/reference
   * Tạo audio pronunciation cho topic của session
   */
  static async generateTtsForSessionTopic(
    sessionId: number
  ): Promise<TextToSpeechResponse> {
    const response = await apiClient.get<{
      data: TextToSpeechResponse;
    }>(SPEAKING_TTS.GENERATE_TTS_SESSION_REFERENCE(String(sessionId)));
    return response.data.data!;
  }

  // ============================================
  // AUDIO STORAGE (2 endpoints)
  // ============================================

  /**
   * GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber}
   * Download audio file từ attempt
   */
  static async downloadAudio(
    sessionId: number,
    attemptNumber: number
  ): Promise<Blob> {
    const response = await apiClient.get(
      SPEAKING_AUDIO.DOWNLOAD_AUDIO(sessionId, attemptNumber),
      { responseType: "blob" }
    );
    return response.data;
  }

  /**
   * GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber}/stream
   * Stream audio file (supports range requests)
   */
  static async streamAudio(
    sessionId: number,
    attemptNumber: number,
    range?: string
  ): Promise<Blob> {
    const headers: Record<string, string> = {};
    if (range) {
      headers["Range"] = range;
    }

    const response = await apiClient.get(
      SPEAKING_AUDIO.STREAM_AUDIO(sessionId, attemptNumber),
      {
        responseType: "blob",
        headers,
      }
    );
    return response.data;
  }
}

export default SpeakingService;

// Re-export types for convenience
export type {
  SpeakingSession,
  SpeakingSessionSummary,
  SpeakingAttempt,
  CreateSpeakingSessionRequest,
  SpeakingPracticeResponse,
  SpeakingProgressResponse,
};
