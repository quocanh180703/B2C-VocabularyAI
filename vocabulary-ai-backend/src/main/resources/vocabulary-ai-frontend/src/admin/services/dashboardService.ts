import { apiClient } from "./apiClient"

export interface DashboardSummary {
  totalUsers: number
  activeSubscriptions: number
  totalRevenue: number
  activeLearningSessionsToday: number
  systemStatus: string
  lastUpdatedAt: string
}

export interface DashboardRevenue {
  totalRevenue: number
  revenueToday: number
  revenueThisMonth: number
  totalPayments: number
  paymentsToday: number
  averagePaymentAmount: number
  popularPackageId: number | null
  popularPackageName: string | null
}

export interface TopStudent {
  userId: number
  fullName: string
  email: string
  totalSessions: number
  wordsLearned: number
  accuracyRate: number
  rank: number
}

export interface ActivityTrend {
  dates: string[]
  learningSessions: number[]
  activeUsers: number[]
  dailyRevenue: number[]
  paymentCounts: number[]
  period: string
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

/**
 * Dashboard Service - Fetch dashboard metrics from backend APIs
 */
export const dashboardService = {
  /**
   * Get dashboard summary metrics
   */
  async getSummary(): Promise<DashboardSummary> {
    const response = await apiClient.get<ApiResponse<DashboardSummary>>(
      "/dashboard/summary"
    )
    return response.data.data
  },

  /**
   * Get revenue metrics
   */
  async getRevenue(): Promise<DashboardRevenue> {
    const response = await apiClient.get<ApiResponse<DashboardRevenue>>(
      "/dashboard/revenue"
    )
    return response.data.data
  },

  /**
   * Get top performing students
   */
  async getTopStudents(): Promise<TopStudent[]> {
    const response = await apiClient.get<ApiResponse<TopStudent[]>>(
      "/dashboard/top-students"
    )
    return response.data.data
  },

  /**
   * Get 30-day activity trends
   */
  async getActivityTrend(): Promise<ActivityTrend> {
    const response = await apiClient.get<ApiResponse<ActivityTrend>>(
      "/dashboard/activity-trend"
    )
    return response.data.data
  },
}
