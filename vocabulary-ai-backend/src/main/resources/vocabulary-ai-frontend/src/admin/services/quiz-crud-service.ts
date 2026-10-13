export interface QuizFormData {
  title: string
  description?: string
  category: string
  level: string
  timeLimit: number
  questionCount: number
  status?: string
}

export interface QuizResponse {
  id: number
  title: string
  description?: string
  category: string
  level: string
  timeLimit: number
  questionCount: number
  status: string
  totalAttempts: number
  completedAttempts: number
  avgScore: number
  passRate: number
  createdAt: string
  updatedAt: string
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

const API_BASE = "/api/admin/quiz"

/**
 * Quiz CRUD service for API calls
 */
export const quizCrudService = {
  /**
   * Create new quiz
   */
  async create(data: QuizFormData): Promise<QuizResponse> {
    const response = await fetch(API_BASE, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Failed to create quiz")
    }

    const result: ApiResponse<QuizResponse> = await response.json()
    return result.data
  },

  /**
   * Update existing quiz
   */
  async update(quizId: number, data: Partial<QuizFormData>): Promise<QuizResponse> {
    const response = await fetch(`${API_BASE}/${quizId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Failed to update quiz")
    }

    const result: ApiResponse<QuizResponse> = await response.json()
    return result.data
  },

  /**
   * Delete quiz
   */
  async delete(quizId: number): Promise<void> {
    const response = await fetch(`${API_BASE}/${quizId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Failed to delete quiz")
    }
  },
}
