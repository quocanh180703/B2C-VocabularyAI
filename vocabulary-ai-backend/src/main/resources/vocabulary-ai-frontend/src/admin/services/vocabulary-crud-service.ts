import type { VocabularyFormData } from "../components/forms/VocabularyFormModal"

const API_BASE = "/api/admin/vocabulary"

export interface VocabularyResponse {
  id: string
  word: string
  meaning: string
  partOfSpeech: string
  cefr: string
  pronunciation?: string
  definition?: string
  example?: string
  audioUrl?: string
  imageUrl?: string
  createdAt: string
  updatedAt: string
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

/**
 * Vocabulary CRUD service for API calls
 */
export const vocabularyCrudService = {
  /**
   * Create new vocabulary entry
   */
  async create(data: VocabularyFormData): Promise<VocabularyResponse> {
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
      throw new Error(error.message || "Failed to create vocabulary")
    }

    const result: ApiResponse<VocabularyResponse> = await response.json()
    return result.data
  },

  /**
   * Update existing vocabulary entry
   */
  async update(wordId: string, data: Partial<VocabularyFormData>): Promise<VocabularyResponse> {
    const response = await fetch(`${API_BASE}/${wordId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Failed to update vocabulary")
    }

    const result: ApiResponse<VocabularyResponse> = await response.json()
    return result.data
  },

  /**
   * Delete vocabulary entry
   */
  async delete(wordId: string): Promise<void> {
    const response = await fetch(`${API_BASE}/${wordId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.message || "Failed to delete vocabulary")
    }
  },
}
