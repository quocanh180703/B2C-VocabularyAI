/**
 * Quiz Service
 * 
 * HTTP client for Quiz API endpoints
 * Handles all communication with backend Quiz Controller
 * 
 * Endpoints:
 * - GET /quizzes - Get all published quizzes
 * - GET /quizzes/{id} - Get quiz with questions
 * - POST /quizzes/{quizId}/start - Start quiz attempt
 * - POST /quizzes/{attemptId}/submit - Submit quiz answers
 * - GET /quizzes/attempts/{attemptId} - Get attempt details
 * - GET /quizzes/history - Get user's quiz history
 */

import axiosClient from '@/config/axiosClient';
import type {
  QuizResponse,
  QuizWithQuestionsResponse,
  QuizAttemptResponse,
  QuizAttemptWithAnswersResponse,
  SubmitQuizRequest,
  PaginatedResponse,
  ApiResponse,
} from '@/types/quiz';

const API_BASE = '/quizzes';

/**
 * Quiz Service - Singleton
 * 
 * All methods return unwrapped data (extracts from ApiResponse<T>)
 */
class QuizService {
  /**
   * Get all published quizzes with pagination
   * 
   * GET /quizzes?page=0&size=20
   * 
   * @param page - Page number (0-indexed)
   * @param size - Items per page (default 20)
   * @returns Paginated quiz list
   * @throws Error if request fails
   */
  async getQuizzes(page: number = 0, size: number = 20): Promise<PaginatedResponse<QuizResponse>> {
    try {
      const response = await axiosClient.get<ApiResponse<PaginatedResponse<QuizResponse>>>(
        `${API_BASE}`,
        {
          params: { page, size },
        }
      );
      return response.data.data;
    } catch (error) {
      console.error('Error fetching quizzes:', error);
      throw error;
    }
  }

  /**
   * Get quiz details with questions (for taking quiz)
   * 
   * GET /quizzes/{quizId}
   * 
   * Returns questions with correctAnswer = null and explanation = null
   * 
   * @param quizId - Quiz ID
   * @returns Quiz with questions
   * @throws Error if quiz not found or not published
   */
  async getQuizDetail(quizId: number): Promise<QuizWithQuestionsResponse> {
    try {
      const response = await axiosClient.get<ApiResponse<QuizWithQuestionsResponse>>(
        `${API_BASE}/${quizId}`
      );
      return response.data.data;
    } catch (error) {
      console.error(`Error fetching quiz ${quizId}:`, error);
      throw error;
    }
  }

  /**
   * Start a new quiz attempt
   * 
   * POST /quizzes/{quizId}/start
   * 
   * Creates a new QuizAttempt in backend
   * Returns attemptId that must be used for subsequent operations
   * 
   * @param quizId - Quiz ID to start
   * @returns Quiz attempt with ID
   * @throws Error if quiz not available
   */
  async startQuiz(quizId: number): Promise<QuizAttemptResponse> {
    try {
      const response = await axiosClient.post<ApiResponse<QuizAttemptResponse>>(
        `${API_BASE}/${quizId}/start`,
        {} // No request body required
      );
      return response.data.data;
    } catch (error) {
      console.error(`Error starting quiz ${quizId}:`, error);
      throw error;
    }
  }

  /**
   * Submit quiz answers and get score
   * 
   * POST /quizzes/{attemptId}/submit
   * 
   * Backend:
   * - Validates attempt is not already completed
   * - Compares each answer with correct answer
   * - Calculates score: (correctCount / totalQuestions) * 100
   * - Saves QuizAnswers with isCorrect flag
   * - Updates attempt with score, correctAnswers, submittedAt
   * 
   * @param attemptId - Attempt ID (from startQuiz)
   * @param answers - User's answers for all questions
   * @returns Updated attempt with calculated score
   * @throws Error if attempt already submitted or invalid
   */
  async submitQuiz(attemptId: number, answers: SubmitQuizRequest): Promise<QuizAttemptResponse> {
    try {
      const response = await axiosClient.post<ApiResponse<QuizAttemptResponse>>(
        `${API_BASE}/${attemptId}/submit`,
        answers
      );
      return response.data.data;
    } catch (error) {
      console.error(`Error submitting quiz attempt ${attemptId}:`, error);
      throw error;
    }
  }

  /**
   * Get quiz attempt details with answer review
   * 
   * GET /quizzes/attempts/{attemptId}
   * 
   * Returns:
   * - Attempt with score, correctAnswers, timeTaken
   * - Answers with correctAnswer and explanation shown
   * 
   * IDOR protected: Only user who owns attempt can view
   * 
   * @param attemptId - Attempt ID
   * @returns Attempt with all answers and explanations
   * @throws Error if attempt not found or not completed or IDOR violation
   */
  async getAttemptDetails(attemptId: number): Promise<QuizAttemptWithAnswersResponse> {
    try {
      const response = await axiosClient.get<ApiResponse<QuizAttemptWithAnswersResponse>>(
        `${API_BASE}/attempts/${attemptId}`
      );
      return response.data.data;
    } catch (error) {
      console.error(`Error fetching attempt ${attemptId}:`, error);
      throw error;
    }
  }

  /**
   * Get user's quiz history (all attempts)
   * 
   * GET /quizzes/history?page=0&size=20
   * 
   * Returns paginated list of all quiz attempts by current user
   * Sorted by createdAt DESC (most recent first)
   * 
   * @param page - Page number (0-indexed)
   * @param size - Items per page (default 20)
   * @returns Paginated attempt history
   * @throws Error if request fails
   */
  async getQuizHistory(
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<QuizAttemptResponse>> {
    try {
      const response = await axiosClient.get<ApiResponse<PaginatedResponse<QuizAttemptResponse>>>(
        `${API_BASE}/history`,
        {
          params: { page, size },
        }
      );
      return response.data.data;
    } catch (error) {
      console.error('Error fetching quiz history:', error);
      throw error;
    }
  }
}

/**
 * Singleton instance of QuizService
 * Use: import { quizService } from '@/services/quizService'
 */
export const quizService = new QuizService();

export default quizService;
