import { apiClient } from './api/client';
import {
  CefrWordResponse,
  CefrWordSearchRequest,
  FavoriteWordResponse,
  LearningProgressResponse,
  ExplanationResponse,
} from './api/types';

/**
 * Vocabulary Service
 * 
 * Handles all API calls for vocabulary data
 * Two main data sources:
 * 1. CEFR Vocabulary Dataset (public, no auth needed) - /api/cefr-words
 * 2. User Learning Progress (requires JWT) - /learning/words
 * 3. User Favorites (requires JWT) - /words/favorites
 * 4. AI Explanations (requires JWT) - /api/v1/ai/vocabulary/explain
 * 
 * All authenticated methods automatically include JWT token via interceptor
 */

// ==================== CEFR VOCABULARY SEARCH & LISTING (PUBLIC) ====================

/**
 * Search CEFR vocabulary words
 * Supports: query, level filter, pagination
 */
export async function searchCefrWords(
  query?: string,
  level?: string,
  page: number = 0,
  size: number = 20
): Promise<{
  content: CefrWordResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}> {
  const params: Record<string, any> = { page, size };
  if (query) params.query = query;
  if (level && level !== 'All') params.level = level;

  const response = await apiClient.get<{
    content: CefrWordResponse[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    empty: boolean;
  }>('/api/cefr-words/search', { params });

  return response.data;
}

/**
 * Get CEFR words filtered by level
 */
export async function getCefrWordsByLevel(
  level: string,
  page: number = 0,
  size: number = 20
): Promise<{
  content: CefrWordResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}> {
  const params = { level, page, size };

  const response = await apiClient.get<{
    content: CefrWordResponse[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    empty: boolean;
  }>('/api/cefr-words', { params });

  return response.data;
}

/**
 * Get a specific CEFR word by ID
 */
export async function getCefrWordById(
  wordId: string
): Promise<CefrWordResponse> {
  const response = await apiClient.get<CefrWordResponse>(
    `/api/cefr-words/${wordId}`
  );
  return response.data;
}

/**
 * Get CEFR word statistics (count by level)
 */
export async function getCefrWordStatistics(): Promise<{
  [level: string]: number;
}> {
  try {
    const response = await apiClient.get<any>('/api/cefr-words/statistics');
    const data = response.data;
    
    // Backend returns { a1Count, a2Count, b1Count, b2Count, c1Count, totalCount }
    // Convert to { A1: ..., A2: ..., B1: ..., B2: ..., C1: ..., Total: ... }
    return {
      A1: data?.a1Count ?? 0,
      A2: data?.a2Count ?? 0,
      B1: data?.b1Count ?? 0,
      B2: data?.b2Count ?? 0,
      C1: data?.c1Count ?? 0,
      Total: data?.totalCount ?? 0,
    };
  } catch (error) {
    console.error('Failed to fetch CEFR word statistics:', error);
    // Return empty stats as fallback
    return { A1: 0, A2: 0, B1: 0, B2: 0, C1: 0, Total: 0 };
  }
}

// ==================== FAVORITE WORDS (AUTHENTICATED) ====================

/**
 * Add word to favorites
 */
export async function addToFavorites(
  wordId: number
): Promise<FavoriteWordResponse> {
  const response = await apiClient.post<{ data: FavoriteWordResponse }>(
    `/words/${wordId}/favorite`,
    {}
  );
  return response.data.data;
}

/**
 * Remove word from favorites
 */
export async function removeFromFavorites(
  wordId: number
): Promise<void> {
  await apiClient.delete(`/words/${wordId}/favorite`);
}

/**
 * Get user's favorite words with pagination
 */
export async function getFavoriteWords(
  page: number = 0,
  size: number = 20
): Promise<{
  content: FavoriteWordResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}> {
  const params = { page, size };

  const response = await apiClient.get<{
    data: {
      content: FavoriteWordResponse[];
      totalElements: number;
      totalPages: number;
      number: number;
      size: number;
      empty: boolean;
    };
  }>('/words/favorites', { params });

  return response.data.data;
}

// ==================== LEARNING PROGRESS (AUTHENTICATED) ====================

/**
 * Start learning a word
 */
export async function startLearningWord(
  wordId: number
): Promise<LearningProgressResponse> {
  const response = await apiClient.post<{ data: LearningProgressResponse }>(
    `/learning/words/${wordId}/start`,
    {}
  );
  return response.data.data;
}

/**
 * Review a word (mark as correct or incorrect)
 */
export async function reviewWord(
  wordId: number,
  isCorrect: boolean
): Promise<LearningProgressResponse> {
  const response = await apiClient.post<{ data: LearningProgressResponse }>(
    `/learning/words/${wordId}/review`,
    { correct: isCorrect }
  );
  return response.data.data;
}

/**
 * Get user's learning progress for all words
 */
export async function getLearningProgress(
  page: number = 0,
  size: number = 20
): Promise<{
  content: LearningProgressResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}> {
  const params = { page, size };

  const response = await apiClient.get<{
    data: {
      content: LearningProgressResponse[];
      totalElements: number;
      totalPages: number;
      number: number;
      size: number;
      empty: boolean;
    };
  }>('/learning/words', { params });

  return response.data.data;
}

/**
 * Get words that need review (spaced repetition)
 */
export async function getWordsNeedingReview(
  page: number = 0,
  size: number = 20
): Promise<{
  content: LearningProgressResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}> {
  const params = { page, size };

  const response = await apiClient.get<{
    data: {
      content: LearningProgressResponse[];
      totalElements: number;
      totalPages: number;
      number: number;
      size: number;
      empty: boolean;
    };
  }>('/learning/words-needing-review', { params });

  return response.data.data;
}

// ==================== AI EXPLANATIONS (AUTHENTICATED) ====================

/**
 * Get AI explanation for a word
 */
export async function explainWord(
  wordId: number,
  context?: string
): Promise<ExplanationResponse> {
  const response = await apiClient.post<{ data: ExplanationResponse }>(
    '/api/v1/ai/vocabulary/explain',
    {
      wordId,
      context: context || 'general',
    }
  );
  return response.data.data;
}

/**
 * Get AI explanations for multiple words (batch)
 */
export async function explainWords(
  wordIds: number[]
): Promise<ExplanationResponse[]> {
  const response = await apiClient.post<{ data: ExplanationResponse[] }>(
    '/api/v1/ai/vocabulary/explain-batch',
    { wordIds }
  );
  return response.data.data;
}

// ==================== COMBINED VOCABULARY DATA ====================

/**
 * Vocabulary data fetched for initial page load
 */
export interface VocabularyPageData {
  words: CefrWordResponse[];
  totalElements: number;
  totalPages: number;
  favoriteWordIds: number[];
  learningProgress: { [wordId: number]: LearningProgressResponse };
  statistics: { [level: string]: number };
}

/**
 * Fetch initial vocabulary page data
 * - Search or list words by level
 * - Load user's favorites
 * - Load user's learning progress
 * - Get word statistics
 * 
 * Used on VocabularyPage initial load
 */
export async function getVocabularyPageData(
  level?: string
): Promise<VocabularyPageData> {
  try {
    // Fetch public data (words + statistics) in parallel
    const [wordsResponse, statisticsResponse] = await Promise.all([
      level && level !== 'All'
        ? getCefrWordsByLevel(level, 0, 20)
        : searchCefrWords(undefined, undefined, 0, 20),
      getCefrWordStatistics(),
    ]);

    // Fetch user-specific data in parallel
    let favoriteWordIds: number[] = [];
    let learningProgress: { [wordId: number]: LearningProgressResponse } = {};

    try {
      const [favoritesResponse, progressResponse] = await Promise.all([
        getFavoriteWords(0, 999), // Load all favorites (pagination not needed for comparison)
        getLearningProgress(0, 999), // Load all progress
      ]);

      // Convert to lookup maps for O(1) comparison
      favoriteWordIds = favoritesResponse.content.map(fav => fav.wordId || fav.id);
      learningProgress = progressResponse.content.reduce(
        (acc, prog) => {
          acc[prog.wordId] = prog;
          return acc;
        },
        {} as { [wordId: number]: LearningProgressResponse }
      );
    } catch (error) {
      console.warn('Failed to load user data (favorites/progress):', error);
      // Continue without user data - public data still available
    }

    return {
      words: wordsResponse.content,
      totalElements: wordsResponse.totalElements,
      totalPages: wordsResponse.totalPages,
      favoriteWordIds,
      learningProgress,
      statistics: statisticsResponse,
    };
  } catch (error) {
    console.error('Failed to fetch vocabulary page data:', error);
    throw error;
  }
}

/**
 * Vocabulary detail page data
 */
export interface VocabularyDetailData {
  word: CefrWordResponse;
  explanation: ExplanationResponse | null;
  isFavorite: boolean;
  learningStatus: LearningProgressResponse | null;
}

/**
 * Fetch vocabulary detail page data
 * - Get word details by ID
 * - Get AI explanation
 * - Check if favorited
 * - Get learning progress
 */
export async function getVocabularyDetailData(
  wordId: string
): Promise<VocabularyDetailData> {
  try {
    // Fetch word details (public)
    const word = await getCefrWordById(wordId);

    // Fetch user data in parallel (may fail if not authenticated)
    let explanation: ExplanationResponse | null = null;
    let isFavorite = false;
    let learningStatus: LearningProgressResponse | null = null;

    try {
      const [explanationResponse, favoritesResponse, progressResponse] =
        await Promise.all([
          explainWord(Number(wordId)),
          getFavoriteWords(0, 999),
          getLearningProgress(0, 999),
        ]);

      explanation = explanationResponse;
      
      // Check if word is in favorites
      isFavorite = favoritesResponse.content.some(
        fav => (fav.wordId || fav.id) === Number(wordId)
      );

      // Get learning status for this word
      learningStatus =
        progressResponse.content.find(prog => prog.wordId === Number(wordId)) ||
        null;
    } catch (error) {
      console.warn('Failed to load user-specific data:', error);
      // Continue with public data - explanation/favorites/progress can be missing
    }

    return {
      word,
      explanation,
      isFavorite,
      learningStatus,
    };
  } catch (error) {
    console.error('Failed to fetch vocabulary detail data:', error);
    throw error;
  }
}
