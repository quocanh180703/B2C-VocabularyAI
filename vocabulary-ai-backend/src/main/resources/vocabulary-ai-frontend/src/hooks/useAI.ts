/**
 * AI Hooks
 * React Query hooks for all AI/Learning Tools APIs
 * Provides caching, refetching, and mutation management
 */

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import AIService from "@/services/api/aiService";
import type {
  ExplainWordRequest,
  GrammarCheckRequest,
  CreateConversationRequest,
  SendMessageRequest,
  HintRequest,
} from "@/services/api/ai.types";

// ============================================
// QUERY KEYS
// ============================================

export const aiQueryKeys = {
  all: ["ai"] as const,
  
  vocabulary: () => [...aiQueryKeys.all, "vocabulary"] as const,
  explanation: (word: string) => [...aiQueryKeys.vocabulary(), word] as const,
  
  grammar: () => [...aiQueryKeys.all, "grammar"] as const,
  grammarCheck: (text: string) => [...aiQueryKeys.grammar(), text] as const,
  
  chat: () => [...aiQueryKeys.all, "chat"] as const,
  conversations: () => [...aiQueryKeys.chat(), "conversations"] as const,
  conversation: (id: string) => [...aiQueryKeys.conversations(), id] as const,
  
  hints: () => [...aiQueryKeys.all, "hints"] as const,
  hint: (wordId: number) => [...aiQueryKeys.hints(), wordId] as const,
};

// ============================================
// VOCABULARY HOOKS
// ============================================

/**
 * Hook to explain a single word
 */
export function useExplainWord() {
  return useMutation({
    mutationFn: (request: ExplainWordRequest) => AIService.explainWord(request),
  });
}

/**
 * Hook to explain multiple words (batch)
 */
export function useExplainWords() {
  return useMutation({
    mutationFn: (requests: ExplainWordRequest[]) => AIService.explainWords(requests),
  });
}

// ============================================
// GRAMMAR HOOKS
// ============================================

/**
 * Hook to check grammar for a single text
 */
export function useCheckGrammar() {
  return useMutation({
    mutationFn: (request: GrammarCheckRequest) => AIService.checkGrammar(request),
  });
}

/**
 * Hook to check grammar for multiple texts (batch)
 */
export function useCheckGrammarBatch() {
  return useMutation({
    mutationFn: (texts: string[]) => AIService.checkGrammarBatch(texts),
  });
}

// ============================================
// CHAT HOOKS
// ============================================

/**
 * Hook to create a new conversation
 */
export function useCreateConversation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateConversationRequest) =>
      AIService.createConversation(request),
    onSuccess: () => {
      // Invalidate conversations list to refetch
      queryClient.invalidateQueries({
        queryKey: aiQueryKeys.conversations(),
      });
    },
  });
}

/**
 * Hook to get all conversations
 */
export function useConversations() {
  return useQuery({
    queryKey: aiQueryKeys.conversations(),
    queryFn: () => AIService.getConversations(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/**
 * Hook to get a single conversation with messages
 */
export function useConversationDetail(conversationId: string | null) {
  return useQuery({
    queryKey: conversationId ? aiQueryKeys.conversation(conversationId) : ["skip"],
    queryFn: () => (conversationId ? AIService.getConversationDetail(conversationId) : null),
    enabled: !!conversationId,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
}

/**
 * Hook to send a message in a conversation
 */
export function useSendMessage() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      conversationId,
      request,
    }: {
      conversationId: string;
      request: SendMessageRequest;
    }) => AIService.sendMessage(conversationId, request),
    onSuccess: (_, { conversationId }) => {
      // Invalidate conversation detail to refetch with new message
      queryClient.invalidateQueries({
        queryKey: aiQueryKeys.conversation(conversationId),
      });
    },
  });
}

/**
 * Hook to delete a conversation
 */
export function useDeleteConversation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (conversationId: string) => AIService.deleteConversation(conversationId),
    onSuccess: () => {
      // Invalidate conversations list
      queryClient.invalidateQueries({
        queryKey: aiQueryKeys.conversations(),
      });
    },
  });
}

// ============================================
// HINTS HOOKS
// ============================================

/**
 * Hook to generate a hint for a word
 */
export function useGenerateHint() {
  return useMutation({
    mutationFn: (request: HintRequest) => AIService.generateHint(request),
  });
}

/**
 * Hook to generate hints for multiple words (batch)
 */
export function useGenerateHintsBatch() {
  return useMutation({
    mutationFn: (requests: HintRequest[]) => AIService.generateHintsBatch(requests),
  });
}
