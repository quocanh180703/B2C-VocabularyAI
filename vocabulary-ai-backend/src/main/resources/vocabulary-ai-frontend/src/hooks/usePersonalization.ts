/**
 * Personalization Hooks
 * React Query hooks for personalization/adaptive learning APIs
 * Provides caching, refetching, and data management
 */

import { useQuery } from "@tanstack/react-query";
import PersonalizationService from "@/services/api/personalizationService";
import type {
  GetProfileRequest,
  GetLearningPlanRequest,
  GetProgressAnalysisRequest,
  GetRecommendationsRequest,
  GetWeaknessStrengthRequest,
  GetAdaptiveDifficultyRequest,
  GetDailyPracticeRequest,
} from "@/services/api/personalization.types";

// ============================================
// QUERY KEYS
// ============================================

export const personalizationQueryKeys = {
  all: ["personalization"] as const,
  
  profile: () => [...personalizationQueryKeys.all, "profile"] as const,
  
  learningPlan: () => [...personalizationQueryKeys.all, "learning-plan"] as const,
  
  progressAnalysis: () => [...personalizationQueryKeys.all, "progress-analysis"] as const,
  
  recommendations: () => [...personalizationQueryKeys.all, "recommendations"] as const,
  
  weaknessStrength: () => [...personalizationQueryKeys.all, "weakness-strength"] as const,
  
  adaptiveDifficulty: () => [...personalizationQueryKeys.all, "adaptive-difficulty"] as const,
  
  dailyPractice: () => [...personalizationQueryKeys.all, "daily-practice"] as const,
};

// ============================================
// PROFILE HOOKS
// ============================================

/**
 * Hook to get user profile with personalization settings
 * Caches for 10 minutes
 */
export function useProfile(request?: GetProfileRequest) {
  return useQuery({
    queryKey: personalizationQueryKeys.profile(),
    queryFn: () => PersonalizationService.getProfile(request),
    staleTime: 10 * 60 * 1000, // 10 minutes
    gcTime: 15 * 60 * 1000, // 15 minutes
  });
}

// ============================================
// LEARNING PLAN HOOKS
// ============================================

/**
 * Hook to get personalized learning plan
 * Caches for 10 minutes
 */
export function useLearningPlan(request?: GetLearningPlanRequest) {
  return useQuery({
    queryKey: personalizationQueryKeys.learningPlan(),
    queryFn: () => PersonalizationService.getLearningPlan(request),
    staleTime: 10 * 60 * 1000, // 10 minutes
    gcTime: 15 * 60 * 1000, // 15 minutes
  });
}

// ============================================
// PROGRESS ANALYSIS HOOKS
// ============================================

/**
 * Hook to get progress analysis and metrics
 * Caches for 10 minutes
 */
export function useProgressAnalysis(request?: GetProgressAnalysisRequest) {
  return useQuery({
    queryKey: personalizationQueryKeys.progressAnalysis(),
    queryFn: () => PersonalizationService.getProgressAnalysis(request),
    staleTime: 10 * 60 * 1000, // 10 minutes
    gcTime: 15 * 60 * 1000, // 15 minutes
  });
}

// ============================================
// RECOMMENDATIONS HOOKS
// ============================================

/**
 * Hook to get personalized recommendations
 * Caches for 10 minutes
 */
export function useRecommendations(request?: GetRecommendationsRequest) {
  return useQuery({
    queryKey: personalizationQueryKeys.recommendations(),
    queryFn: () => PersonalizationService.getRecommendations(request),
    staleTime: 10 * 60 * 1000, // 10 minutes
    gcTime: 15 * 60 * 1000, // 15 minutes
  });
}

// ============================================
// WEAKNESS/STRENGTH HOOKS
// ============================================

/**
 * Hook to get weakness and strength analysis
 * Caches for 10 minutes
 */
export function useWeaknessStrength(request?: GetWeaknessStrengthRequest) {
  return useQuery({
    queryKey: personalizationQueryKeys.weaknessStrength(),
    queryFn: () => PersonalizationService.getWeaknessStrength(request),
    staleTime: 10 * 60 * 1000, // 10 minutes
    gcTime: 15 * 60 * 1000, // 15 minutes
  });
}

// ============================================
// ADAPTIVE DIFFICULTY HOOKS
// ============================================

/**
 * Hook to get adaptive difficulty settings
 * Caches for 10 minutes
 */
export function useAdaptiveDifficulty(request?: GetAdaptiveDifficultyRequest) {
  return useQuery({
    queryKey: personalizationQueryKeys.adaptiveDifficulty(),
    queryFn: () => PersonalizationService.getAdaptiveDifficulty(request),
    staleTime: 10 * 60 * 1000, // 10 minutes
    gcTime: 15 * 60 * 1000, // 15 minutes
  });
}

// ============================================
// DAILY PRACTICE HOOKS
// ============================================

/**
 * Hook to get daily practice exercises
 * Caches for 10 minutes
 */
export function useDailyPractice(request?: GetDailyPracticeRequest) {
  return useQuery({
    queryKey: personalizationQueryKeys.dailyPractice(),
    queryFn: () => PersonalizationService.getDailyPractice(request),
    staleTime: 10 * 60 * 1000, // 10 minutes
    gcTime: 15 * 60 * 1000, // 15 minutes
  });
}
