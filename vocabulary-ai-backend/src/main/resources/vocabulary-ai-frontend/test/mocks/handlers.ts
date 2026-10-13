import { http, HttpResponse } from 'msw'
import {
  mockExplanationResponse,
  mockGrammarCheckResponse,
  mockConversation,
  mockConversationDetail,
  mockMessage,
  mockHintResponse,
  mockUserProfile,
  mockLearningPlan,
  mockProgressAnalysis,
  mockRecommendationsResponse,
  mockWeaknessStrengthAnalysis,
  mockAdaptiveDifficulty,
  mockDailyPractice,
  mockSpeakingSession,
  mockSpeakingSessionList,
  mockSpeakingAnswer,
  mockSpeakingFeedback,
} from './fixtures'

// Pattern-based handlers that work with any base URL
export const handlers = [
  // ============================================
  // VOCABULARY ENDPOINTS
  // ============================================

  // Explain single word
  http.post('*/api/v1/ai/vocabulary/explain', async ({ request }) => {
    const body = (await request.json()) as Record<string, any>
    return HttpResponse.json(
      {
        success: true,
        data: mockExplanationResponse({
          word: body.word || 'example',
        }),
      },
      { status: 200 }
    )
  }),

  // Explain batch words
  http.post('*/api/v1/ai/vocabulary/explain-batch', async ({ request }) => {
    const body = (await request.json()) as { words: Record<string, any>[] }
    return HttpResponse.json(
      {
        success: true,
        data: (body.words || []).map((w, idx) =>
          mockExplanationResponse({ word: w.word || `word${idx}` })
        ),
      },
      { status: 200 }
    )
  }),

  // Cache clear vocabulary
  http.post('*/api/v1/ai/vocabulary/cache-clear', () => {
    return HttpResponse.json(
      {
        success: true,
        message: 'Vocabulary cache cleared',
      },
      { status: 200 }
    )
  }),

  // ============================================
  // GRAMMAR ENDPOINTS
  // ============================================

  // Check grammar single text
  http.post('*/api/v1/ai/grammar/check', async ({ request }) => {
    const body = (await request.json()) as Record<string, any>
    return HttpResponse.json(
      {
        success: true,
        data: mockGrammarCheckResponse(body.text || 'Sample text'),
      },
      { status: 200 }
    )
  }),

  // Check grammar batch
  http.post('*/api/v1/ai/grammar/check-batch', async ({ request }) => {
    const body = (await request.json()) as { texts: string[] }
    return HttpResponse.json(
      {
        success: true,
        data: (body.texts || []).map((text) => mockGrammarCheckResponse(text)),
      },
      { status: 200 }
    )
  }),

  // Cache clear grammar
  http.post('*/api/v1/ai/grammar/cache-clear', () => {
    return HttpResponse.json(
      {
        success: true,
        message: 'Grammar cache cleared',
      },
      { status: 200 }
    )
  }),

  // ============================================
  // CHAT ENDPOINTS
  // ============================================

  // Create conversation
  http.post('*/api/v1/ai/chat/conversations', async ({ request }) => {
    const body = (await request.json()) as Record<string, any>
    return HttpResponse.json(
      {
        success: true,
        data: mockConversation({
          title: body.title || 'New Conversation',
          type: body.type || 'LEARNING',
        }),
      },
      { status: 201 }
    )
  }),

  // Get conversations list
  http.get('*/api/v1/ai/chat/conversations', () => {
    return HttpResponse.json(
      {
        success: true,
        data: [
          mockConversation({ id: 1 }),
          mockConversation({ id: 2 }),
          mockConversation({ id: 3 }),
        ],
      },
      { status: 200 }
    )
  }),

  // Get conversation detail
  http.get('*/api/v1/ai/chat/conversations/:conversationId', ({ params }) => {
    return HttpResponse.json(
      {
        success: true,
        data: mockConversationDetail({
          id: Number(params.conversationId),
        }),
      },
      { status: 200 }
    )
  }),

  // Send message
  http.post(
    '*/api/v1/ai/chat/conversations/:conversationId/messages',
    async ({ request, params }) => {
      const body = (await request.json()) as Record<string, any>
      return HttpResponse.json(
        {
          success: true,
          data: mockMessage({
            conversationId: Number(params.conversationId),
            content: body.content || 'Response message',
            role: 'ASSISTANT',
          }),
        },
        { status: 201 }
      )
    }
  ),

  // Delete conversation
  http.delete('*/api/v1/ai/chat/conversations/:conversationId', () => {
    return HttpResponse.json(
      {
        success: true,
        message: 'Conversation deleted',
      },
      { status: 200 }
    )
  }),

  // ============================================
  // HINTS ENDPOINTS
  // ============================================

  // Generate hint
  http.post('*/api/v1/ai/hints/generate', async ({ request }) => {
    const body = (await request.json()) as Record<string, any>
    return HttpResponse.json(
      {
        success: true,
        data: mockHintResponse(body.wordId || 1),
      },
      { status: 200 }
    )
  }),

  // Generate hints batch
  http.post('*/api/v1/ai/hints/generate-batch', async ({ request }) => {
    const body = (await request.json()) as { hints: Record<string, any>[] }
    return HttpResponse.json(
      {
        success: true,
        data: (body.hints || []).map((h, idx) => mockHintResponse(h.wordId || idx + 1)),
      },
      { status: 200 }
    )
  }),

  // Cache clear hints
  http.post('*/api/v1/ai/hints/cache-clear', () => {
    return HttpResponse.json(
      {
        success: true,
        message: 'Hints cache cleared',
      },
      { status: 200 }
    )
  }),

  // ============================================
  // PERSONALIZATION ENDPOINTS
  // ============================================

  // Get profile
  http.get('*/api/v1/ai/personalization/profile', () => {
    return HttpResponse.json(
      {
        success: true,
        data: mockUserProfile(),
      },
      { status: 200 }
    )
  }),

  // Get learning plan
  http.get('*/api/v1/ai/personalization/learning-plan', () => {
    return HttpResponse.json(
      {
        success: true,
        data: mockLearningPlan(),
      },
      { status: 200 }
    )
  }),

  // Get progress analysis
  http.get('*/api/v1/ai/personalization/progress-analysis', () => {
    return HttpResponse.json(
      {
        success: true,
        data: mockProgressAnalysis(),
      },
      { status: 200 }
    )
  }),

  // Get recommendations
  http.get('*/api/v1/ai/personalization/recommendations', () => {
    return HttpResponse.json(
      {
        success: true,
        data: mockRecommendationsResponse(),
      },
      { status: 200 }
    )
  }),

  // Get weakness strength
  http.get('*/api/v1/ai/personalization/weakness-strength', () => {
    return HttpResponse.json(
      {
        success: true,
        data: mockWeaknessStrengthAnalysis(),
      },
      { status: 200 }
    )
  }),

  // Get adaptive difficulty
  http.get('*/api/v1/ai/personalization/adaptive-difficulty', () => {
    return HttpResponse.json(
      {
        success: true,
        data: mockAdaptiveDifficulty(),
      },
      { status: 200 }
    )
  }),

  // Get daily practice
  http.get('*/api/v1/ai/personalization/daily-practice', () => {
    return HttpResponse.json(
      {
        success: true,
        data: mockDailyPractice(),
      },
      { status: 200 }
    )
  }),

  // ============================================
  // SPEAKING ENDPOINTS
  // ============================================

  // Create speaking session
  http.post('*/api/v1/ai/speaking/sessions', async ({ request }) => {
    const body = (await request.json()) as Record<string, any>
    return HttpResponse.json(
      {
        success: true,
        data: mockSpeakingSession({
          title: body.title || 'Speaking Session',
          topic: body.topic || 'General',
          difficulty: body.difficulty || 'B1',
        }),
      },
      { status: 201 }
    )
  }),

  // Get speaking sessions
  http.get('*/api/v1/ai/speaking/sessions', () => {
    return HttpResponse.json(
      {
        success: true,
        data: mockSpeakingSessionList(),
      },
      { status: 200 }
    )
  }),

  // Get speaking session by ID
  http.get('*/api/v1/ai/speaking/sessions/:sessionId', ({ params }) => {
    return HttpResponse.json(
      {
        success: true,
        data: mockSpeakingSession({
          id: Number(params.sessionId),
        }),
      },
      { status: 200 }
    )
  }),

  // Submit speaking answer
  http.post(
    '*/api/v1/ai/speaking/sessions/:sessionId/answers',
    async ({ request, params }) => {
      const body = await request.arrayBuffer()
      return HttpResponse.json(
        {
          success: true,
          data: mockSpeakingAnswer({
            sessionId: Number(params.sessionId),
          }),
        },
        { status: 201 }
      )
    }
  ),

  // Get speaking feedback
  http.get('*/api/v1/ai/speaking/sessions/:sessionId/feedback', ({ params }) => {
    return HttpResponse.json(
      {
        success: true,
        data: mockSpeakingFeedback(),
      },
      { status: 200 }
    )
  }),

  // Complete speaking session
  http.post('*/api/v1/ai/speaking/sessions/:sessionId/complete', ({ params }) => {
    return HttpResponse.json(
      {
        success: true,
        data: mockSpeakingSession({
          id: Number(params.sessionId),
          status: 'completed',
          score: 87,
        }),
      },
      { status: 200 }
    )
  }),
]
