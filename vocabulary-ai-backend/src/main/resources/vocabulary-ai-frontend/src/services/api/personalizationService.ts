/**
 * Personalization Service
 * Unified service for all personalization/adaptive learning APIs
 * Handles: Profile, Learning Plan, Progress Analysis, Recommendations, etc.
 */

import { apiClient } from "./client";
import { PERSONALIZATION_ENDPOINTS } from "./endpoints";
import {
  UserProfile,
  LearningPlan,
  ProgressAnalysis,
  RecommendationsResponse,
  WeaknessStrengthAnalysis,
  AdaptiveDifficulty,
  DailyPractice,
  GetProfileRequest,
  GetLearningPlanRequest,
  GetProgressAnalysisRequest,
  GetRecommendationsRequest,
  GetWeaknessStrengthRequest,
  GetAdaptiveDifficultyRequest,
  GetDailyPracticeRequest,
} from "./personalization.types";

export class PersonalizationService {
  // ============================================
  // USER PROFILE
  // ============================================

  /**
   * Get user profile with personalization settings
   */
  static async getProfile(request?: GetProfileRequest): Promise<UserProfile> {
    const response = await apiClient.get<UserProfile>(
      PERSONALIZATION_ENDPOINTS.GET_PROFILE,
      request ? { params: request } : undefined
    );
    return response.data.data!;
  }

  // ============================================
  // LEARNING PLAN
  // ============================================

  /**
   * Get personalized learning plan
   */
  static async getLearningPlan(request?: GetLearningPlanRequest): Promise<LearningPlan> {
    const response = await apiClient.get<LearningPlan>(
      PERSONALIZATION_ENDPOINTS.GET_LEARNING_PLAN,
      request ? { params: request } : undefined
    );
    return response.data.data!;
  }

  // ============================================
  // PROGRESS ANALYSIS
  // ============================================

  /**
   * Get progress analysis and metrics
   */
  static async getProgressAnalysis(request?: GetProgressAnalysisRequest): Promise<ProgressAnalysis> {
    const response = await apiClient.get<ProgressAnalysis>(
      PERSONALIZATION_ENDPOINTS.GET_PROGRESS_ANALYSIS,
      request ? { params: request } : undefined
    );
    return response.data.data!;
  }

  // ============================================
  // RECOMMENDATIONS
  // ============================================

  /**
   * Get personalized recommendations
   */
  static async getRecommendations(
    request?: GetRecommendationsRequest
  ): Promise<RecommendationsResponse> {
    const response = await apiClient.get<RecommendationsResponse>(
      PERSONALIZATION_ENDPOINTS.GET_RECOMMENDATIONS,
      request ? { params: request } : undefined
    );
    return response.data.data!;
  }

  // ============================================
  // WEAKNESS/STRENGTH ANALYSIS
  // ============================================

  /**
   * Get weakness and strength analysis
   */
  static async getWeaknessStrength(
    request?: GetWeaknessStrengthRequest
  ): Promise<WeaknessStrengthAnalysis> {
    const response = await apiClient.get<WeaknessStrengthAnalysis>(
      PERSONALIZATION_ENDPOINTS.GET_WEAKNESS_STRENGTH,
      request ? { params: request } : undefined
    );
    return response.data.data!;
  }

  // ============================================
  // ADAPTIVE DIFFICULTY
  // ============================================

  /**
   * Get adaptive difficulty settings
   */
  static async getAdaptiveDifficulty(
    request?: GetAdaptiveDifficultyRequest
  ): Promise<AdaptiveDifficulty> {
    const response = await apiClient.get<AdaptiveDifficulty>(
      PERSONALIZATION_ENDPOINTS.GET_ADAPTIVE_DIFFICULTY,
      request ? { params: request } : undefined
    );
    return response.data.data!;
  }

  // ============================================
  // DAILY PRACTICE
  // ============================================

  /**
   * Get daily practice exercises
   */
  static async getDailyPractice(request?: GetDailyPracticeRequest): Promise<DailyPractice> {
    const response = await apiClient.get<DailyPractice>(
      PERSONALIZATION_ENDPOINTS.GET_DAILY_PRACTICE,
      request ? { params: request } : undefined
    );
    return response.data.data!;
  }
}

export default PersonalizationService;
export type {
  UserProfile,
  LearningPlan,
  ProgressAnalysis,
  RecommendationsResponse,
  WeaknessStrengthAnalysis,
  AdaptiveDifficulty,
  DailyPractice,
};
