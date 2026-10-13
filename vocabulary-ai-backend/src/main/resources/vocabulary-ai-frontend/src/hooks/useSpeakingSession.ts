/**
 * useSpeakingSession Hook
 * Quản lý Speaking Session state và operations
 * 
 * Features:
 * - Create session
 * - Load session detail
 * - Complete/cancel session
 * - Error handling
 * - Loading states
 */

import { useState, useCallback } from "react";
import SpeakingService from "@/services/api/speakingService";
import {
  SpeakingSession,
  SpeakingSessionSummary,
  CreateSpeakingSessionRequest,
  SpeakingLevel,
} from "@/services/api/speaking.types";

export interface UseSpeakingSessionState {
  session: SpeakingSessionSummary | null;
  isLoading: boolean;
  error?: string;
}

export interface UseSpeakingSessionActions {
  createSession: (request: CreateSpeakingSessionRequest) => Promise<SpeakingSession | null>;
  loadSession: (sessionId: number) => Promise<void>;
  completeSession: (sessionId: number) => Promise<void>;
  cancelSession: (sessionId: number) => Promise<void>;
  deleteSession: (sessionId: number) => Promise<void>;
  clearError: () => void;
}

export function useSpeakingSession(): UseSpeakingSessionState & UseSpeakingSessionActions {
  const [state, setState] = useState<UseSpeakingSessionState>({
    session: null,
    isLoading: false,
  });

  const createSession = useCallback(
    async (request: CreateSpeakingSessionRequest): Promise<SpeakingSession | null> => {
      try {
        setState((prev) => ({ ...prev, isLoading: true, error: undefined }));

        // Validate request
        if (!request.topic || request.topic.length < 3) {
          throw new Error("Chủ đề phải có ít nhất 3 ký tự");
        }
        if (!request.level) {
          throw new Error("Vui lòng chọn mức độ");
        }

        const newSession = await SpeakingService.createSession(request);
        setState((prev) => ({ ...prev, isLoading: false }));
        return newSession;
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Lỗi khi tạo phiên";
        setState((prev) => ({ ...prev, isLoading: false, error: errorMessage }));
        return null;
      }
    },
    []
  );

  const loadSession = useCallback(async (sessionId: number) => {
    try {
      setState((prev) => ({ ...prev, isLoading: true, error: undefined }));
      const sessionData = await SpeakingService.getSessionDetail(sessionId);
      setState((prev) => ({ ...prev, session: sessionData, isLoading: false }));
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Lỗi khi tải phiên";
      setState((prev) => ({
        ...prev,
        isLoading: false,
        error: errorMessage,
        session: null,
      }));
    }
  }, []);

  const completeSession = useCallback(async (sessionId: number) => {
    try {
      setState((prev) => ({ ...prev, isLoading: true, error: undefined }));
      await SpeakingService.completeSession(sessionId);

      // Reload session detail
      if (state.session?.id === sessionId) {
        await loadSession(sessionId);
      }

      setState((prev) => ({ ...prev, isLoading: false }));
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Lỗi khi hoàn thành phiên";
      setState((prev) => ({ ...prev, isLoading: false, error: errorMessage }));
    }
  }, [state.session?.id, loadSession]);

  const cancelSession = useCallback(async (sessionId: number) => {
    try {
      setState((prev) => ({ ...prev, isLoading: true, error: undefined }));
      await SpeakingService.cancelSession(sessionId);

      // Reload session detail
      if (state.session?.id === sessionId) {
        await loadSession(sessionId);
      }

      setState((prev) => ({ ...prev, isLoading: false }));
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Lỗi khi hủy phiên";
      setState((prev) => ({ ...prev, isLoading: false, error: errorMessage }));
    }
  }, [state.session?.id, loadSession]);

  const deleteSession = useCallback(async (sessionId: number) => {
    try {
      setState((prev) => ({ ...prev, isLoading: true, error: undefined }));
      await SpeakingService.deleteSession(sessionId);

      // Clear session if it's the current one
      if (state.session?.id === sessionId) {
        setState((prev) => ({ ...prev, session: null }));
      }

      setState((prev) => ({ ...prev, isLoading: false }));
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Lỗi khi xóa phiên";
      setState((prev) => ({ ...prev, isLoading: false, error: errorMessage }));
    }
  }, [state.session?.id]);

  const clearError = useCallback(() => {
    setState((prev) => ({ ...prev, error: undefined }));
  }, []);

  return {
    ...state,
    createSession,
    loadSession,
    completeSession,
    cancelSession,
    deleteSession,
    clearError,
  };
}

export default useSpeakingSession;
