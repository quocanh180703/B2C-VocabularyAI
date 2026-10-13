/**
 * Vocabulary Service
 * Xử lý tất cả vocabulary/word-related API calls
 */

import { apiClient } from "./client";
import { VOCABULARY_ENDPOINTS } from "./endpoints";
import {
  Word,
  WordDetailResponse,
  CreateWordRequest,
  GetWordsRequest,
  ApiListResponse,
} from "./types";

export class VocabularyService {
  /**
   * Lấy danh sách từ vựng
   */
  static async getWords(params: GetWordsRequest = {}): Promise<{
    words: Word[];
    pagination: any;
  }> {
    const response = await apiClient.get<Word[]>(VOCABULARY_ENDPOINTS.GET_WORDS, {
      params,
    });
    return {
      words: response.data.data || [],
      pagination: (response.data as any).pagination,
    };
  }

  /**
   * Lấy chi tiết một từ vựng
   */
  static async getWordDetail(wordId: string): Promise<WordDetailResponse> {
    const response = await apiClient.get<WordDetailResponse>(
      VOCABULARY_ENDPOINTS.GET_WORD_DETAIL(wordId)
    );
    return response.data.data!;
  }

  /**
   * Tạo từ vựng mới (Admin)
   */
  static async createWord(data: CreateWordRequest): Promise<Word> {
    const response = await apiClient.post<Word>(
      VOCABULARY_ENDPOINTS.CREATE_WORD,
      data
    );
    return response.data.data!;
  }

  /**
   * Cập nhật từ vựng (Admin)
   */
  static async updateWord(wordId: string, data: Partial<CreateWordRequest>): Promise<Word> {
    const response = await apiClient.put<Word>(
      VOCABULARY_ENDPOINTS.UPDATE_WORD(wordId),
      data
    );
    return response.data.data!;
  }

  /**
   * Xóa từ vựng (Admin)
   */
  static async deleteWord(wordId: string): Promise<void> {
    await apiClient.delete(VOCABULARY_ENDPOINTS.DELETE_WORD(wordId));
  }

  /**
   * Tìm kiếm từ vựng
   */
  static async searchWords(query: string, limit: number = 10): Promise<Word[]> {
    const response = await apiClient.get<Word[]>(
      VOCABULARY_ENDPOINTS.SEARCH_WORDS,
      {
        params: { q: query, limit },
      }
    );
    return response.data.data || [];
  }

  /**
   * Lấy danh sách categories
   */
  static async getCategories(): Promise<string[]> {
    const response = await apiClient.get<string[]>(
      VOCABULARY_ENDPOINTS.GET_CATEGORIES
    );
    return response.data.data || [];
  }

  /**
   * Lấy từ vựng theo category
   */
  static async getWordsByCategory(category: string): Promise<Word[]> {
    const response = await apiClient.get<Word[]>(
      VOCABULARY_ENDPOINTS.GET_WORDS_BY_CATEGORY(category)
    );
    return response.data.data || [];
  }

  /**
   * Thêm từ vào favorites
   */
  static async addToFavorites(wordId: string): Promise<void> {
    await apiClient.post(VOCABULARY_ENDPOINTS.ADD_TO_FAVORITES(wordId));
  }

  /**
   * Xóa từ khỏi favorites
   */
  static async removeFromFavorites(wordId: string): Promise<void> {
    await apiClient.delete(VOCABULARY_ENDPOINTS.REMOVE_FROM_FAVORITES(wordId));
  }

  /**
   * Lấy danh sách favorites
   */
  static async getFavorites(params: any = {}): Promise<{
    words: Word[];
    pagination: any;
  }> {
    const response = await apiClient.get<Word[]>(
      VOCABULARY_ENDPOINTS.GET_FAVORITES,
      { params }
    );
    return {
      words: response.data.data || [],
      pagination: (response.data as any).pagination,
    };
  }
}

export default VocabularyService;
