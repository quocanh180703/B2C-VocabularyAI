/**
 * AI Service Types
 * Type definitions for all AI/Learning Tools APIs
 * Mapped from backend DTOs
 */

// ============================================
// VOCABULARY TYPES
// ============================================

export interface ExplainWordRequest {
  wordId: number;  // Changed from: word: string
  context?: string;
  // Note: level is not supported by backend, removed
}

export interface ExplanationResponse {
  wordId: number;
  word: string;
  explanation: string;
  examples: string[];
  etymology: string;
  relatedWords: string[];
  partOfSpeech: string;
}

// ============================================
// GRAMMAR TYPES
// ============================================

export interface GrammarCheckRequest {
  text: string;
}

export interface GrammarError {
  position: number;
  errorType: string;
  original: string;
  correction: string;
  explanation: string;
}

export interface GrammarSuggestion {
  title: string;
  content: string;
  priority: "HIGH" | "MEDIUM" | "LOW";
}

export interface GrammarCheckResponse {
  originalText: string;
  correctedText: string;
  errors: GrammarError[];
  suggestions: GrammarSuggestion[];
}

// ============================================
// CHAT/CONVERSATION TYPES
// ============================================

export interface CreateConversationRequest {
  title?: string;
  type: "LEARNING" | "PRACTICE" | "FEEDBACK";
}

export interface SendMessageRequest {
  content: string;
}

export interface MessageResponse {
  id: number;
  conversationId: number;
  content: string;
  role: "USER" | "ASSISTANT";
  timestamp: string;
}

export interface ConversationResponse {
  id: number;
  title: string;
  type: "LEARNING" | "PRACTICE" | "FEEDBACK";
  summary?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConversationDetailResponse {
  id: number;
  title: string;
  type: "LEARNING" | "PRACTICE" | "FEEDBACK";
  summary?: string;
  createdAt: string;
  updatedAt: string;
  isArchived: boolean;
  messages: MessageResponse[];
}

// ============================================
// HINTS TYPES
// ============================================

export interface HintRequest {
  wordId: number;
  level?: "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
}

export interface HintResponse {
  learningProgressId: number;
  hint: string;
  difficulty: string;
}
