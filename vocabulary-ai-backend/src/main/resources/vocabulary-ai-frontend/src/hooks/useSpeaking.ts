/**
 * Speaking Hooks
 * React Query hooks for speaking practice APIs
 * Provides caching, mutations, and session management
 */

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import SpeakingService from "@/services/api/speakingService";
import type {
  CreateSpeakingSessionRequest,
  SubmitSpeakingAnswerRequest,
} from "@/services/api/speaking.types";

// ============================================
// QUERY KEYS
// ============================================

export const speakingQueryKeys = {
  all: ["speaking"] as const,

  sessions: () => [...speakingQueryKeys.all, "sessions"] as const,
  sessionsList: (params?: any) =>
    [...speakingQueryKeys.sessions(), { params }] as const,

  session: (id: string) => [...speakingQueryKeys.sessions(), id] as const,

  feedback: (sessionId: string) =>
    [...speakingQueryKeys.all, "feedback", sessionId] as const,
};

// ============================================
// SESSION HOOKS
// ============================================

/**
 * Hook to create a new speaking session
 */
export function useCreateSpeakingSession() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateSpeakingSessionRequest) =>
      SpeakingService.createSession(request),
    onSuccess: (session) => {
      // Add to sessions cache
      queryClient.setQueryData(
        speakingQueryKeys.session(session.id.toString()),
        session
      );
      // Invalidate sessions list
      queryClient.invalidateQueries({
        queryKey: speakingQueryKeys.sessions(),
      });
    },
  });
}

/**
 * Hook to get all speaking sessions
 */
export function useSpeakingSessions(params?: {
  page?: number;
  pageSize?: number;
  status?: string;
}) {
  return useQuery({
    queryKey: speakingQueryKeys.sessionsList(params),
    queryFn: () => SpeakingService.getSessions(params),
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
}

/**
 * Hook to get a specific speaking session
 */
export function useSpeakingSession(sessionId: string | null) {
  return useQuery({
    queryKey: sessionId ? speakingQueryKeys.session(sessionId) : ["skip"],
    queryFn: () => (sessionId ? SpeakingService.getSession(sessionId) : null),
    enabled: !!sessionId,
    staleTime: 2 * 60 * 1000, // 2 minutes
    gcTime: 5 * 60 * 1000, // 5 minutes
  });
}

// ============================================
// ANSWER SUBMISSION HOOKS
// ============================================

/**
 * Hook to submit a speaking answer
 */
export function useSubmitSpeakingAnswer() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      sessionId,
      request,
    }: {
      sessionId: string;
      request: SubmitSpeakingAnswerRequest;
    }) => SpeakingService.submitAnswer(sessionId, request),
    onSuccess: (_, { sessionId }) => {
      // Invalidate session and feedback
      queryClient.invalidateQueries({
        queryKey: speakingQueryKeys.session(sessionId),
      });
      queryClient.invalidateQueries({
        queryKey: speakingQueryKeys.feedback(sessionId),
      });
    },
  });
}

// ============================================
// FEEDBACK HOOKS
// ============================================

/**
 * Hook to get feedback for a session
 */
export function useSpeakingFeedback(sessionId: string | null) {
  return useQuery({
    queryKey: sessionId ? speakingQueryKeys.feedback(sessionId) : ["skip"],
    queryFn: () => (sessionId ? SpeakingService.getFeedback(sessionId) : null),
    enabled: !!sessionId,
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
}

// ============================================
// SESSION COMPLETION HOOKS
// ============================================

/**
 * Hook to complete a speaking session
 */
export function useCompleteSpeakingSession() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (sessionId: string) => SpeakingService.completeSession(sessionId),
    onSuccess: (session) => {
      // Update session cache
      queryClient.setQueryData(
        speakingQueryKeys.session(session.id.toString()),
        session
      );
      // Invalidate sessions list
      queryClient.invalidateQueries({
        queryKey: speakingQueryKeys.sessions(),
      });
    },
  });
}
