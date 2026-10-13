import { apiClient } from "./apiClient"

export interface StudentListItem {
  userId: number
  username: string
  email: string
  fullName: string
  status: string
  hasActiveSubscription: boolean
  totalSessions: number
  createdAt: string
}

export interface StudentDetail {
  userId: number
  username: string
  email: string
  fullName: string
  status: string
  totalSessions: number
  wordsLearned: number
  accuracyRate: number
  hasActiveSubscription: boolean
  packageName: string | null
  subscriptionEndAt: string | null
  remainingDays: number
  totalPaid: number
  paymentCount: number
}

export interface StudentStats {
  totalStudents: number
  activeStudents: number
  inactiveStudents: number
  bannedStudents: number
  avgSessionsPerStudent: number
  avgWordsPerStudent: number
  avgAccuracyRate: number
  activeSubscriptionsToday: number
  activeTodayCount: number
  newStudentsThisMonth: number
  churnRate: number
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
 * Student Service - Fetch student data from backend APIs
 */
export const studentService = {
  /**
   * Get paginated list of students
   */
  async list(
    page: number = 0,
    size: number = 20,
    sort: string = "createdAt,desc",
    status?: string,
    search?: string
  ): Promise<PageResponse<StudentListItem>> {
    const params = new URLSearchParams()
    params.append("page", page.toString())
    params.append("size", size.toString())
    params.append("sort", sort)
    if (status && status !== "All") {
      params.append("status", status)
    }
    if (search && search.trim()) {
      params.append("search", search)
    }

    const response = await apiClient.get<ApiResponse<PageResponse<StudentListItem>>>(
      `/students?${params.toString()}`
    )
    return response.data.data
  },

  /**
   * Get student detail
   */
  async getDetail(userId: number): Promise<StudentDetail> {
    const response = await apiClient.get<ApiResponse<StudentDetail>>(
      `/students/${userId}`
    )
    return response.data.data
  },

  /**
   * Get student statistics
   */
  async getStats(): Promise<StudentStats> {
    const response = await apiClient.get<ApiResponse<StudentStats>>(
      `/students/stats/summary`
    )
    return response.data.data
  },

  /**
   * Update student status (admin only)
   * Note: This would need backend endpoint implementation
   */
  async updateStatus(userId: number, status: string): Promise<StudentListItem> {
    // For now, return optimistic update
    // Backend endpoint: PUT /api/admin/students/{userId}/status
    console.warn("updateStatus not yet implemented in backend")
    return {} as StudentListItem
  },
}
