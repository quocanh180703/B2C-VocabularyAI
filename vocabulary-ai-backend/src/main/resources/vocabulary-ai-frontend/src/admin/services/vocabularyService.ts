import { apiClient } from "./apiClient"

export interface VocabularyListItem {
  id: string
  word: string
  pronunciation: string
  partOfSpeech: string
  cefr: string
  meaning: string
  source: string
  createdAt: string
}

export interface VocabularyDetail {
  id: string
  word: string
  pronunciation: string
  partOfSpeech: string
  cefr: string
  definition: string
  source: string
  language: string
  usageCount: number
  createdAt: string
  updatedAt: string
}

export interface CefrStats {
  a1Count: number
  a2Count: number
  b1Count: number
  b2Count: number
  c1Count: number
  totalCount: number
}

export interface Category {
  categoryId: string
  categoryName: string
  wordCount: number
  createdAt: string
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
}

/**
 * Vocabulary Service - Fetch vocabulary data from backend APIs
 */
export const vocabularyService = {
  /**
   * Get paginated list of vocabulary words
   */
  async list(
    page: number = 0,
    size: number = 20,
    sort: string = "word,asc",
    cefr?: string,
    pos?: string,
    search?: string
  ): Promise<PageResponse<VocabularyListItem>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())
    params.append("sort", sort)
    if (cefr && cefr !== "All") {
      params.append("cefr", cefr)
    }
    if (pos && pos !== "All") {
      params.append("pos", pos)
    }
    if (search && search.trim()) {
      params.append("search", search)
    }

    const response = await apiClient.get<ApiResponse<PageResponse<VocabularyListItem>>>(
      `/vocabulary?${params.toString()}`
    )
    return response.data.data
  },

  /**
   * Get vocabulary word detail
   */
  async getDetail(wordId: string): Promise<VocabularyDetail> {
    const response = await apiClient.get<ApiResponse<VocabularyDetail>>(
      `/vocabulary/${wordId}`
    )
    return response.data.data
  },

  /**
   * Get CEFR level distribution statistics
   */
  async getCefrStats(): Promise<CefrStats> {
    const response = await apiClient.get<ApiResponse<CefrStats>>(
      `/vocabulary/stats/cefr`
    )
    return response.data.data
  },

  /**
   * Get list of vocabulary categories
   */
  async getCategories(
    page: number = 0,
    size: number = 50
  ): Promise<PageResponse<Category>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())

    const response = await apiClient.get<ApiResponse<PageResponse<Category>>>(
      `/vocabulary/categories?${params.toString()}`
    )
    return response.data.data
  },
}
