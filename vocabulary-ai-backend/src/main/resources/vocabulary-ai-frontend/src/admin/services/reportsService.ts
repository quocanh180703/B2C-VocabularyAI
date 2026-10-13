import { apiClient } from "./apiClient"

export interface UserEngagementData {
  date: string
  activeUsers: number
}

export interface LearningProgressData {
  totalAttempts: number
  avgScore: number
  avgCompletionTime: number
  uniqueUsers: number
}

export interface ContentPerformanceData {
  quizId: string
  quizTitle: string
  level: string
  attemptCount: number
  avgScore: number
  completionRate: number
}

export interface RevenueData {
  totalTransactions: number
  totalRevenue: number
  avgTransactionValue: number
  uniqueCustomers: number
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

/**
 * Reports Service - Fetch reporting and analytics data from backend APIs
 */
export const reportsService = {
  /**
   * Get user engagement report
   */
  async getUserEngagement(): Promise<UserEngagementData[]> {
    const response = await apiClient.get<ApiResponse<UserEngagementData[]>>(
      `/reports/engagement`
    )
    return response.data.data
  },

  /**
   * Get learning progress report
   */
  async getLearningProgress(): Promise<LearningProgressData> {
    const response = await apiClient.get<ApiResponse<LearningProgressData>>(
      `/reports/learning-progress`
    )
    return response.data.data
  },

  /**
   * Get content performance report
   */
  async getContentPerformance(): Promise<ContentPerformanceData[]> {
    const response = await apiClient.get<ApiResponse<ContentPerformanceData[]>>(
      `/reports/content-performance`
    )
    return response.data.data
  },

  /**
   * Get revenue report
   */
  async getRevenue(): Promise<RevenueData> {
    const response = await apiClient.get<ApiResponse<RevenueData>>(
      `/reports/revenue`
    )
    return response.data.data
  },
}
