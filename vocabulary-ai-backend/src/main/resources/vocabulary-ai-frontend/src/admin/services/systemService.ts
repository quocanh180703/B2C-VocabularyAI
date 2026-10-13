import { apiClient } from "./apiClient"

export interface SystemOverview {
  totalUsers: number
  activeUsersToday: number
  totalSessions: number
  totalQuizzes: number
  totalVocabularies: number
  avgUserEngagement: number
}

export interface UserRegistrationTrend {
  date: string
  count: number
}

export interface SystemHealth {
  totalUsers: number
  apiCallsToday: number
  averageResponseTime: number
  errorRate: number
  status: string
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

/**
 * System Service - Fetch system metrics and dashboard data from backend APIs
 */
export const systemService = {
  /**
   * Get system overview statistics
   */
  async getOverview(): Promise<SystemOverview> {
    const response = await apiClient.get<ApiResponse<SystemOverview>>(
      `/system/overview`
    )
    return response.data.data
  },

  /**
   * Get user registration trend
   */
  async getRegistrationTrend(): Promise<UserRegistrationTrend[]> {
    const response = await apiClient.get<ApiResponse<UserRegistrationTrend[]>>(
      `/system/users/registration-trend`
    )
    return response.data.data
  },

  /**
   * Get system health status
   */
  async getHealth(): Promise<SystemHealth> {
    const response = await apiClient.get<ApiResponse<SystemHealth>>(
      `/system/health`
    )
    return response.data.data
  },
}
