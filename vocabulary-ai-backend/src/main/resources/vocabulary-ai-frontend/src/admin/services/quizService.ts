import { apiClient } from "./apiClient"

export interface QuizListItem {
  id: string
  title: string
  level: string
  category: string
  timeLimit: number
  status: string
  questionCount: number
  attempts: number
  avgScore: number
  createdAt: string
  updatedAt: string
}

export interface QuizDetail {
  id: string
  title: string
  description: string
  level: string
  category: string
  timeLimit: number
  status: string
  questionCount: number
  attempts: number
  avgScore: number
  passRate: number
  createdAt: string
  updatedAt: string
}

export interface QuizStats {
  totalQuizzes: number
  publishedQuizzes: number
  draftQuizzes: number
  archivedQuizzes: number
  avgScore: number
  levelDistribution: {
    [key: string]: number
  }
  statusDistribution: {
    [key: string]: number
  }
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
 * Quiz Service - Fetch quiz data from backend APIs
 */
export const quizService = {
  /**
   * Get paginated list of quizzes
   */
  async list(
    page: number = 0,
    size: number = 20,
    sort: string = "createdAt,desc",
    level?: string,
    status?: string,
    search?: string
  ): Promise<PageResponse<QuizListItem>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())
    params.append("sort", sort)
    if (level && level !== "All") {
      params.append("level", level)
    }
    if (status && status !== "All") {
      params.append("status", status)
    }
    if (search && search.trim()) {
      params.append("search", search)
    }

    const response = await apiClient.get<ApiResponse<PageResponse<QuizListItem>>>(
      `/quiz?${params.toString()}`
    )
    return response.data.data
  },

  /**
   * Get quiz detail
   */
  async getDetail(quizId: string): Promise<QuizDetail> {
    const response = await apiClient.get<ApiResponse<QuizDetail>>(
      `/quiz/${quizId}`
    )
    return response.data.data
  },

  /**
   * Get quiz statistics
   */
  async getStats(): Promise<QuizStats> {
    const response = await apiClient.get<ApiResponse<QuizStats>>(
      `/quiz/stats/summary`
    )
    return response.data.data
  },
}
