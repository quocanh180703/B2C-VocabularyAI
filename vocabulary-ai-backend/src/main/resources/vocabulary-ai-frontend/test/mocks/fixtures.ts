import {
  ExplanationResponse,
  GrammarCheckResponse,
  GrammarError,
  GrammarSuggestion,
  ConversationResponse,
  ConversationDetailResponse,
  MessageResponse,
  HintResponse,
} from '@/services/api/ai.types'
import type {
  UserProfile,
  LearningPlan,
  ProgressAnalysis,
  RecommendationsResponse,
  WeaknessStrengthAnalysis,
  AdaptiveDifficulty,
  DailyPractice,
} from '@/services/api/personalization.types'
import type {
  SpeakingSession,
  SpeakingSessionList,
  SpeakingAnswer,
  SpeakingFeedback,
} from '@/services/api/speaking.types'

// ============================================
// VOCABULARY FIXTURES
// ============================================

export const mockExplanationResponse = (
  overrides?: Partial<ExplanationResponse>
): ExplanationResponse => ({
  wordId: 1,
  word: 'example',
  explanation: 'A thing characteristic of its kind or illustrating a general rule; a specimen, sample, or instance.',
  examples: [
    'Can you give me an example?',
    'That is a good example of the problem.',
    'For example, the United States is a republic.',
  ],
  etymology: 'From Latin exemplum, meaning "sample, model".',
  relatedWords: ['instance', 'specimen', 'sample', 'case'],
  partOfSpeech: 'noun',
  ...overrides,
})

// ============================================
// GRAMMAR FIXTURES
// ============================================

const mockGrammarError = (overrides?: Partial<GrammarError>): GrammarError => ({
  position: 5,
  errorType: 'GRAMMAR',
  original: 'is',
  correction: 'are',
  explanation: 'Subject-verb agreement: plural subject requires plural verb',
  ...overrides,
})

const mockGrammarSuggestion = (overrides?: Partial<GrammarSuggestion>): GrammarSuggestion => ({
  title: 'Consider a more formal tone',
  content: 'The sentence uses informal language. Consider using "example" instead of "case".',
  priority: 'LOW',
  ...overrides,
})

export const mockGrammarCheckResponse = (
  originalText: string = 'The examples is very helpful.',
  overrides?: Partial<GrammarCheckResponse>
): GrammarCheckResponse => ({
  originalText,
  correctedText: 'The examples are very helpful.',
  errors: [
    mockGrammarError({
      position: 12,
      original: 'is',
      correction: 'are',
    }),
  ],
  suggestions: [mockGrammarSuggestion()],
  ...overrides,
})

// ============================================
// CHAT FIXTURES
// ============================================

export const mockConversation = (
  overrides?: Partial<ConversationResponse>
): ConversationResponse => ({
  id: 1,
  title: 'English Conversation Practice',
  type: 'LEARNING',
  summary: 'Discussing daily life and routines',
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  ...overrides,
})

export const mockMessage = (
  overrides?: Partial<MessageResponse>
): MessageResponse => ({
  id: 1,
  conversationId: 1,
  content: 'Hello, how are you today?',
  role: 'USER',
  timestamp: new Date().toISOString(),
  ...overrides,
})

export const mockConversationDetail = (
  overrides?: Partial<ConversationDetailResponse>
): ConversationDetailResponse => {
  const id = overrides?.id || 1
  return {
    id,
    title: 'English Conversation Practice',
    type: 'LEARNING',
    summary: 'Discussing daily life and routines',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    isArchived: false,
    messages: [
      mockMessage({
        id: 1,
        conversationId: id,
        content: 'Hello, how are you today?',
        role: 'USER',
      }),
      mockMessage({
        id: 2,
        conversationId: id,
        content: 'I am doing well, thank you for asking! How about you?',
        role: 'ASSISTANT',
      }),
      mockMessage({
        id: 3,
        conversationId: id,
        content: 'I\'m great! What did you do today?',
        role: 'USER',
      }),
      mockMessage({
        id: 4,
        conversationId: id,
        content: 'I went to the market and bought some fresh vegetables.',
        role: 'ASSISTANT',
      }),
    ],
    ...overrides,
  }
}

// ============================================
// HINTS FIXTURES
// ============================================

export const mockHintResponse = (
  wordId: number = 1,
  overrides?: Partial<HintResponse>
): HintResponse => ({
  learningProgressId: wordId,
  hint: 'Think of a situation where this word would be used in a sentence.',
  difficulty: 'MEDIUM',
  ...overrides,
})

// ============================================
// BATCH FIXTURES
// ============================================

export const mockBatchExplanations = (count: number = 3): ExplanationResponse[] =>
  Array.from({ length: count }, (_, i) =>
    mockExplanationResponse({
      wordId: i + 1,
      word: `word${i + 1}`,
    })
  )

export const mockBatchGrammarChecks = (count: number = 3): GrammarCheckResponse[] =>
  Array.from({ length: count }, (_, i) =>
    mockGrammarCheckResponse(`Sample text ${i + 1}`)
  )

export const mockBatchConversations = (count: number = 3): ConversationResponse[] =>
  Array.from({ length: count }, (_, i) =>
    mockConversation({
      id: i + 1,
      title: `Conversation ${i + 1}`,
    })
  )

export const mockBatchHints = (count: number = 3): HintResponse[] =>
  Array.from({ length: count }, (_, i) =>
    mockHintResponse(i + 1)
  )

// ============================================
// PERSONALIZATION FIXTURES
// ============================================

export const mockUserProfile = (
  overrides?: Partial<UserProfile>
): UserProfile => ({
  userId: 1,
  username: 'learner',
  email: 'learner@example.com',
  nativeLanguage: 'Vietnamese',
  targetLanguage: 'English',
  proficiencyLevel: 'B1',
  learningStyle: 'visual',
  dailyGoal: 30,
  joinedAt: '2024-01-15T10:00:00Z',
  lastActive: new Date().toISOString(),
  totalLearningMinutes: 1250,
  currentStreak: 15,
  longestStreak: 45,
  ...overrides,
})

export const mockLearningPlan = (
  overrides?: Partial<LearningPlan>
): LearningPlan => ({
  planId: 1,
  userId: 1,
  startDate: '2024-02-01',
  endDate: '2024-02-28',
  totalDays: 28,
  completedDays: 12,
  weeklyHours: 7,
  focusAreas: ['Business English', 'Speaking', 'Vocabulary'],
  items: [
    {
      day: 1,
      date: '2024-02-01',
      topic: 'Business Greetings',
      estimatedMinutes: 30,
      priority: 'high',
      completed: true,
      activities: [
        {
          id: 1,
          type: 'vocabulary',
          title: 'Business Vocabulary',
          description: 'Learn common business terms',
          estimatedMinutes: 15,
          completed: true,
        },
      ],
    },
  ],
  overallProgress: 42,
  ...overrides,
})

export const mockProgressAnalysis = (
  overrides?: Partial<ProgressAnalysis>
): ProgressAnalysis => ({
  userId: 1,
  analysisDate: new Date().toISOString(),
  overallProgress: 65,
  weeklyProgress: [
    {
      week: 1,
      startDate: '2024-02-01',
      endDate: '2024-02-07',
      minutesSpent: 180,
      sessionsCompleted: 6,
      wordsLearned: 45,
      accuracy: 85,
      averageScore: 88,
    },
  ],
  metrics: [
    {
      name: 'Vocabulary Mastery',
      value: 450,
      target: 500,
      progress: 90,
      trend: 'up',
    },
    {
      name: 'Grammar Accuracy',
      value: 82,
      target: 90,
      progress: 91,
      trend: 'up',
    },
  ],
  estimatedNextLevelDate: '2024-03-15',
  estimatedCompletionDate: '2024-06-30',
  ...overrides,
})

export const mockRecommendationsResponse = (
  overrides?: Partial<RecommendationsResponse>
): RecommendationsResponse => ({
  userId: 1,
  generatedAt: new Date().toISOString(),
  recommendations: [
    {
      id: 1,
      type: 'vocabulary',
      title: 'Business Email Writing',
      description: 'Learn vocabulary for professional emails',
      reason: 'Based on your weak areas in professional communication',
      difficulty: 'B2',
      priority: 'high',
      content: {
        words: ['escalate', 'stakeholder', 'deadline'],
      },
      estimatedMinutes: 20,
    },
  ],
  nextReviewDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
  ...overrides,
})

export const mockWeaknessStrengthAnalysis = (
  overrides?: Partial<WeaknessStrengthAnalysis>
): WeaknessStrengthAnalysis => ({
  userId: 1,
  analysisDate: new Date().toISOString(),
  overallProficiency: 75,
  strengths: [
    {
      skillName: 'Reading Comprehension',
      proficiency: 88,
      level: 'advanced',
      accuracy: 92,
      averageResponseTime: 2500,
      attemptCount: 45,
      successCount: 41,
      lastPracticed: new Date().toISOString(),
      recommendedAction: 'Continue with advanced reading materials',
    },
  ],
  weaknesses: [
    {
      skillName: 'Speaking Fluency',
      proficiency: 62,
      level: 'intermediate',
      accuracy: 75,
      averageResponseTime: 4200,
      attemptCount: 30,
      successCount: 22,
      lastPracticed: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
      recommendedAction: 'Practice more speaking exercises',
    },
  ],
  topicBreakdown: [
    {
      topic: 'Business English',
      proficiency: 72,
      trend: 'improving',
    },
  ],
  recommendations: ['Increase speaking practice', 'Review phrasal verbs'],
  ...overrides,
})

export const mockAdaptiveDifficulty = (
  overrides?: Partial<AdaptiveDifficulty>
): AdaptiveDifficulty => ({
  userId: 1,
  currentLevel: 'B1',
  nextLevel: 'B2',
  progressToNextLevel: 65,
  adjustmentRecommended: false,
  availableLevels: [
    {
      level: 'B1',
      name: 'Intermediate',
      description: 'For learners at intermediate level',
      recommendedMinutes: 30,
      contentCount: 120,
      difficulty: 50,
    },
    {
      level: 'B2',
      name: 'Upper Intermediate',
      description: 'For learners at upper intermediate level',
      recommendedMinutes: 40,
      contentCount: 150,
      difficulty: 60,
    },
  ],
  performanceHistory: [
    {
      date: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
      level: 'B1',
      accuracy: 82,
      averageScore: 85,
    },
  ],
  ...overrides,
})

export const mockDailyPractice = (
  overrides?: Partial<DailyPractice>
): DailyPractice => ({
  date: new Date().toISOString().split('T')[0],
  totalEstimatedMinutes: 30,
  completedMinutes: 18,
  items: [
    {
      id: 1,
      type: 'vocabulary',
      title: 'Daily Vocabulary',
      description: 'Learn 5 new words',
      estimatedMinutes: 10,
      difficulty: 'B1',
      content: 'Word 1, Word 2, Word 3, Word 4, Word 5',
      completed: true,
      score: 95,
    },
    {
      id: 2,
      type: 'speaking',
      title: 'Speaking Practice',
      description: 'Record your voice',
      estimatedMinutes: 15,
      difficulty: 'B1',
      content: 'Describe your day',
      completed: false,
    },
  ],
  streakDays: 15,
  motivation: 'Great job! Keep up your streak!',
  nextChallenge: 'Complete 5 consecutive days without missing a session',
  ...overrides,
})

// ============================================
// SPEAKING FIXTURES
// ============================================

export const mockSpeakingSession = (
  overrides?: Partial<SpeakingSession>
): SpeakingSession => ({
  id: 1,
  userId: 1,
  title: 'Restaurant Conversation',
  description: 'Order food at a restaurant',
  topic: 'Restaurant ordering',
  difficulty: 'B1',
  totalQuestions: 5,
  currentQuestion: 2,
  duration: 300,
  createdAt: new Date().toISOString(),
  status: 'active',
  ...overrides,
})

export const mockSpeakingSessionList = (
  overrides?: Partial<SpeakingSessionList>
): SpeakingSessionList => ({
  sessions: [
    mockSpeakingSession({ id: 1 }),
    mockSpeakingSession({ id: 2, status: 'completed', score: 87 }),
  ],
  total: 2,
  page: 1,
  pageSize: 10,
  ...overrides,
})

export const mockSpeakingAnswer = (
  overrides?: Partial<SpeakingAnswer>
): SpeakingAnswer => ({
  id: 1,
  sessionId: 1,
  questionId: 1,
  audioUrl: 'blob:http://localhost:3000/audio-123',
  text: 'I would like to order the grilled salmon please',
  duration: 5,
  submittedAt: new Date().toISOString(),
  ...overrides,
})

export const mockSpeakingFeedback = (
  overrides?: Partial<SpeakingFeedback>
): SpeakingFeedback => ({
  id: 1,
  answerId: 1,
  score: 85,
  accuracy: 88,
  fluency: 82,
  explanation: 'Good pronunciation and grammar. Work on speaking more naturally.',
  corrections: [
    {
      type: 'pronunciation',
      original: 'grilled',
      suggestion: 'pronounced as "grild"',
      explanation: 'The letter "d" at the end should not be pronounced separately',
    },
  ],
  strengths: ['Clear articulation', 'Correct grammar'],
  areasToImprove: ['Intonation', 'Speaking pace'],
  ...overrides,
})
