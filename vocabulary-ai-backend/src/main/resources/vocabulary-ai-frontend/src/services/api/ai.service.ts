/**
 * DEPRECATED: Old AI Service
 * 
 * This file is deprecated and should not be used.
 * Use the new aiService.ts instead, which has:
 * - Correct endpoints (/api/v1/ai/* instead of /ai/*)
 * - Complete coverage of all 6 AI modules
 * - Proper TypeScript types with ai.types.ts
 * - React Query integration via useAI.ts hooks
 * 
 * Migration guide:
 * Old: import AIService from "@/services/api/ai.service.ts"
 * New: import AIService from "@/services/api/aiService.ts"
 *      import { useExplainWord, useCheckGrammar } from "@/hooks/useAI"
 * 
 * Files to delete:
 * - src/services/api/ai.service.ts (this file)
 * 
 * Files created:
 * - src/services/api/aiService.ts (new - correct endpoints)
 * - src/services/api/ai.types.ts (new - proper types)
 * - src/hooks/useAI.ts (new - React Query hooks)
 */

import { apiClient } from "./client";
import { AI_ENDPOINTS } from "./endpoints";
import {
  ExplainWordRequest,
  WordExplanation,
  GrammarCheckRequest,
  GrammarCheckResponse,
  ChatRequest,
  ChatResponse,
} from "./types";

/**
 * @deprecated Use aiService.ts instead with proper endpoints
 */
export class AIService {
  /**
   * Giải thích từ vựng
   * @deprecated Use AIService.explainWord from aiService.ts instead
   */
  static async explainWord(request: ExplainWordRequest): Promise<WordExplanation> {
    const response = await apiClient.post<WordExplanation>(
      AI_ENDPOINTS.EXPLAIN_WORD,
      request
    );
    return response.data.data!;
  }

  /**
   * Kiểm tra ngữ pháp
   * @deprecated Use AIService.checkGrammar from aiService.ts instead
   */
  static async checkGrammar(request: GrammarCheckRequest): Promise<GrammarCheckResponse> {
    const response = await apiClient.post<GrammarCheckResponse>(
      AI_ENDPOINTS.GRAMMAR_CHECK,
      request
    );
    return response.data.data!;
  }

  /**
   * Chat với AI tutor
   * @deprecated Use AIService.createConversation / sendMessage from aiService.ts instead
   */
  static async chat(request: ChatRequest): Promise<ChatResponse> {
    const response = await apiClient.post<ChatResponse>(
      AI_ENDPOINTS.CHAT,
      request
    );
    return response.data.data!;
  }

  /**
   * Lấy lịch sử conversation
   * @deprecated Use AIService.getConversationDetail from aiService.ts instead
   */
  static async getConversation(conversationId: string): Promise<any> {
    const response = await apiClient.get(
      AI_ENDPOINTS.GET_CONVERSATION(conversationId)
    );
    return response.data.data;
  }

  /**
   * Lấy danh sách conversations
   * @deprecated Use AIService.getConversations from aiService.ts instead
   */
  static async getConversations(params: any = {}): Promise<any[]> {
    const response = await apiClient.get(
      AI_ENDPOINTS.GET_CONVERSATIONS,
      { params }
    );
    return response.data.data || [];
  }

  /**
   * Xóa conversation
   * @deprecated Use AIService.deleteConversation from aiService.ts instead
   */
  static async clearConversation(conversationId: string): Promise<void> {
    await apiClient.delete(AI_ENDPOINTS.CLEAR_CONVERSATION(conversationId));
  }
}

export default AIService;
