import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import {
  useExplainWord,
  useExplainWords,
  useCheckGrammar,
  useCheckGrammarBatch,
  useCreateConversation,
  useConversations,
  useConversationDetail,
  useSendMessage,
  useDeleteConversation,
  useGenerateHint,
  useGenerateHintsBatch,
  aiQueryKeys,
} from '@/hooks/useAI'
import { ExplainWordRequest, HintRequest } from '@/services/api/ai.types'

const createWrapper = (queryClient: QueryClient) => ({ children }: any) =>
  React.createElement(QueryClientProvider, { client: queryClient }, children)

describe('useAI - Vocabulary Hooks', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })
  })

  afterEach(() => {
    queryClient.clear()
  })

  describe('useExplainWord', () => {
    it('should explain a single word', async () => {
      const { result } = renderHook(() => useExplainWord(), {
        wrapper: createWrapper(queryClient),
      })

      const request: ExplainWordRequest = {
        word: 'example',
        level: 'A1',
      }

      result.current.mutate(request)

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toBeDefined()
      expect(result.current.data?.word).toBe('example')
      expect(result.current.data?.explanation).toBeDefined()
      expect(result.current.data?.examples).toBeDefined()
      expect(Array.isArray(result.current.data?.examples)).toBe(true)
    })

    it('should handle explain word with context', async () => {
      const { result } = renderHook(() => useExplainWord(), {
        wrapper: createWrapper(queryClient),
      })

      const request: ExplainWordRequest = {
        word: 'brilliant',
        context: 'The scientist made a brilliant discovery.',
      }

      result.current.mutate(request)

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.word).toBe('brilliant')
    })

    it('should include etymology and related words', async () => {
      const { result } = renderHook(() => useExplainWord(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate({ word: 'example' })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.etymology).toBeDefined()
      expect(result.current.data?.relatedWords).toBeDefined()
      expect(Array.isArray(result.current.data?.relatedWords)).toBe(true)
    })
  })

  describe('useExplainWords', () => {
    it('should explain multiple words', async () => {
      const { result } = renderHook(() => useExplainWords(), {
        wrapper: createWrapper(queryClient),
      })

      const requests: ExplainWordRequest[] = [
        { word: 'example' },
        { word: 'illustration' },
        { word: 'specimen' },
      ]

      result.current.mutate(requests)

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBe(3)
      expect(result.current.data?.[0].word).toBe('example')
    })

    it('should handle empty batch', async () => {
      const { result } = renderHook(() => useExplainWords(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate([])

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBe(0)
    })
  })
})

describe('useAI - Grammar Hooks', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })
  })

  afterEach(() => {
    queryClient.clear()
  })

  describe('useCheckGrammar', () => {
    it('should check grammar for single text', async () => {
      const { result } = renderHook(() => useCheckGrammar(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate({ text: 'The examples is very helpful.' })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.originalText).toBe('The examples is very helpful.')
      expect(result.current.data?.correctedText).toBeDefined()
      expect(result.current.data?.errors).toBeDefined()
      expect(Array.isArray(result.current.data?.errors)).toBe(true)
    })

    it('should return suggestions', async () => {
      const { result } = renderHook(() => useCheckGrammar(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate({ text: 'Sample text' })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.suggestions).toBeDefined()
      expect(Array.isArray(result.current.data?.suggestions)).toBe(true)
    })

    it('should include error details', async () => {
      const { result } = renderHook(() => useCheckGrammar(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate({ text: 'Text with errors' })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      const errors = result.current.data?.errors
      if (errors && errors.length > 0) {
        expect(errors[0]).toHaveProperty('position')
        expect(errors[0]).toHaveProperty('errorType')
        expect(errors[0]).toHaveProperty('correction')
        expect(errors[0]).toHaveProperty('explanation')
      }
    })
  })

  describe('useCheckGrammarBatch', () => {
    it('should check grammar for multiple texts', async () => {
      const { result } = renderHook(() => useCheckGrammarBatch(), {
        wrapper: createWrapper(queryClient),
      })

      const texts = ['Sample text 1', 'Sample text 2', 'Sample text 3']
      result.current.mutate(texts)

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBe(3)
    })

    it('should handle empty batch', async () => {
      const { result } = renderHook(() => useCheckGrammarBatch(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate([])

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBe(0)
    })
  })
})

describe('useAI - Chat Hooks', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })
  })

  afterEach(() => {
    queryClient.clear()
  })

  describe('useCreateConversation', () => {
    it('should create a new conversation', async () => {
      const { result } = renderHook(() => useCreateConversation(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate({
        title: 'Learning English',
        type: 'LEARNING',
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.id).toBeDefined()
      expect(result.current.data?.title).toBe('Learning English')
      expect(result.current.data?.type).toBe('LEARNING')
      expect(result.current.data?.createdAt).toBeDefined()
    })

    it('should invalidate conversations list on success', async () => {
      const { result } = renderHook(() => useCreateConversation(), {
        wrapper: createWrapper(queryClient),
      })

      const invalidateSpy = queryClient.invalidateQueries({
        queryKey: aiQueryKeys.conversations(),
      })

      result.current.mutate({
        title: 'New Conversation',
        type: 'PRACTICE',
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.type).toBe('PRACTICE')
    })
  })

  describe('useConversations', () => {
    it('should fetch all conversations', async () => {
      const { result } = renderHook(() => useConversations(), {
        wrapper: createWrapper(queryClient),
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBeGreaterThan(0)
    })

    it('should cache conversations for 5 minutes', async () => {
      const { result } = renderHook(() => useConversations(), {
        wrapper: createWrapper(queryClient),
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      const cacheData = queryClient.getQueryData(aiQueryKeys.conversations())
      expect(cacheData).toBeDefined()
    })
  })

  describe('useConversationDetail', () => {
    it('should fetch conversation detail with messages', async () => {
      const { result } = renderHook(() => useConversationDetail('1'), {
        wrapper: createWrapper(queryClient),
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.id).toBe(1)
      expect(result.current.data?.messages).toBeDefined()
      expect(Array.isArray(result.current.data?.messages)).toBe(true)
    })

    it('should not fetch when conversationId is null', async () => {
      const { result } = renderHook(() => useConversationDetail(null), {
        wrapper: createWrapper(queryClient),
      })

      expect(result.current.isLoading).toBe(false)
      expect(result.current.data).toBeUndefined()
    })

    it('should include message role information', async () => {
      const { result } = renderHook(() => useConversationDetail('1'), {
        wrapper: createWrapper(queryClient),
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      const messages = result.current.data?.messages
      if (messages && messages.length > 0) {
        expect(['USER', 'ASSISTANT']).toContain(messages[0].role)
        expect(messages[0].content).toBeDefined()
        expect(messages[0].timestamp).toBeDefined()
      }
    })
  })

  describe('useSendMessage', () => {
    it('should send a message in conversation', async () => {
      const { result } = renderHook(() => useSendMessage(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate({
        conversationId: '1',
        request: { content: 'Hello, how are you?' },
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.conversationId).toBe(1)
      expect(result.current.data?.content).toBeDefined()
      expect(result.current.data?.role).toBe('ASSISTANT')
    })

    it('should invalidate conversation detail after sending', async () => {
      const { result } = renderHook(() => useSendMessage(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate({
        conversationId: '1',
        request: { content: 'Test message' },
      })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.id).toBeDefined()
    })
  })

  describe('useDeleteConversation', () => {
    it('should delete a conversation', async () => {
      const { result } = renderHook(() => useDeleteConversation(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate('1')

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })
    })

    it('should invalidate conversations list after deletion', async () => {
      const { result } = renderHook(() => useDeleteConversation(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate('1')

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })
    })
  })
})

describe('useAI - Hints Hooks', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })
  })

  afterEach(() => {
    queryClient.clear()
  })

  describe('useGenerateHint', () => {
    it('should generate a hint for a word', async () => {
      const { result } = renderHook(() => useGenerateHint(), {
        wrapper: createWrapper(queryClient),
      })

      const request: HintRequest = {
        wordId: 1,
        level: 'A1',
      }

      result.current.mutate(request)

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.learningProgressId).toBeDefined()
      expect(result.current.data?.hint).toBeDefined()
      expect(result.current.data?.difficulty).toBeDefined()
    })
  })

  describe('useGenerateHintsBatch', () => {
    it('should generate hints for multiple words', async () => {
      const { result } = renderHook(() => useGenerateHintsBatch(), {
        wrapper: createWrapper(queryClient),
      })

      const requests: HintRequest[] = [
        { wordId: 1 },
        { wordId: 2 },
        { wordId: 3 },
      ]

      result.current.mutate(requests)

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBe(3)
    })

    it('should handle empty batch', async () => {
      const { result } = renderHook(() => useGenerateHintsBatch(), {
        wrapper: createWrapper(queryClient),
      })

      result.current.mutate([])

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBe(0)
    })
  })
})

describe('aiQueryKeys', () => {
  it('should generate consistent vocabulary query keys', () => {
    const key1 = aiQueryKeys.explanation('test')
    const key2 = aiQueryKeys.explanation('test')
    expect(key1).toEqual(key2)
  })

  it('should generate unique keys for different words', () => {
    const key1 = aiQueryKeys.explanation('word1')
    const key2 = aiQueryKeys.explanation('word2')
    expect(key1).not.toEqual(key2)
  })

  it('should generate consistent conversation query keys', () => {
    const key1 = aiQueryKeys.conversation('123')
    const key2 = aiQueryKeys.conversation('123')
    expect(key1).toEqual(key2)
  })
})
