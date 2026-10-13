/**
 * AI Service
 * Unified service for all AI/Learning Tools APIs
 * Handles: Vocabulary Explanation, Grammar Check, Chat, Hints
 */

import { apiClient } from "./client";
import { AI_ENDPOINTS } from "./endpoints";
import {
  ExplainWordRequest,
  ExplanationResponse,
  GrammarCheckRequest,
  GrammarCheckResponse,
  CreateConversationRequest,
  SendMessageRequest,
  MessageResponse,
  ConversationResponse,
  ConversationDetailResponse,
  HintRequest,
  HintResponse,
} from "./ai.types";

export class AIService {
  // ============================================
  // VOCABULARY - Explain Word
  // ============================================

  /**
   * Get AI explanation for a single word
   */
  static async explainWord(request: ExplainWordRequest): Promise<ExplanationResponse> {
    try {
      console.debug('📤 [AI] Explaining word:', { endpoint: AI_ENDPOINTS.VOCABULARY_EXPLAIN, request });
      const response = await apiClient.post<ExplanationResponse>(
        AI_ENDPOINTS.VOCABULARY_EXPLAIN,
        request
      );
      console.debug('✅ [AI] Word explanation received:', response.data.data);
      return response.data.data!;
    } catch (error) {
      console.error('❌ [AI] Failed to explain word:', { error, request, endpoint: AI_ENDPOINTS.VOCABULARY_EXPLAIN });
      throw error;
    }
  }

  /**
   * Get AI explanations for multiple words (batch)
   */
  static async explainWords(requests: ExplainWordRequest[]): Promise<ExplanationResponse[]> {
    const response = await apiClient.post<ExplanationResponse[]>(
      AI_ENDPOINTS.VOCABULARY_EXPLAIN_BATCH,
      { words: requests }
    );
    return response.data.data || [];
  }

  // ============================================
  // GRAMMAR - Check
  // ============================================

  /**
   * Check grammar for a single text
   */
  static async checkGrammar(request: GrammarCheckRequest): Promise<GrammarCheckResponse> {
    const response = await apiClient.post<GrammarCheckResponse>(
      AI_ENDPOINTS.GRAMMAR_CHECK,
      request
    );
    return response.data.data!;
  }

  /**
   * Check grammar for multiple texts (batch)
   */
  static async checkGrammarBatch(texts: string[]): Promise<GrammarCheckResponse[]> {
    const response = await apiClient.post<GrammarCheckResponse[]>(
      AI_ENDPOINTS.GRAMMAR_CHECK_BATCH,
      { texts }
    );
    return response.data.data || [];
  }

  // ============================================
  // CHAT - Conversation
  // ============================================

  /**
   * Create a new conversation
   */
  static async createConversation(
    request: CreateConversationRequest
  ): Promise<ConversationResponse> {
    try {
      console.debug('📤 [AI] Creating conversation:', { endpoint: AI_ENDPOINTS.CHAT_CREATE_CONVERSATION, request });
      const response = await apiClient.post<ConversationResponse>(
        AI_ENDPOINTS.CHAT_CREATE_CONVERSATION,
        request
      );
      console.debug('✅ [AI] Conversation created:', response.data.data);
      return response.data.data!;
    } catch (error) {
      console.error('❌ [AI] Failed to create conversation:', { error, request, endpoint: AI_ENDPOINTS.CHAT_CREATE_CONVERSATION });
      throw error;
    }
  }

  /**
   * Get all user conversations
   */
  static async getConversations(): Promise<ConversationResponse[]> {
    const response = await apiClient.get<ConversationResponse[]>(
      AI_ENDPOINTS.CHAT_GET_CONVERSATIONS
    );
    return response.data.data || [];
  }

  /**
   * Get conversation detail with full message history
   */
  static async getConversationDetail(conversationId: string): Promise<ConversationDetailResponse> {
    const response = await apiClient.get<ConversationDetailResponse>(
      AI_ENDPOINTS.CHAT_GET_CONVERSATION(conversationId)
    );
    return response.data.data!;
  }

  /**
   * Send a message in a conversation
   */
  static async sendMessage(
    conversationId: string,
    request: SendMessageRequest
  ): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>(
      AI_ENDPOINTS.CHAT_SEND_MESSAGE(conversationId),
      request
    );
    return response.data.data!;
  }

  /**
   * Delete a conversation
   */
  static async deleteConversation(conversationId: string): Promise<void> {
    await apiClient.delete(AI_ENDPOINTS.CHAT_DELETE_CONVERSATION(conversationId));
  }

  // ============================================
  // HINTS - Generate
  // ============================================

  /**
   * Generate a hint for a word
   */
  static async generateHint(request: HintRequest): Promise<HintResponse> {
    const response = await apiClient.post<HintResponse>(
      AI_ENDPOINTS.HINTS_GENERATE,
      request
    );
    return response.data.data!;
  }

  /**
   * Generate hints for multiple words (batch)
   */
  static async generateHintsBatch(requests: HintRequest[]): Promise<HintResponse[]> {
    const response = await apiClient.post<HintResponse[]>(
      AI_ENDPOINTS.HINTS_GENERATE_BATCH,
      { hints: requests }
    );
    return response.data.data || [];
  }
}

export default AIService;
export type { ExplanationResponse, GrammarCheckResponse, ConversationDetailResponse, HintResponse };
