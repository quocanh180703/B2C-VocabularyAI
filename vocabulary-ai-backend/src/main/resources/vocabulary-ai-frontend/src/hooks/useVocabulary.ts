import { useState, useEffect, useCallback } from 'react';
import * as vocabularyService from '@/services/vocabularyService';
import { CefrWordResponse, FavoriteWordResponse, LearningProgressResponse } from '@/services/api/types';
import { useAuth } from '@/hooks/useAuth';

export interface VocabularyState {
  // Search & Filter State
  words: CefrWordResponse[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;

  // Favorite & Learning State (user-specific)
  favoriteWordIds: number[];
  learningProgress: { [wordId: number]: LearningProgressResponse };

  // Statistics
  statistics: { [level: string]: number };

  // UI State
  loading: boolean;
  error: Error | null;

  // Actions
  search: (query: string, level: string, page: number, size: number) => Promise<void>;
  refetch: () => Promise<void>;
  addToFavorites: (wordId: number) => Promise<void>;
  removeFromFavorites: (wordId: number) => Promise<void>;
  startLearning: (wordId: number) => Promise<void>;
  reviewWord: (wordId: number, isCorrect: boolean) => Promise<void>;
  isFavorited: (wordId: number) => boolean;
  getLearningStatus: (wordId: number) => LearningProgressResponse | null;
}

interface VocabularySearchParams {
  query?: string;
  level?: string;
  page?: number;
  size?: number;
}

/**
 * useVocabulary Hook
 * 
 * Manages all vocabulary-related state and API calls
 * Handles searching, filtering, pagination, favorites, and learning progress
 * 
 * Features:
 * - Search with debounce (handled by parent component)
 * - CEFR level filtering
 * - Pagination
 * - Favorite management (requires auth)
 * - Learning progress tracking (requires auth)
 * - Caches favorites & learning progress for quick lookup
 * 
 * Usage:
 * ```
 * const vocab = useVocabulary();
 * 
 * // Search vocabulary
 * await vocab.search('beautiful', 'B2', 0, 20);
 * 
 * // Manage favorites (authenticated)
 * await vocab.addToFavorites(123);
 * if (vocab.isFavorited(123)) { ... }
 * 
 * // Track learning progress (authenticated)
 * await vocab.startLearning(123);
 * const status = vocab.getLearningStatus(123);
 * ```
 */
export function useVocabulary(): VocabularyState {
  const { isAuthenticated, user } = useAuth();

  // Search & Pagination State
  const [words, setWords] = useState<CefrWordResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);

  // User-specific State (requires auth)
  const [favoriteWordIds, setFavoriteWordIds] = useState<number[]>([]);
  const [learningProgress, setLearningProgress] = useState<{
    [wordId: number]: LearningProgressResponse;
  }>({});

  // Statistics
  const [statistics, setStatistics] = useState<{ [level: string]: number }>({});

  // UI State
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  // Store last search params for refetch
  const [lastSearchParams, setLastSearchParams] = useState<VocabularySearchParams>({
    query: '',
    level: 'All',
    page: 0,
    size: 20,
  });

  /**
   * Search vocabulary words with filters and pagination
   */
  const search = useCallback(
    async (query: string = '', level: string = 'All', page: number = 0, size: number = 20) => {
      try {
        setLoading(true);
        setError(null);

        console.log('🔍 Searching vocabulary:', {
          query,
          level,
          page,
          size,
          timestamp: new Date().toISOString(),
        });

        const result = await vocabularyService.searchCefrWords(query, level, page, size);

        setWords(result.content);
        setTotalElements(result.totalElements);
        setTotalPages(result.totalPages);
        setCurrentPage(result.number);
        setPageSize(result.size);

        // Store search params for refetch
        setLastSearchParams({ query, level, page, size });

        console.log(`✅ Found ${result.content.length} words`);
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to search vocabulary';
        const searchError = new Error(errorMessage);
        setError(searchError);
        console.error('Search error:', err);
      } finally {
        setLoading(false);
      }
    },
    []
  );

  /**
   * Load user-specific data (favorites, learning progress, stats)
   * Only called when authenticated
   */
  const loadUserData = useCallback(async () => {
    if (!isAuthenticated) {
      setFavoriteWordIds([]);
      setLearningProgress({});
      return;
    }

    try {
      console.log('👤 Loading user vocabulary data...');

      // Load favorites and progress in parallel (may fail if not authenticated)
      let favoriteIds: number[] = [];
      let progressMap: { [wordId: number]: LearningProgressResponse } = {};

      try {
        const [favoritesData, progressData] = await Promise.all([
          vocabularyService.getFavoriteWords(0, 999),
          vocabularyService.getLearningProgress(0, 999),
        ]);

        favoriteIds = favoritesData.content.map(fav => fav.wordId || fav.id);
        progressMap = progressData.content.reduce(
          (acc, prog) => {
            acc[prog.wordId] = prog;
            return acc;
          },
          {} as { [wordId: number]: LearningProgressResponse }
        );
      } catch (err) {
        console.warn('Failed to load favorites/progress:', err);
        // Continue without user-specific data
      }

      // Load statistics separately (public endpoint)
      let statsData = {};
      try {
        statsData = await vocabularyService.getCefrWordStatistics();
      } catch (err) {
        console.warn('Failed to load statistics:', err);
        statsData = { A1: 0, A2: 0, B1: 0, B2: 0, C1: 0, Total: 0 };
      }

      setFavoriteWordIds(favoriteIds);
      setLearningProgress(progressMap);
      setStatistics(statsData);

      console.log(`✅ Loaded user data: ${favoriteIds.length} favorites, ${Object.keys(progressMap).length} in progress`);
    } catch (err) {
      console.warn('Failed to load user vocabulary data:', err);
      // Non-fatal error - continue with public data
    }
  }, [isAuthenticated]);

  /**
   * Refetch current search with stored parameters
   */
  const refetch = useCallback(async () => {
    await search(
      lastSearchParams.query,
      lastSearchParams.level,
      lastSearchParams.page,
      lastSearchParams.size
    );
  }, [search, lastSearchParams]);

  /**
   * Add word to favorites
   */
  const addToFavorites = useCallback(async (wordId: number) => {
    if (!isAuthenticated) {
      throw new Error('Must be authenticated to add favorites');
    }

    try {
      console.log('⭐ Adding to favorites:', wordId);
      await vocabularyService.addToFavorites(wordId);

      // Update local state
      setFavoriteWordIds(prev => {
        if (prev.includes(wordId)) return prev;
        return [...prev, wordId];
      });

      console.log('✅ Added to favorites:', wordId);
    } catch (err) {
      console.error('Failed to add to favorites:', err);
      throw err;
    }
  }, [isAuthenticated]);

  /**
   * Remove word from favorites
   */
  const removeFromFavorites = useCallback(async (wordId: number) => {
    if (!isAuthenticated) {
      throw new Error('Must be authenticated to manage favorites');
    }

    try {
      console.log('🗑️ Removing from favorites:', wordId);
      await vocabularyService.removeFromFavorites(wordId);

      // Update local state
      setFavoriteWordIds(prev => prev.filter(id => id !== wordId));

      console.log('✅ Removed from favorites:', wordId);
    } catch (err) {
      console.error('Failed to remove from favorites:', err);
      throw err;
    }
  }, [isAuthenticated]);

  /**
   * Start learning a word
   */
  const startLearning = useCallback(async (wordId: number) => {
    if (!isAuthenticated) {
      throw new Error('Must be authenticated to start learning');
    }

    try {
      console.log('📚 Starting to learn:', wordId);
      const progress = await vocabularyService.startLearningWord(wordId);

      // Update local state
      setLearningProgress(prev => ({
        ...prev,
        [wordId]: progress,
      }));

      console.log('✅ Started learning:', wordId);
    } catch (err) {
      console.error('Failed to start learning:', err);
      throw err;
    }
  }, [isAuthenticated]);

  /**
   * Review a word (mark as correct or incorrect)
   */
  const reviewWord = useCallback(
    async (wordId: number, isCorrect: boolean) => {
      if (!isAuthenticated) {
        throw new Error('Must be authenticated to review words');
      }

      try {
        console.log(`✏️ Reviewing word ${wordId}: ${isCorrect ? 'correct' : 'incorrect'}`);
        const progress = await vocabularyService.reviewWord(wordId, isCorrect);

        // Update local state
        setLearningProgress(prev => ({
          ...prev,
          [wordId]: progress,
        }));

        console.log('✅ Reviewed word:', wordId);
      } catch (err) {
        console.error('Failed to review word:', err);
        throw err;
      }
    },
    [isAuthenticated]
  );

  /**
   * Check if word is favorited (O(1) lookup)
   */
  const isFavorited = useCallback(
    (wordId: number): boolean => {
      return favoriteWordIds.includes(wordId);
    },
    [favoriteWordIds]
  );

  /**
   * Get learning status for a word
   */
  const getLearningStatus = useCallback(
    (wordId: number): LearningProgressResponse | null => {
      return learningProgress[wordId] || null;
    },
    [learningProgress]
  );

  // Load user data when authentication state changes
  useEffect(() => {
    loadUserData();
  }, [isAuthenticated, user?.id, loadUserData]);

  return {
    // Search & Pagination
    words,
    totalElements,
    totalPages,
    currentPage,
    pageSize,

    // User Data
    favoriteWordIds,
    learningProgress,

    // Statistics
    statistics,

    // UI State
    loading,
    error,

    // Actions
    search,
    refetch,
    addToFavorites,
    removeFromFavorites,
    startLearning,
    reviewWord,
    isFavorited,
    getLearningStatus,
  };
}
