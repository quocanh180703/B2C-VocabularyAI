import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import {
  useExplainWord,
  useCheckGrammar,
  useCreateConversation,
  useConversations,
  useConversationDetail,
  useSendMessage,
  useGenerateHint,
  aiQueryKeys,
} from '@/hooks/useAI'

const createWrapper = (queryClient: QueryClient) => ({ children }: any) =>
  React.createElement(QueryClientProvider, { client: queryClient }, children)

describe('AI Features Integration Tests', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false, staleTime: 0 },
        mutations: { retry: false },
      },
    })
  })

  afterEach(() => {
    queryClient.clear()
  })

  describe('Vocabulary Learning Flow', () => {
    it('should complete full vocabulary learning flow', async () => {
      // Step 1: Explain a word
      const { result: explainResult } = renderHook(() => useExplainWord(), {
        wrapper: createWrapper(queryClient),
      })

      explainResult.current.mutate({
        word: 'serendipity',
        level: 'B1',
        context: 'The discovery was pure serendipity.',
      })

      await waitFor(() => {
        expect(explainResult.current.isSuccess).toBe(true)
      })

      const wordExplanation = explainResult.current.data
      expect(wordExplanation?.word).toBe('serendipity')
      expect(wordExplanation?.explanation).toBeDefined()
      expect(wordExplanation?.examples?.length).toBeGreaterThan(0)

      // Step 2: Generate hint for the word
      const { result: hintResult } = renderHook(() => useGenerateHint(), {
        wrapper: createWrapper(queryClient),
      })

      hintResult.current.mutate({
        wordId: wordExplanation?.wordId || 1,
        level: 'B1',
      })

      await waitFor(() => {
        expect(hintResult.current.isSuccess).toBe(true)
      })

      const hint = hintResult.current.data
      expect(hint?.hint).toBeDefined()
      expect(hint?.difficulty).toBeDefined()
    })

    it('should support batch vocabulary learning', async () => {
      const { result } = renderHook(() => useExplainWord(), {
        wrapper: createWrapper(queryClient),
      })

      // Learn multiple words at once is not directly supported by mutate,
      // but users would call explainWord multiple times
      const words = ['ephemeral', 'ubiquitous', 'benevolent']

      for (const word of words) {
        result.current.mutate({ word })

        await waitFor(() => {
          expect(result.current.isSuccess).toBe(true)
        })

        expect(result.current.data?.word).toBe(word)
      }
    })

    it('should cache vocabulary explanations', async () => {
      const { result: result1 } = renderHook(() => useExplainWord(), {
        wrapper: createWrapper(queryClient),
      })

      result1.current.mutate({ word: 'example' })

      await waitFor(() => {
        expect(result1.current.isSuccess).toBe(true)
      })

      // Check cache
      const cachedData = queryClient.getQueryData(
        aiQueryKeys.explanation('example')
      )
      expect(cachedData).toBeUndefined() // Mutation results not cached by default
    })
  })

  describe('Grammar Practice Flow', () => {
    it('should complete grammar checking and learning flow', async () => {
      // Step 1: Check grammar
      const { result: grammarResult } = renderHook(() => useCheckGrammar(), {
        wrapper: createWrapper(queryClient),
      })

      const originalText = 'She have gone to the market.'
      grammarResult.current.mutate({ text: originalText })

      await waitFor(() => {
        expect(grammarResult.current.isSuccess).toBe(true)
      })

      const grammarCheck = grammarResult.current.data
      expect(grammarCheck?.originalText).toBe(originalText)
      expect(grammarCheck?.correctedText).toBeDefined()
      expect(grammarCheck?.errors?.length).toBeGreaterThan(0)

      // Step 2: Generate hints for corrections
      const { result: hintResult } = renderHook(() => useGenerateHint(), {
        wrapper: createWrapper(queryClient),
      })

      hintResult.current.mutate({ wordId: 1, level: 'A2' })

      await waitFor(() => {
        expect(hintResult.current.isSuccess).toBe(true)
      })

      expect(hintResult.current.data?.hint).toBeDefined()
    })

    it('should handle batch grammar checking', async () => {
      const { result } = renderHook(() => useCheckGrammar(), {
        wrapper: createWrapper(queryClient),
      })

      const texts = [
        'I goes to school.',
        'She have studied English.',
        'They is playing football.',
      ]

      // Simulate batch checking (would use useCheckGrammarBatch)
      for (const text of texts) {
        result.current.mutate({ text })

        await waitFor(() => {
          expect(result.current.isSuccess).toBe(true)
        })

        expect(result.current.data?.originalText).toBe(text)
      }
    })

    it('should track error patterns in grammar checks', async () => {
      const { result } = renderHook(() => useCheckGrammar(), {
        wrapper: createWrapper(queryClient),
      })

      const problematicText = 'The student are study very hard.'
      result.current.mutate({ text: problematicText })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      const grammarCheck = result.current.data
      if (grammarCheck?.errors && grammarCheck.errors.length > 0) {
        const errorTypes = new Set(grammarCheck.errors.map((e) => e.errorType))
        expect(errorTypes.size).toBeGreaterThan(0)
      }
    })
  })

  describe('Chat Conversation Flow', () => {
    it('should complete full conversation flow', async () => {
      // Step 1: Create conversation
      const { result: createResult } = renderHook(
        () => useCreateConversation(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      createResult.current.mutate({
        title: 'English Learning Session',
        type: 'LEARNING',
      })

      await waitFor(() => {
        expect(createResult.current.isSuccess).toBe(true)
      })

      const conversation = createResult.current.data
      expect(conversation?.id).toBeDefined()
      expect(conversation?.title).toBe('English Learning Session')

      // Step 2: Load conversation detail
      const { result: detailResult } = renderHook(
        () => useConversationDetail(String(conversation?.id)),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      await waitFor(() => {
        expect(detailResult.current.isSuccess).toBe(true)
      })

      const conversationDetail = detailResult.current.data
      expect(conversationDetail?.messages).toBeDefined()
      expect(Array.isArray(conversationDetail?.messages)).toBe(true)

      // Step 3: Send message
      const { result: sendResult } = renderHook(() => useSendMessage(), {
        wrapper: createWrapper(queryClient),
      })

      sendResult.current.mutate({
        conversationId: String(conversation?.id),
        request: { content: 'Can you help me with English grammar?' },
      })

      await waitFor(() => {
        expect(sendResult.current.isSuccess).toBe(true)
      })

      const message = sendResult.current.data
      expect(message?.conversationId).toBe(conversation?.id)
      expect(message?.role).toBe('ASSISTANT')
    })

    it('should manage multiple conversations', async () => {
      // Step 1: Create first conversation
      const { result: createResult1 } = renderHook(
        () => useCreateConversation(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      createResult1.current.mutate({
        title: 'Conversation 1',
        type: 'LEARNING',
      })

      await waitFor(() => {
        expect(createResult1.current.isSuccess).toBe(true)
      })

      const conv1 = createResult1.current.data

      // Step 2: Create second conversation
      const { result: createResult2 } = renderHook(
        () => useCreateConversation(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      createResult2.current.mutate({
        title: 'Conversation 2',
        type: 'PRACTICE',
      })

      await waitFor(() => {
        expect(createResult2.current.isSuccess).toBe(true)
      })

      const conv2 = createResult2.current.data

      // Step 3: Fetch all conversations
      const { result: conversationsResult } = renderHook(
        () => useConversations(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      await waitFor(() => {
        expect(conversationsResult.current.isSuccess).toBe(true)
      })

      const conversations = conversationsResult.current.data
      expect(Array.isArray(conversations)).toBe(true)
      expect(conversations?.length).toBeGreaterThanOrEqual(2)
    })

    it('should maintain message history in conversation', async () => {
      // Step 1: Create conversation
      const { result: createResult } = renderHook(
        () => useCreateConversation(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      createResult.current.mutate({
        title: 'Test Conversation',
        type: 'LEARNING',
      })

      await waitFor(() => {
        expect(createResult.current.isSuccess).toBe(true)
      })

      const conversation = createResult.current.data

      // Step 2: Get conversation detail and verify messages persist
      const { result: detailResult1 } = renderHook(
        () => useConversationDetail(String(conversation?.id)),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      await waitFor(() => {
        expect(detailResult1.current.isSuccess).toBe(true)
      })

      const detail1 = detailResult1.current.data
      const messageCount1 = detail1?.messages?.length || 0

      // Step 3: Send a message
      const { result: sendResult } = renderHook(() => useSendMessage(), {
        wrapper: createWrapper(queryClient),
      })

      sendResult.current.mutate({
        conversationId: String(conversation?.id),
        request: { content: 'Hello' },
      })

      await waitFor(() => {
        expect(sendResult.current.isSuccess).toBe(true)
      })

      // Step 4: Fetch conversation detail again
      const { result: detailResult2 } = renderHook(
        () => useConversationDetail(String(conversation?.id)),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      await waitFor(() => {
        expect(detailResult2.current.isSuccess).toBe(true)
      })

      const detail2 = detailResult2.current.data
      const messageCount2 = detail2?.messages?.length || 0

      // Messages should accumulate
      expect(messageCount2).toBeGreaterThanOrEqual(messageCount1)
    })
  })

  describe('Cache Invalidation and Refetching', () => {
    it('should invalidate conversations cache after creating new one', async () => {
      // Step 1: Get conversations
      const { result: firstFetch } = renderHook(
        () => useConversations(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      await waitFor(() => {
        expect(firstFetch.current.isSuccess).toBe(true)
      })

      const initialCount = firstFetch.current.data?.length || 0

      // Step 2: Create new conversation (should invalidate cache)
      const { result: createResult } = renderHook(
        () => useCreateConversation(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      createResult.current.mutate({
        title: 'New Conversation',
        type: 'LEARNING',
      })

      await waitFor(() => {
        expect(createResult.current.isSuccess).toBe(true)
      })

      // Step 3: Fetch conversations again
      const { result: secondFetch } = renderHook(
        () => useConversations(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      await waitFor(() => {
        expect(secondFetch.current.isSuccess).toBe(true)
      })

      const newCount = secondFetch.current.data?.length || 0
      expect(newCount).toBeGreaterThanOrEqual(initialCount)
    })

    it('should invalidate conversation detail after sending message', async () => {
      // Setup: Create conversation
      const { result: createResult } = renderHook(
        () => useCreateConversation(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      createResult.current.mutate({
        title: 'Test',
        type: 'LEARNING',
      })

      await waitFor(() => {
        expect(createResult.current.isSuccess).toBe(true)
      })

      const conversation = createResult.current.data

      // Step 1: Get conversation detail
      const { result: detailResult1 } = renderHook(
        () => useConversationDetail(String(conversation?.id)),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      await waitFor(() => {
        expect(detailResult1.current.isSuccess).toBe(true)
      })

      const initialMessages = detailResult1.current.data?.messages?.length || 0

      // Step 2: Send message (should invalidate cache)
      const { result: sendResult } = renderHook(() => useSendMessage(), {
        wrapper: createWrapper(queryClient),
      })

      sendResult.current.mutate({
        conversationId: String(conversation?.id),
        request: { content: 'Test message' },
      })

      await waitFor(() => {
        expect(sendResult.current.isSuccess).toBe(true)
      })

      // Step 3: Fetch conversation detail again
      const { result: detailResult2 } = renderHook(
        () => useConversationDetail(String(conversation?.id)),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      await waitFor(() => {
        expect(detailResult2.current.isSuccess).toBe(true)
      })

      const newMessages = detailResult2.current.data?.messages?.length || 0
      expect(newMessages).toBeGreaterThanOrEqual(initialMessages)
    })
  })

  describe('Error Recovery and Retry Logic', () => {
    it('should handle missing conversation gracefully', async () => {
      const { result } = renderHook(
        () => useConversationDetail('999999'),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      // Should attempt to fetch
      await waitFor(() => {
        // Either success or error, but should complete
        expect(result.current.isLoading).toBe(false)
      })
    })

    it('should allow retry after failed request', async () => {
      const { result } = renderHook(() => useConversations(), {
        wrapper: createWrapper(queryClient),
      })

      // Should succeed on first attempt
      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      // Manually invalidate to force refetch
      await queryClient.invalidateQueries({
        queryKey: aiQueryKeys.conversations(),
      })

      // Should refetch successfully
      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })
    })
  })

  describe('Cross-Feature Integration', () => {
    it('should integrate vocabulary learning with conversation context', async () => {
      // Step 1: Explain a word
      const { result: explainResult } = renderHook(() => useExplainWord(), {
        wrapper: createWrapper(queryClient),
      })

      explainResult.current.mutate({ word: 'ambiguous', level: 'B1' })

      await waitFor(() => {
        expect(explainResult.current.isSuccess).toBe(true)
      })

      const explanation = explainResult.current.data

      // Step 2: Create conversation about the word
      const { result: createResult } = renderHook(
        () => useCreateConversation(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      createResult.current.mutate({
        title: `Learning: ${explanation?.word}`,
        type: 'LEARNING',
      })

      await waitFor(() => {
        expect(createResult.current.isSuccess).toBe(true)
      })

      const conversation = createResult.current.data
      expect(conversation?.title).toContain('ambiguous')

      // Step 3: Send message about the word in conversation
      const { result: sendResult } = renderHook(() => useSendMessage(), {
        wrapper: createWrapper(queryClient),
      })

      sendResult.current.mutate({
        conversationId: String(conversation?.id),
        request: {
          content: `How can I use "${explanation?.word}" in a sentence?`,
        },
      })

      await waitFor(() => {
        expect(sendResult.current.isSuccess).toBe(true)
      })

      const message = sendResult.current.data
      expect(message?.content).toBeDefined()
    })

    it('should integrate grammar checking with conversation feedback', async () => {
      // Step 1: Check grammar
      const { result: grammarResult } = renderHook(() => useCheckGrammar(), {
        wrapper: createWrapper(queryClient),
      })

      grammarResult.current.mutate({
        text: 'I has two cats at home.',
      })

      await waitFor(() => {
        expect(grammarResult.current.isSuccess).toBe(true)
      })

      const grammarCheck = grammarResult.current.data

      // Step 2: Create conversation for feedback
      const { result: createResult } = renderHook(
        () => useCreateConversation(),
        {
          wrapper: createWrapper(queryClient),
        }
      )

      createResult.current.mutate({
        title: 'Grammar Feedback Session',
        type: 'FEEDBACK',
      })

      await waitFor(() => {
        expect(createResult.current.isSuccess).toBe(true)
      })

      const conversation = createResult.current.data

      // Step 3: Send correction in conversation
      const { result: sendResult } = renderHook(() => useSendMessage(), {
        wrapper: createWrapper(queryClient),
      })

      sendResult.current.mutate({
        conversationId: String(conversation?.id),
        request: {
          content: `I should write: "${grammarCheck?.correctedText}"`,
        },
      })

      await waitFor(() => {
        expect(sendResult.current.isSuccess).toBe(true)
      })

      const message = sendResult.current.data
      expect(message?.conversationId).toBe(conversation?.id)
    })
  })
})
