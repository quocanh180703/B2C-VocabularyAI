/**
 * Learning Service
 * 
 * Handles all API calls for the Learning & Flashcard module
 * Manages spaced repetition, review queue, learning progress, and statistics
 * 
 * All methods require JWT authentication (Bearer token)
 */

import { apiClient } from './api/client';
import { PaginatedResponse } from './api/types';

/**
 * Learning Progress Response DTO
 * Represents a user's progress on a single word
 */
export interface LearningProgressResponse {
  id: number;
  word: {
    id: number;
    english: string;
    vietnamese: string;
    pronunciation: string;
    partOfSpeech: 'NOUN' | 'VERB' | 'ADJECTIVE' | 'ADVERB' | 'PREPOSITION';
    definition: string;
    exampleSentence?: string;
    imageUrl?: string;
    audioUrl?: string;
    topicId?: number;
    proficiencyLevel?: 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';
    createdAt: string;
    updatedAt: string;
  };
  status: 'NEW' | 'LEARNING' | 'MASTERED';
  correctCount: number;
  wrongCount: number;
  reviewCount: number;
  accuracy: number; // 0-100
  needsReview: boolean;
  lastReviewedAt?: string; // ISO datetime
  nextReviewAt?: string; // ISO datetime (null when MASTERED)
  createdAt: string;
  updatedAt: string;
}

/**
 * Learning History Response DTO
 * Represents a single learning activity (learn, review, quiz, speaking)
 */
export interface LearningHistoryResponse {
  id: number;
  word: {
    id: number;
    english: string;
    vietnamese: string;
    pronunciation: string;
    partOfSpeech: 'NOUN' | 'VERB' | 'ADJECTIVE' | 'ADVERB' | 'PREPOSITION';
    definition: string;
    exampleSentence?: string;
    imageUrl?: string;
    audioUrl?: string;
    topicId?: number;
    proficiencyLevel?: 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';
    createdAt: string;
    updatedAt: string;
  };
  activityType: 'LEARN' | 'REVIEW' | 'QUIZ' | 'SPEAKING';
  isCorrect?: boolean; // null for LEARN activity
  createdAt: string;
}

/**
 * Progress Statistics Response DTO
 * Summary statistics about user's learning progress
 */
export interface ProgressStatisticsResponse {
  totalWords: number;
  newWords: number;
  learningWords: number;
  masteredWords: number;
  wordsNeedingReview: number;
  totalActivities: number;
  correctAnswers: number;
  overallAccuracyRate: number;
  learningPercentage: number; // Calculated: (learningWords + masteredWords) / totalWords * 100
  masteryPercentage: number; // Calculated: masteredWords / totalWords * 100
}

/**
 * Review Word Request DTO
 * Request body for reviewing a word
 */
export interface ReviewWordRequest {
  correct: boolean; // true if user answered correctly, false otherwise
}

/**
 * Learning Service Class
 * Singleton for all learning-related API operations
 */
class LearningService {
  private baseURL = '/learning';

  /**
   * Start learning a word
   * 
   * POST /learning/words/{wordId}/start
   * 
   * Creates initial learning progress record for a word
   * Returns LearningProgressResponse with status='LEARNING' and nextReviewAt=+1 day
   * 
   * @param wordId - ID of word to start learning
   * @returns Promise<LearningProgressResponse>
   * @throws Error if word doesn't exist or already learning
   */
  async startLearningWord(wordId: number): Promise<LearningProgressResponse> {
    try {
      const response = await apiClient.post<LearningProgressResponse>(
        `${this.baseURL}/words/${wordId}/start`,
        {} // Empty body
      );
      return response.data.data;
    } catch (error) {
      console.error(`Failed to start learning word ${wordId}:`, error);
      throw error;
    }
  }

  /**
   * Review a word (mark correct or incorrect)
   * 
   * POST /learning/words/{wordId}/review
   * 
   * Records a review attempt and updates learning progress
   * Backend automatically:
   * - Updates reviewCount, correctCount/wrongCount
   * - Recalculates accuracy percentage
   * - Determines next review date using Spaced Repetition System (SRS)
   * - Updates status to 'MASTERED' if accuracy >= 80% AND reviewCount >= 5
   * 
   * Spaced Repetition Intervals:
   * - After review 0 (first attempt): next review in 1 day
   * - After review 1: next review in 3 days
   * - After review 2: next review in 7 days
   * - After review 3: next review in 14 days
   * - After review 4+: next review in 30 days
   * - After mastery (5+ reviews with 80%+ accuracy): nextReviewAt = null
   * 
   * CRITICAL: This endpoint has no rate limiting on backend
   * Frontend MUST implement duplicate submission prevention:
   * - Disable button immediately on click
   * - Use mutation lock to prevent simultaneous requests
   * - Only allow 1 request per user action
   * 
   * @param wordId - ID of word being reviewed
   * @param isCorrect - true if user answered correctly
   * @returns Promise<LearningProgressResponse> - Updated progress including nextReviewAt
   * @throws Error if word not found or not learning yet
   */
  async reviewWord(wordId: number, isCorrect: boolean): Promise<LearningProgressResponse> {
    try {
      const request: ReviewWordRequest = { correct: isCorrect };
      const response = await apiClient.post<LearningProgressResponse>(
        `${this.baseURL}/words/${wordId}/review`,
        request
      );
      return response.data.data;
    } catch (error) {
      console.error(`Failed to review word ${wordId}:`, error);
      throw error;
    }
  }

  /**
   * Get user's learning progress (all words)
   * 
   * GET /learning/progress?page=0&size=20
   * 
   * Retrieves paginated list of all words user has started learning
   * Sorted by createdAt descending (newest first)
   * 
   * @param page - Page number (0-indexed, default: 0)
   * @param size - Items per page (default: 20, max: 100)
   * @returns Promise<PaginatedResponse<LearningProgressResponse>>
   */
  async getUserProgress(
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<LearningProgressResponse>> {
    try {
      const params = { page, size };
      const response = await apiClient.get<PaginatedResponse<LearningProgressResponse>>(
        `${this.baseURL}/progress`,
        { params }
      );
      return response.data.data;
    } catch (error) {
      console.error('Failed to get user progress:', error);
      throw error;
    }
  }

  /**
   * Get words needing review (Spaced Repetition Queue)
   * 
   * GET /learning/review-queue?page=0&size=20
   * 
   * Retrieves words that are due for review based on Spaced Repetition System
   * Only includes words where:
   * - nextReviewAt is NOT null
   * - nextReviewAt <= current time (overdue for review)
   * 
   * Sorted by nextReviewAt ascending (due soonest first)
   * This is the main source for the Review Queue / Flashcard feature
   * 
   * @param page - Page number (0-indexed, default: 0)
   * @param size - Items per page (default: 20, max: 100)
   * @returns Promise<PaginatedResponse<LearningProgressResponse>>
   */
  async getReviewQueue(
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<LearningProgressResponse>> {
    try {
      const params = { page, size };
      const response = await apiClient.get<PaginatedResponse<LearningProgressResponse>>(
        `${this.baseURL}/review-queue`,
        { params }
      );
      return response.data.data;
    } catch (error) {
      console.error('Failed to get review queue:', error);
      throw error;
    }
  }

  /**
   * Get learning history (Activity Log)
   * 
   * GET /learning/history?page=0&size=20
   * 
   * Retrieves detailed audit log of all learning activities
   * Includes LEARN, REVIEW, QUIZ, SPEAKING activities
   * Sorted by createdAt descending (newest first)
   * 
   * Activity Types:
   * - LEARN: User started learning a word (isCorrect = null)
   * - REVIEW: User reviewed a word (isCorrect = true/false)
   * - QUIZ: Word appeared in quiz (Phase 9+, isCorrect = true/false)
   * - SPEAKING: Speaking practice activity (Phase 10+, isCorrect = null/true/false)
   * 
   * @param page - Page number (0-indexed, default: 0)
   * @param size - Items per page (default: 20, max: 100)
   * @returns Promise<PaginatedResponse<LearningHistoryResponse>>
   */
  async getLearningHistory(
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<LearningHistoryResponse>> {
    try {
      const params = { page, size };
      const response = await apiClient.get<PaginatedResponse<LearningHistoryResponse>>(
        `${this.baseURL}/history`,
        { params }
      );
      return response.data.data;
    } catch (error) {
      console.error('Failed to get learning history:', error);
      throw error;
    }
  }

  /**
   * Get progress statistics
   * 
   * GET /learning/statistics
   * 
   * Retrieves summary statistics about user's learning progress
   * Computed on-demand (not cached) from database aggregations
   * Includes totals, percentages, and calculated metrics
   * 
   * Statistics Breakdown:
   * - totalWords: Total unique words user has started learning
   * - newWords: Words with status='NEW' (never reviewed)
   * - learningWords: Words with status='LEARNING'
   * - masteredWords: Words with status='MASTERED'
   * - wordsNeedingReview: Words where nextReviewAt <= now
   * - totalActivities: Count of all LEARN/REVIEW/QUIZ/SPEAKING activities
   * - correctAnswers: Count of activities where isCorrect=true
   * - overallAccuracyRate: (correctAnswers / totalActivities) * 100
   * - learningPercentage: ((learningWords + masteredWords) / totalWords) * 100
   * - masteryPercentage: (masteredWords / totalWords) * 100
   * 
   * Note: Backend computes all values. Frontend should NOT calculate these.
   * 
   * @returns Promise<ProgressStatisticsResponse> - Single object, not paginated
   */
  async getProgressStatistics(): Promise<ProgressStatisticsResponse> {
    try {
      const response = await apiClient.get<ProgressStatisticsResponse>(
        `${this.baseURL}/statistics`
      );
      return response.data.data;
    } catch (error) {
      console.error('Failed to get progress statistics:', error);
      throw error;
    }
  }
}

/**
 * Singleton instance of LearningService
 * Use this for all learning-related API calls
 * 
 * Example:
 * ```typescript
 * import { learningService } from '@/services/learningService';
 * 
 * // Start learning a word
 * const progress = await learningService.startLearningWord(123);
 * 
 * // Review the word
 * const updated = await learningService.reviewWord(123, true);
 * 
 * // Get review queue
 * const queue = await learningService.getReviewQueue(0, 20);
 * 
 * // Get statistics
 * const stats = await learningService.getProgressStatistics();
 * ```
 */
export const learningService = new LearningService();

export default learningService;
