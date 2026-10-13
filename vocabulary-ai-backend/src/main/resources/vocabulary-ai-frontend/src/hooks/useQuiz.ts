/**
 * useQuiz Hook
 * 
 * React Query hook for managing Quiz operations
 * Handles queries, mutations, pagination, and cache management
 * 
 * Features:
 * - 3 queries: quizzes, currentQuiz, quizHistory
 * - 2 mutations: startQuiz, submitQuiz
 * - Automatic cache invalidation after mutations
 * - Duplicate submission prevention
 * - Loading and error states
 * - Pagination helpers
 */

import { useState, useCallback, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { quizService } from '@/services/quizService';
import type {
  QuizResponse,
  QuizWithQuestionsResponse,
  QuizAttemptResponse,
  QuizAttemptWithAnswersResponse,
  SubmitQuizRequest,
  UseQuizReturn,
} from '@/types/quiz';
import { QUIZ_QUERY_KEYS } from '@/types/quiz';

// ============================================
// CONSTANTS
// ============================================

const QUIZ_STALE_TIME = 30 * 1000; // 30 seconds
const QUIZ_CACHE_TIME = 5 * 60 * 1000; // 5 minutes

// ============================================
// HOOK IMPLEMENTATION
// ============================================

/**
 * useQuiz Hook
 * 
 * Manages all Quiz-related queries and mutations with React Query
 * 
 * @returns {UseQuizReturn} Quiz state and operations
 * 
 * @example
 * ```tsx
 * function QuizPage() {
 *   const { quizzes, quizzesLoading, startQuiz } = useQuiz();
 *   
 *   return (
 *     <div>
 *       {quizzesLoading && <Skeleton />}
 *       {quizzes.map(quiz => (
 *         <button onClick={() => startQuiz(quiz.id)}>
 *           {quiz.title}
 *         </button>
 *       ))}
 *     </div>
 *   );
 * }
 * ```
 */
export function useQuiz(): UseQuizReturn {
  const queryClient = useQueryClient();

  // ============================================
  // STATE MANAGEMENT
  // ============================================

  // Quiz list pagination
  const [quizzesPage, setQuizzesPage] = useState(0);
  const quizzesPageSize = 20;

  // Quiz history pagination
  const [historyPage, setHistoryPage] = useState(0);
  const historyPageSize = 20;

  // Current quiz ID for detail view
  const [currentQuizId, setCurrentQuizId] = useState<number | undefined>();

  // Current attempt ID
  const [currentAttemptId, setCurrentAttemptId] = useState<number | undefined>();

  // Duplicate submission prevention
  const submitMutationInProgress = useRef(false);

  // ============================================
  // QUERIES
  // ============================================

  /**
   * Query: Get all published quizzes
   */
  const {
    data: quizzesData,
    isLoading: quizzesLoading,
    error: quizzesError,
  } = useQuery({
    queryKey: QUIZ_QUERY_KEYS.list(quizzesPage, quizzesPageSize),
    queryFn: () => quizService.getQuizzes(quizzesPage, quizzesPageSize),
    staleTime: QUIZ_STALE_TIME,
    gcTime: QUIZ_CACHE_TIME,
  });

  /**
   * Query: Get current quiz with questions (for taking)
   */
  const {
    data: currentQuiz,
    isLoading: quizLoading,
    error: quizError,
  } = useQuery({
    queryKey: currentQuizId ? QUIZ_QUERY_KEYS.detail(currentQuizId) : undefined,
    queryFn: () => (currentQuizId ? quizService.getQuizDetail(currentQuizId) : undefined),
    staleTime: QUIZ_STALE_TIME,
    gcTime: QUIZ_CACHE_TIME,
    enabled: !!currentQuizId,
  });

  /**
   * Query: Get current attempt (in-progress quiz)
   * This is NOT fetched directly; it's set from mutation response
   * But we can query it for polling/refresh if needed
   */
  const [currentAttempt, setCurrentAttempt] = useState<QuizAttemptResponse | undefined>();

  /**
   * Query: Get attempt details with answer review
   */
  const {
    data: attemptDetails,
    isLoading: detailsLoading,
    error: detailsError,
  } = useQuery({
    queryKey: currentAttemptId ? QUIZ_QUERY_KEYS.attempt(currentAttemptId) : undefined,
    queryFn: () => (currentAttemptId ? quizService.getAttemptDetails(currentAttemptId) : undefined),
    staleTime: QUIZ_STALE_TIME,
    gcTime: QUIZ_CACHE_TIME,
    enabled: !!currentAttemptId,
  });

  /**
   * Query: Get user's quiz history
   */
  const {
    data: historyData,
    isLoading: historyLoading,
    error: historyError,
  } = useQuery({
    queryKey: QUIZ_QUERY_KEYS.historyPaginated(historyPage, historyPageSize),
    queryFn: () => quizService.getQuizHistory(historyPage, historyPageSize),
    staleTime: QUIZ_STALE_TIME,
    gcTime: QUIZ_CACHE_TIME,
  });

  // ============================================
  // MUTATIONS
  // ============================================

  /**
   * Mutation: Start quiz attempt
   */
  const {
    mutateAsync: startQuizMutation,
    isPending: isStartingQuiz,
  } = useMutation({
    mutationFn: (quizId: number) => quizService.startQuiz(quizId),
    onSuccess: (data) => {
      // Update current attempt
      setCurrentAttempt(data);
      setCurrentAttemptId(data.id);

      // Invalidate history (new attempt created)
      queryClient.invalidateQueries({
        queryKey: QUIZ_QUERY_KEYS.history(),
      });
    },
  });

  /**
   * Mutation: Submit quiz answers
   * 
   * Triple protection against duplicate submission:
   * 1. useRef lock (check before calling backend)
   * 2. React Query isPending (disable button)
   * 3. Backend validates (attempt not already completed)
   */
  const {
    mutateAsync: submitQuizMutation,
    isPending: isSubmittingQuiz,
  } = useMutation({
    mutationFn: async (params: { attemptId: number; answers: SubmitQuizRequest }) => {
      // Layer 1: useRef lock
      if (submitMutationInProgress.current) {
        throw new Error('Submission already in progress');
      }
      submitMutationInProgress.current = true;

      try {
        return await quizService.submitQuiz(params.attemptId, params.answers);
      } finally {
        submitMutationInProgress.current = false;
      }
    },
    onSuccess: (data) => {
      // Update current attempt
      setCurrentAttempt(data);

      // Invalidate all related queries
      queryClient.invalidateQueries({
        queryKey: QUIZ_QUERY_KEYS.attempts(),
      });
      queryClient.invalidateQueries({
        queryKey: QUIZ_QUERY_KEYS.history(),
      });
    },
  });

  // ============================================
  // PUBLIC METHODS
  // ============================================

  /**
   * Start taking a quiz
   * @param quizId - Quiz ID to start
   * @returns Promise with attempt ID
   */
  const startQuiz = useCallback(
    async (quizId: number): Promise<QuizAttemptResponse> => {
      setCurrentQuizId(quizId);
      return startQuizMutation(quizId);
    },
    [startQuizMutation]
  );

  /**
   * Submit quiz answers
   * @param attemptId - Attempt ID (from startQuiz)
   * @param answers - User's answers
   * @returns Promise with result
   */
  const submitQuiz = useCallback(
    async (attemptId: number, answers: SubmitQuizRequest): Promise<QuizAttemptResponse> => {
      return submitQuizMutation({ attemptId, answers });
    },
    [submitQuizMutation]
  );

  /**
   * Load more quizzes (pagination)
   */
  const loadMoreQuizzes = useCallback(() => {
    if (quizzesData && quizzesPage < quizzesData.totalPages - 1) {
      setQuizzesPage((p) => p + 1);
    }
  }, [quizzesData, quizzesPage]);

  /**
   * Load more history (pagination)
   */
  const loadMoreHistory = useCallback(() => {
    if (historyData && historyPage < historyData.totalPages - 1) {
      setHistoryPage((p) => p + 1);
    }
  }, [historyData, historyPage]);

  /**
   * Refetch all data
   */
  const refetch = useCallback(async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: QUIZ_QUERY_KEYS.lists() }),
      queryClient.invalidateQueries({ queryKey: QUIZ_QUERY_KEYS.details() }),
      queryClient.invalidateQueries({ queryKey: QUIZ_QUERY_KEYS.history() }),
    ]);
  }, [queryClient]);

  // ============================================
  // RETURN INTERFACE
  // ============================================

  return {
    // Quiz List State
    quizzes: quizzesData?.content ?? [],
    quizzesLoading,
    quizzesError: quizzesError as Error | undefined,
    quizzesPageSize,
    quizzesTotalPages: quizzesData?.totalPages ?? 0,
    quizzesCurrentPage: quizzesPage,

    // Quiz Detail State
    currentQuiz,
    quizLoading,
    quizError: quizError as Error | undefined,

    // Current Attempt State
    currentAttempt,
    attemptLoading: isStartingQuiz,
    attemptError: undefined, // Set by mutation error handler if needed

    // Attempt Details (Review) State
    attemptDetails,
    detailsLoading,
    detailsError: detailsError as Error | undefined,

    // Quiz History State
    quizHistory: historyData?.content ?? [],
    historyLoading,
    historyError: historyError as Error | undefined,
    historyPageSize,
    historyTotalPages: historyData?.totalPages ?? 0,
    historyCurrentPage: historyPage,

    // Mutations
    startQuiz,
    isStartingQuiz,
    submitQuiz,
    isSubmittingQuiz,

    // Pagination
    loadMoreQuizzes,
    loadMoreHistory,

    // Helpers
    refetch,
  };
}

/**
 * Export as named export for explicit usage
 */
export default useQuiz;
