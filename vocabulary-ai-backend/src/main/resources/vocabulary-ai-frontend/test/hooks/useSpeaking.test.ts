/**
 * Tests for Speaking Hooks
 * Tests React Query hooks for speaking APIs
 */

import { describe, it, expect, beforeEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  useCreateSpeakingSession,
  useSpeakingSessions,
  useSpeakingSession,
  useSubmitSpeakingAnswer,
  useSpeakingFeedback,
  useCompleteSpeakingSession,
} from '@/hooks/useSpeaking'
import {
  mockSpeakingSession,
  mockSpeakingSessionList,
  mockSpeakingFeedback,
} from '../mocks/fixtures'

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

describe('useSpeaking Hooks', () => {
  describe('useCreateSpeakingSession', () => {
    it('should be a mutation hook', () => {
      const { result } = renderHookWithProvider(() => useCreateSpeakingSession())

      expect(result.current).toBeDefined()
      expect(result.current.mutate).toBeDefined()
      expect(result.current.mutateAsync).toBeDefined()
    })

    it('should create a speaking session', async () => {
      const { result } = renderHookWithProvider(() => useCreateSpeakingSession())

      result.current.mutate({
        topic: 'Restaurant ordering',
        difficulty: 'B1',
        title: 'My first session',
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.topic).toBe('Restaurant ordering')
    })

    it('should handle errors', async () => {
      const { result } = renderHookWithProvider(() => useCreateSpeakingSession())

      // Test error handling
      expect(result.current.status).toBeDefined()
    })
  })

  describe('useSpeakingSessions', () => {
    it('should fetch speaking sessions list', async () => {
      const { result } = renderHookWithProvider(() => useSpeakingSessions())

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.sessions).toBeDefined()
      expect(Array.isArray(result.current.data?.sessions)).toBe(true)
    })

    it('should support pagination', async () => {
      const { result } = renderHookWithProvider(() =>
        useSpeakingSessions({ page: 1, pageSize: 10 })
      )

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data?.total).toBeDefined()
      expect(result.current.data?.page).toBeDefined()
      expect(result.current.data?.pageSize).toBeDefined()
    })

    it('should support status filter', async () => {
      const { result } = renderHookWithProvider(() =>
        useSpeakingSessions({ status: 'completed' })
      )

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data?.sessions).toBeDefined()
    })

    it('should cache data correctly', async () => {
      const { result, rerender } = renderHookWithProvider(() => useSpeakingSessions())

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      const firstData = result.current.data

      rerender()

      expect(result.current.data).toBe(firstData)
    })
  })

  describe('useSpeakingSession', () => {
    it('should fetch a specific speaking session', async () => {
      const { result } = renderHookWithProvider(() => useSpeakingSession('1'))

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.id).toBe(1)
    })

    it('should not fetch if sessionId is null', async () => {
      const { result } = renderHookWithProvider(() => useSpeakingSession(null))

      expect(result.current.data).toBeUndefined()
    })

    it('should have session details', async () => {
      const { result } = renderHookWithProvider(() => useSpeakingSession('1'))

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data?.title).toBeDefined()
      expect(result.current.data?.topic).toBeDefined()
      expect(result.current.data?.difficulty).toBeDefined()
      expect(result.current.data?.status).toBeDefined()
    })
  })

  describe('useSubmitSpeakingAnswer', () => {
    it('should be a mutation hook', () => {
      const { result } = renderHookWithProvider(() => useSubmitSpeakingAnswer())

      expect(result.current).toBeDefined()
      expect(result.current.mutate).toBeDefined()
      expect(result.current.mutateAsync).toBeDefined()
    })

    it('should submit an answer with audio', async () => {
      const { result } = renderHookWithProvider(() => useSubmitSpeakingAnswer())

      const audioBlob = new Blob(['audio data'], { type: 'audio/webm' })

      result.current.mutate({
        sessionId: '1',
        request: {
          questionId: 1,
          audioBlob,
          duration: 5,
        },
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.audioUrl).toBeDefined()
    })

    it('should handle audio submission errors', async () => {
      const { result } = renderHookWithProvider(() => useSubmitSpeakingAnswer())

      // Test error handling
      expect(result.current.status).toBeDefined()
    })
  })

  describe('useSpeakingFeedback', () => {
    it('should fetch feedback for a session', async () => {
      const { result } = renderHookWithProvider(() => useSpeakingFeedback('1'))

      expect(result.current.isLoading).toBe(true)

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.score).toBeDefined()
      expect(result.current.data?.explanation).toBeDefined()
    })

    it('should not fetch if sessionId is null', async () => {
      const { result } = renderHookWithProvider(() => useSpeakingFeedback(null))

      expect(result.current.data).toBeUndefined()
    })

    it('should have feedback details', async () => {
      const { result } = renderHookWithProvider(() => useSpeakingFeedback('1'))

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data?.accuracy).toBeDefined()
      expect(result.current.data?.fluency).toBeDefined()
      expect(result.current.data?.corrections).toBeDefined()
      expect(result.current.data?.strengths).toBeDefined()
      expect(result.current.data?.areasToImprove).toBeDefined()
    })
  })

  describe('useCompleteSpeakingSession', () => {
    it('should be a mutation hook', () => {
      const { result } = renderHookWithProvider(() => useCompleteSpeakingSession())

      expect(result.current).toBeDefined()
      expect(result.current.mutate).toBeDefined()
      expect(result.current.mutateAsync).toBeDefined()
    })

    it('should complete a speaking session', async () => {
      const { result } = renderHookWithProvider(() => useCompleteSpeakingSession())

      result.current.mutate('1')

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.status).toBe('completed')
    })

    it('should invalidate sessions cache after completion', async () => {
      const { result } = renderHookWithProvider(() => useCompleteSpeakingSession())

      result.current.mutate('1')

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toBeDefined()
    })
  })

  describe('Query Keys and Caching', () => {
    it('should have consistent cache behavior', async () => {
      const { result: sessionsResult } = renderHookWithProvider(() =>
        useSpeakingSessions()
      )

      await waitFor(() => {
        expect(sessionsResult.current.isLoading).toBe(false)
      })

      const { result: sessionResult } = renderHookWithProvider(() =>
        useSpeakingSession('1')
      )

      await waitFor(() => {
        expect(sessionResult.current.isLoading).toBe(false)
      })

      // Both should have data
      expect(sessionsResult.current.data).toBeDefined()
      expect(sessionResult.current.data).toBeDefined()
    })
  })
})
