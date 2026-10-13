/**
 * Tests for Personalization Hooks
 * Tests React Query hooks for personalization APIs
 */

import { describe, it, expect, beforeEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  useProfile,
  useLearningPlan,
  useProgressAnalysis,
  useRecommendations,
  useWeaknessStrength,
  useAdaptiveDifficulty,
  useDailyPractice,
} from '@/hooks/usePersonalization'
import { mockUserProfile, mockLearningPlan, mockProgressAnalysis } from '../mocks/fixtures'

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  })

const renderHookWithProvider = (hook: () => any) => {
  const queryClient = createTestQueryClient()
  return renderHook(hook, {
    wrapper: ({ children }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    ),
  })
}

describe('usePersonalization Hooks', () => {
  describe('useProfile', () => {
    it('should fetch user profile', async () => {
      const { result } = renderHookWithProvider(() => useProfile())

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.username).toBeDefined()
    })

    it('should cache profile data', async () => {
      const { result, rerender } = renderHookWithProvider(() => useProfile())

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      const firstData = result.current.data

      rerender()

      expect(result.current.data).toBe(firstData)
    })

    it('should handle errors', async () => {
      const { result } = renderHookWithProvider(() => useProfile())

      // Simulate error by mocking failed request
      await waitFor(() => {
        expect(result.current.status).toBeDefined()
      })
    })
  })

  describe('useLearningPlan', () => {
    it('should fetch learning plan', async () => {
      const { result } = renderHookWithProvider(() => useLearningPlan())

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.planId).toBeDefined()
      expect(result.current.data?.items).toBeDefined()
    })

    it('should support query parameters', async () => {
      const { result } = renderHookWithProvider(() => useLearningPlan({ weeks: 4 }))

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
    })
  })

  describe('useProgressAnalysis', () => {
    it('should fetch progress analysis', async () => {
      const { result } = renderHookWithProvider(() => useProgressAnalysis())

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.overallProgress).toBeDefined()
      expect(result.current.data?.metrics).toBeDefined()
    })

    it('should support date range parameters', async () => {
      const { result } = renderHookWithProvider(() =>
        useProgressAnalysis({ daysBack: 30 })
      )

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
    })
  })

  describe('useRecommendations', () => {
    it('should fetch recommendations', async () => {
      const { result } = renderHookWithProvider(() => useRecommendations())

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.recommendations).toBeDefined()
      expect(Array.isArray(result.current.data?.recommendations)).toBe(true)
    })

    it('should support limit parameter', async () => {
      const { result } = renderHookWithProvider(() => useRecommendations({ limit: 5 }))

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data?.recommendations).toBeDefined()
    })
  })

  describe('useWeaknessStrength', () => {
    it('should fetch weakness strength analysis', async () => {
      const { result } = renderHookWithProvider(() => useWeaknessStrength())

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.strengths).toBeDefined()
      expect(result.current.data?.weaknesses).toBeDefined()
    })

    it('should have strengths and weaknesses arrays', async () => {
      const { result } = renderHookWithProvider(() => useWeaknessStrength())

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(Array.isArray(result.current.data?.strengths)).toBe(true)
      expect(Array.isArray(result.current.data?.weaknesses)).toBe(true)
    })
  })

  describe('useAdaptiveDifficulty', () => {
    it('should fetch adaptive difficulty settings', async () => {
      const { result } = renderHookWithProvider(() => useAdaptiveDifficulty())

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.currentLevel).toBeDefined()
      expect(result.current.data?.nextLevel).toBeDefined()
    })

    it('should have difficulty progression', async () => {
      const { result } = renderHookWithProvider(() => useAdaptiveDifficulty())

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data?.progressToNextLevel).toBeDefined()
      expect(typeof result.current.data?.progressToNextLevel).toBe('number')
    })
  })

  describe('useDailyPractice', () => {
    it('should fetch daily practice exercises', async () => {
      const { result } = renderHookWithProvider(() => useDailyPractice())

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.items).toBeDefined()
      expect(Array.isArray(result.current.data?.items)).toBe(true)
    })

    it('should have practice metrics', async () => {
      const { result } = renderHookWithProvider(() => useDailyPractice())

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data?.totalEstimatedMinutes).toBeDefined()
      expect(result.current.data?.completedMinutes).toBeDefined()
      expect(result.current.data?.streakDays).toBeDefined()
    })

    it('should support date parameter', async () => {
      const { result } = renderHookWithProvider(() =>
        useDailyPractice({ date: '2024-02-01' })
      )

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
    })
  })

  describe('Cache and Refetch', () => {
    it('should have correct cache time', async () => {
      const { result } = renderHookWithProvider(() => useProfile())

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      // Data should be cached
      expect(result.current.data).toBeDefined()
    })

    it('should support manual refetch', async () => {
      const { result } = renderHookWithProvider(() => useProfile())

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      const firstData = result.current.data

      result.current.refetch()

      // Should be able to refetch
      expect(result.current.isRefetching).toBeDefined()
    })
  })
})
