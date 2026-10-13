/**
 * useLearning Hook
 * 
 * Complete React hook for Learning & Flashcard module
 * Manages queries, mutations, pagination, and state using React Query (TanStack Query)
 * 
 * Features:
 * - Queries: progress, review-queue, history, statistics
 * - Mutations: startLearning, reviewWord
 * - Pagination: load more for progress, review queue, history
 * - Cache invalidation: automatic after mutations
 * - Error handling: per-query error state
 * - Duplicate prevention: mutation lock for review endpoint
 * 
 * Usage:
 * ```tsx
 * function LearningPage() {
 *   const {
 *     progress,
 *     progressLoading,
 *     reviewQueue,
 *     reviewWord,
 *     statistics,
 *     refetch,
 *   } = useLearning();
 *
 *   return (
 *     <div>
 *       <Statistics stats={statistics} loading={statsLoading} />
 *       <ReviewForm onReview={reviewWord} />
 *     </div>
 *   );
 * }
 * ```
 */

import { useState, useCallback, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { learningService } from '@/services/learningService';
import {
  LearningProgressResponse,
  LearningHistoryResponse,
  ProgressStatisticsResponse,
  PaginatedResponse,
  UseLearningReturn,
} from '@/types/learning';
import { STALE_TIME } from '@/constants/learning';

// ============================================
// Query Key Factory
// ============================================

/**
 * Query keys for React Query
 * Organized by feature for easy cache invalidation
 */
const QUERY_KEYS = {
  /** Progress queries */
  progress: ['learning', 'progress'] as const,
  progressPage: (page: number) => [...QUERY_KEYS.progress, page] as const,

  /** Review queue queries */
  reviewQueue: ['learning', 'review-queue'] as const,
  reviewQueuePage: (page: number) => [...QUERY_KEYS.reviewQueue, page] as const,

  /** History queries */
  history: ['learning', 'history'] as const,
  historyPage: (page: number) => [...QUERY_KEYS.history, page] as const,

  /** Statistics queries */
  statistics: ['learning', 'statistics'] as const,
} as const;

// ============================================
// Main Hook Implementation
// ============================================

/**
 * useLearning Hook
 * 
 * Primary hook for all Learning module functionality
 * Manages queries, mutations, pagination, and state
 * 
 * @returns UseLearningReturn - Complete learning state and methods
 */
export function useLearning(): UseLearningReturn {
  // ==================== State Management ====================

  const queryClient = useQueryClient();

  // Pagination state
  const [progressPage, setProgressPage] = useState(0);
  const [reviewQueuePage, setReviewQueuePage] = useState(0);
  const [historyPage, setHistoryPage] = useState(0);

  // Duplicate submission prevention
  // CRITICAL: Prevents 1 user action → 2 API requests
  const reviewMutationInProgress = useRef(false);

  // ==================== Progress Query ====================

  /**
   * Query: Get user's learning progress
   * 
   * Fetches paginated list of all words user has started learning
   * Sorted by createdAt descending (newest first)
   * Stale time: 30 seconds (refresh after 30s of inactivity)
   */
  const {
    data: progressData,
    isLoading: progressLoading,
    error: progressError,
    isFetching: progressFetching,
  } = useQuery({
    queryKey: QUERY_KEYS.progressPage(progressPage),
    queryFn: () => learningService.getUserProgress(progressPage, 20),
    staleTime: STALE_TIME.PROGRESS,
    retry: 2,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });

  // ==================== Review Queue Query ====================

  /**
   * Query: Get words needing review (SRS queue)
   * 
   * Fetches words that are due for review based on Spaced Repetition System
   * Only includes words where nextReviewAt <= now
   * Sorted by nextReviewAt ascending (due soonest first)
   * Stale time: 30 seconds
   */
  const {
    data: reviewQueueData,
    isLoading: reviewQueueLoading,
    error: reviewQueueError,
    isFetching: reviewQueueFetching,
  } = useQuery({
    queryKey: QUERY_KEYS.reviewQueuePage(reviewQueuePage),
    queryFn: () => learningService.getReviewQueue(reviewQueuePage, 20),
    staleTime: STALE_TIME.REVIEW_QUEUE,
    retry: 2,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });

  // ==================== History Query ====================

  /**
   * Query: Get learning history (activity log)
   * 
   * Fetches detailed audit log of all learning activities
   * Includes LEARN, REVIEW, QUIZ, SPEAKING activities
   * Sorted by createdAt descending (newest first)
   * Stale time: 30 seconds
   */
  const {
    data: historyData,
    isLoading: historyLoading,
    error: historyError,
    isFetching: historyFetching,
  } = useQuery({
    queryKey: QUERY_KEYS.historyPage(historyPage),
    queryFn: () => learningService.getLearningHistory(historyPage, 20),
    staleTime: STALE_TIME.HISTORY,
    retry: 2,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });

  // ==================== Statistics Query ====================

  /**
   * Query: Get progress statistics
   * 
   * Fetches summary statistics about user's learning progress
   * Backend-computed (not cached, computed on-demand)
   * Single object (not paginated)
   * Stale time: 60 seconds (higher stale time for on-demand computation)
   */
  const {
    data: statistics,
    isLoading: statsLoading,
    error: statsError,
    isFetching: statsFetching,
  } = useQuery({
    queryKey: QUERY_KEYS.statistics,
    queryFn: () => learningService.getProgressStatistics(),
    staleTime: STALE_TIME.STATISTICS,
    retry: 2,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });

  // ==================== Start Learning Mutation ====================

  /**
   * Mutation: Start learning a word
   * 
   * Creates initial learning progress for a new word
   * Side effects:
   * - Invalidates progress query (refetch on next use)
   * - Invalidates statistics query (refetch on next use)
   */
  const { mutateAsync: startLearning, isPending: startingWord } = useMutation({
    mutationFn: (wordId: number) => learningService.startLearningWord(wordId),
    onSuccess: () => {
      // Invalidate related queries to refetch from backend
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.progress });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.statistics });
    },
    onError: (error) => {
      console.error('Failed to start learning word:', error);
    },
  });

  // ==================== Review Word Mutation ====================

  /**
   * Mutation: Review a word (mark correct or incorrect)
   * 
   * Records a review attempt and updates learning progress
   * Backend automatically:
   * - Updates reviewCount, correctCount/wrongCount
   * - Recalculates accuracy
   * - Updates nextReviewAt using SRS algorithm
   * - Updates status to MASTERED if criteria met
   * 
   * CRITICAL: Duplicate submission prevention
   * - Uses useRef to track if mutation is in progress
   * - Prevents simultaneous API requests
   * - Frontend button disable alone is not enough
   * 
   * Side effects:
   * - Invalidates all progress-related queries
   * - Causes automatic refetch on next use
   * - NO manual state updates (use cache invalidation instead)
   */
  const { mutateAsync: reviewWord, isPending: reviewingWord } = useMutation({
    mutationFn: async (params: { wordId: number; isCorrect: boolean }) => {
      // DUPLICATE PREVENTION: Check if already in progress
      if (reviewMutationInProgress.current) {
        throw new Error('Review already in progress. Please wait.');
      }

      reviewMutationInProgress.current = true;
      try {
        const result = await learningService.reviewWord(params.wordId, params.isCorrect);
        return result;
      } finally {
        reviewMutationInProgress.current = false;
      }
    },
    onSuccess: () => {
      // Invalidate all related caches to ensure fresh data
      // Frontend will refetch on next use automatically
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.progress });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.reviewQueue });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.history });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.statistics });
    },
    onError: (error) => {
      console.error('Failed to review word:', error);
    },
  });

  // ==================== Pagination Helpers ====================

  /**
   * Load more progress items
   * Increments page number and queries next batch
   */
  const loadMoreProgress = useCallback(() => {
    setProgressPage((prev) => prev + 1);
  }, []);

  /**
   * Load more review queue items
   * Increments page number and queries next batch
   */
  const loadMoreReviewQueue = useCallback(() => {
    setReviewQueuePage((prev) => prev + 1);
  }, []);

  /**
   * Load more history items
   * Increments page number and queries next batch
   */
  const loadMoreHistory = useCallback(() => {
    setHistoryPage((prev) => prev + 1);
  }, []);

  // ==================== Manual Refetch ====================

  /**
   * Manually refetch all queries
   * Invalidates all caches and triggers fresh fetch from backend
   * Useful for manual "Refresh" button
   */
  const refetch = useCallback(async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.progress }),
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.reviewQueue }),
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.history }),
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.statistics }),
    ]);
  }, [queryClient]);

  // ==================== Extract Pagination Info ====================

  /**
   * Get total pages for progress
   */
  const progressTotalPages = progressData?.totalPages ?? 0;

  /**
   * Get total pages for review queue
   */
  const reviewQueueTotalPages = reviewQueueData?.totalPages ?? 0;

  /**
   * Get total pages for history
   */
  const historyTotalPages = historyData?.totalPages ?? 0;

  // ==================== Return Hook Value ====================

  return {
    // ==================== Progress State ====================
    progress: progressData?.content ?? [],
    progressLoading: progressLoading || progressFetching,
    progressError: progressError as Error | undefined,

    // ==================== Review Queue State ====================
    reviewQueue: reviewQueueData?.content ?? [],
    reviewQueueLoading: reviewQueueLoading || reviewQueueFetching,
    reviewQueueError: reviewQueueError as Error | undefined,

    // ==================== History State ====================
    history: historyData?.content ?? [],
    historyLoading: historyLoading || historyFetching,
    historyError: historyError as Error | undefined,

    // ==================== Statistics State ====================
    statistics: statistics ?? null,
    statsLoading: statsLoading || statsFetching,
    statsError: statsError as Error | undefined,

    // ==================== Mutations ====================
    startLearning,
    reviewWord: (wordId: number, isCorrect: boolean) =>
      reviewWord({ wordId, isCorrect }),

    // ==================== Pagination Helpers ====================
    loadMoreProgress,
    loadMoreReviewQueue,
    loadMoreHistory,

    // ==================== Manual Refetch ====================
    refetch,

    // ==================== Pagination Info (Optional) ====================
    progressPage,
    reviewQueuePage,
    historyPage,
    progressTotalPages,
    reviewQueueTotalPages,
    historyTotalPages,
  };
}

// ============================================
// Helper Hooks (Optional, for advanced usage)
// ============================================

/**
 * useReviewWord Hook
 * 
 * Simplified hook for just the review word mutation
 * Use this if you only need review functionality in a component
 * 
 * Example:
 * ```tsx
 * const { reviewWord, isReviewing } = useReviewWord();
 * 
 * const handleReview = async (wordId: number, correct: boolean) => {
 *   await reviewWord(wordId, correct);
 * };
 * ```
 */
export function useReviewWord() {
  const queryClient = useQueryClient();
  const reviewMutationInProgress = useRef(false);

  const { mutateAsync: reviewWord, isPending: isReviewing } = useMutation({
    mutationFn: async (params: { wordId: number; isCorrect: boolean }) => {
      if (reviewMutationInProgress.current) {
        throw new Error('Review already in progress. Please wait.');
      }

      reviewMutationInProgress.current = true;
      try {
        return await learningService.reviewWord(params.wordId, params.isCorrect);
      } finally {
        reviewMutationInProgress.current = false;
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.progress });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.reviewQueue });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.statistics });
    },
  });

  return {
    reviewWord: (wordId: number, isCorrect: boolean) =>
      reviewWord({ wordId, isCorrect }),
    isReviewing,
  };
}

/**
 * useStartLearning Hook
 * 
 * Simplified hook for just the start learning mutation
 * Use this if you only need to start learning in a component
 */
export function useStartLearning() {
  const queryClient = useQueryClient();

  const { mutateAsync: startLearning, isPending: isStarting } = useMutation({
    mutationFn: (wordId: number) => learningService.startLearningWord(wordId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.progress });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.statistics });
    },
  });

  return {
    startLearning,
    isStarting,
  };
}

/**
 * useProgressStatistics Hook
 * 
 * Simplified hook for just the statistics query
 * Use this if you only need statistics in a component
 */
export function useProgressStatistics() {
  const {
    data: statistics,
    isLoading,
    error,
  } = useQuery({
    queryKey: QUERY_KEYS.statistics,
    queryFn: () => learningService.getProgressStatistics(),
    staleTime: STALE_TIME.STATISTICS,
    retry: 2,
  });

  return {
    statistics: statistics ?? null,
    isLoading,
    error: error as Error | undefined,
  };
}

/**
 * useReviewQueue Hook
 * 
 * Simplified hook for just the review queue query
 * Use this if you only need the review queue in a component
 */
export function useReviewQueue(pageSize: number = 20) {
  const [page, setPage] = useState(0);

  const {
    data,
    isLoading,
    error,
  } = useQuery({
    queryKey: QUERY_KEYS.reviewQueuePage(page),
    queryFn: () => learningService.getReviewQueue(page, pageSize),
    staleTime: STALE_TIME.REVIEW_QUEUE,
    retry: 2,
  });

  const loadMore = useCallback(() => {
    setPage((prev) => prev + 1);
  }, []);

  return {
    words: data?.content ?? [],
    totalPages: data?.totalPages ?? 0,
    currentPage: page,
    isLoading,
    error: error as Error | undefined,
    loadMore,
  };
}

export default useLearning;
