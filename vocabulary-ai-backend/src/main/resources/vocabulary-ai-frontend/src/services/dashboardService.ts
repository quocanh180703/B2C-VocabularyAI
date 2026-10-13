import { apiClient } from './api/client';
import {
  UserResponse,
  ProgressStatisticsResponse,
  LearningProgressResponse,
  FavoriteWordResponse,
  QuizAttemptResponse,
  RecommendationResponse,
} from './api/types';

/**
 * Dashboard Service
 * 
 * Handles all API calls for dashboard data
 * All methods use the authenticated API client (JWT token automatically included)
 */

// ==================== USER DATA ====================

/**
 * Get current authenticated user's profile
 */
export async function getCurrentUser(): Promise<UserResponse> {
  const response = await apiClient.get<{ data: UserResponse }>('/users/me');
  if (!response.data?.data) {
    throw new Error('Invalid user response format');
  }
  return response.data.data;
}

// ==================== LEARNING STATISTICS ====================

/**
 * Get user's learning progress statistics
 * Includes: total words learned, streak, daily goal progress, accuracy, etc.
 */
export async function getLearningStatistics(): Promise<ProgressStatisticsResponse> {
  const response = await apiClient.get<{ data: ProgressStatisticsResponse }>(
    '/learning/statistics'
  );
  if (!response.data?.data) {
    throw new Error('Invalid statistics response format');
  }
  return response.data.data;
}

// ==================== REVIEW QUEUE ====================

/**
 * Get words needing review (due for spaced repetition)
 * Returns paginated results with most urgent reviews first
 */
export async function getReviewQueue(
  page: number = 0,
  size: number = 1
): Promise<{
  content: LearningProgressResponse[];
  totalElements: number;
  totalPages: number;
  empty: boolean;
}> {
  const response = await apiClient.get<{
    data: {
      content: LearningProgressResponse[];
      totalElements: number;
      totalPages: number;
      empty: boolean;
    };
  }>('/learning/review-queue', {
    params: { page, size },
  });
  if (!response.data?.data) {
    throw new Error('Invalid review queue response format');
  }
  return response.data.data;
}

// ==================== FAVORITE WORDS ====================

/**
 * Get user's favorite words
 * Returns most recent favorites first
 */
export async function getFavoriteWords(
  page: number = 0,
  size: number = 3
): Promise<{
  content: FavoriteWordResponse[];
  totalElements: number;
  totalPages: number;
  empty: boolean;
}> {
  const response = await apiClient.get<{
    data: {
      content: FavoriteWordResponse[];
      totalElements: number;
      totalPages: number;
      empty: boolean;
    };
  }>('/words/favorites', {
    params: { page, size },
  });
  if (!response.data?.data) {
    throw new Error('Invalid favorites response format');
  }
  return response.data.data;
}

// ==================== QUIZ HISTORY ====================

/**
 * Get user's recent quiz attempts
 * Returns most recent attempts first
 */
export async function getQuizHistory(
  page: number = 0,
  size: number = 1
): Promise<{
  content: QuizAttemptResponse[];
  totalElements: number;
  totalPages: number;
  empty: boolean;
}> {
  const response = await apiClient.get<{
    data: {
      content: QuizAttemptResponse[];
      totalElements: number;
      totalPages: number;
      empty: boolean;
    };
  }>('/quizzes/history', {
    params: { page, size },
  });
  if (!response.data?.data) {
    throw new Error('Invalid quiz history response format');
  }
  return response.data.data;
}

// ==================== PERSONALIZED DATA ====================

/**
 * Get AI-generated personalized recommendations
 * Note: This endpoint might be slower due to AI processing
 * Consider caching the result
 */
export async function getRecommendations(): Promise<RecommendationResponse> {
  const response = await apiClient.get<{ data: RecommendationResponse }>(
    '/ai/personalized/recommendations'
  );
  if (!response.data?.data) {
    throw new Error('Invalid recommendations response format');
  }
  return response.data.data;
}

// ==================== COMBINED DASHBOARD DATA ====================

/**
 * Fetch all dashboard data in parallel
 * This is the main function used by the dashboard component
 * 
 * Returns: Combined dashboard data from multiple APIs
 */
export interface DashboardData {
  user: UserResponse;
  statistics: ProgressStatisticsResponse;
  reviewQueue: {
    content: LearningProgressResponse[];
    totalElements: number;
  };
  favoriteWords: {
    content: FavoriteWordResponse[];
    totalElements: number;
  };
  quizHistory: {
    content: QuizAttemptResponse[];
    totalElements: number;
  };
  recommendations: RecommendationResponse;
}

export async function getDashboardData(): Promise<DashboardData> {
  try {
    // Fetch non-AI data in parallel (fast endpoints)
    const [user, statistics, reviewQueueResponse, favoriteWordsResponse, quizHistoryResponse] =
      await Promise.all([
        getCurrentUser().catch(err => {
          console.error('Failed to fetch user data:', err);
          throw new Error('Could not load your profile. Please try again.');
        }),
        getLearningStatistics().catch(err => {
          console.error('Failed to fetch statistics:', err);
          throw new Error('Could not load your learning statistics. Please try again.');
        }),
        getReviewQueue(0, 1).catch(err => {
          console.error('Failed to fetch review queue:', err);
          throw new Error('Could not load your review queue. Please try again.');
        }),
        getFavoriteWords(0, 3).catch(err => {
          console.error('Failed to fetch favorite words:', err);
          throw new Error('Could not load your favorite words. Please try again.');
        }),
        getQuizHistory(0, 1).catch(err => {
          console.error('Failed to fetch quiz history:', err);
          throw new Error('Could not load your quiz history. Please try again.');
        }),
      ]);

    // Fallback recommendation - don't fetch in background
    const fallbackRecommendation: RecommendationResponse = {
      id: 0,
      title: 'Continue Learning',
      description: 'Keep up with your daily learning goal',
      priority: 'HIGH',
      actionItems: [],
      expectedImpact: 0,
      timeRequired: 0,
      tags: [],
    };

    // Return immediately with fallback recommendations
    return {
      user,
      statistics,
      reviewQueue: {
        content: reviewQueueResponse.content,
        totalElements: reviewQueueResponse.totalElements,
      },
      favoriteWords: {
        content: favoriteWordsResponse.content,
        totalElements: favoriteWordsResponse.totalElements,
      },
      quizHistory: {
        content: quizHistoryResponse.content,
        totalElements: quizHistoryResponse.totalElements,
      },
      recommendations: fallbackRecommendation,
    };
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error);
    throw error;
  }
}
