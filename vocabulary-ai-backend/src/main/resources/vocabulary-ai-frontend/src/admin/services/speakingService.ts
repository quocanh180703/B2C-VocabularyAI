import { apiClient } from "./apiClient"

export interface SpeakingSessionListItem {
  id: string
  userId: string
  userEmail: string
  topic: string
  level: string
  status: string
  attemptCount: number
  overallScore: number
  startedAt: string
  completedAt: string
  createdAt: string
}

export interface SpeakingSessionDetail {
  id: string
  userId: string
  userEmail: string
  topic: string
  description: string
  level: string
  status: string
  attemptCount: number
  overallScore: number
  startedAt: string
  completedAt: string
  createdAt: string
}

export interface SpeakingStats {
  totalSessions: number
  completedSessions: number
  inProgressSessions: number
  cancelledSessions: number
  averageScore: number
  averageAttempts: number
  activeUsers: number
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
 * Speaking Service - Fetch speaking session data from backend APIs
 */
export const speakingService = {
  /**
   * Get paginated list of speaking sessions
   */
  async list(
    page: number = 0,
    size: number = 20,
    sort: string = "createdAt,desc",
    userId?: string,
    level?: string,
    status?: string
  ): Promise<PageResponse<SpeakingSessionListItem>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())
    params.append("sort", sort)
    if (userId && userId.trim()) {
      params.append("userId", userId)
    }
    if (level && level !== "All") {
      params.append("level", level)
    }
    if (status && status !== "All") {
      params.append("status", status)
    }

    const response = await apiClient.get<ApiResponse<PageResponse<SpeakingSessionListItem>>>(
      `/speaking?${params.toString()}`
    )
    return response.data.data
  },

  /**
   * Get speaking session detail
   */
  async getDetail(sessionId: string): Promise<SpeakingSessionDetail> {
    const response = await apiClient.get<ApiResponse<SpeakingSessionDetail>>(
      `/speaking/${sessionId}`
    )
    return response.data.data
  },

  /**
   * Get speaking statistics
   */
  async getStats(): Promise<SpeakingStats> {
    const response = await apiClient.get<ApiResponse<SpeakingStats>>(
      `/speaking/stats/summary`
    )
    return response.data.data
  },
}
