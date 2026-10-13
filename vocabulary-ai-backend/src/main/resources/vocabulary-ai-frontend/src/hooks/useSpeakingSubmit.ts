/**
 * useSpeakingSubmit Hook
 * Xử lý submit audio attempt + nhận feedback từ backend
 * 
 * Features:
 * - Submit audio file to backend
 * - Track upload progress
 * - Handle loading & error states
 * - Cache feedback data
 * - Retry logic
 */

import { useState, useCallback, useRef } from "react";
import SpeakingService from "@/services/api/speakingService";
import { SpeakingPracticeResponse } from "@/services/api/speaking.types";
import { AUDIO_CONSTRAINTS } from "@/constants/speaking.constants";

export interface UseSubmitState {
  isSubmitting: boolean;
  uploadProgress: number; // 0-100
  response: SpeakingPracticeResponse | null;
  error?: string;
}

export interface UseSubmitActions {
  submitAttempt: (sessionId: number, audioBlob: Blob) => Promise<SpeakingPracticeResponse | null>;
  retrySubmit: () => Promise<SpeakingPracticeResponse | null>;
  clearResponse: () => void;
  clearError: () => void;
}

export function useSpeakingSubmit(): UseSubmitState & UseSubmitActions {
  const [state, setState] = useState<UseSubmitState>({
    isSubmitting: false,
    uploadProgress: 0,
    response: null,
  });

  // Store last submit params for retry
  const lastParamsRef = useRef<{ sessionId: number; audioBlob: Blob } | null>(null);

  const validateAudioBlob = (blob: Blob): { valid: boolean; error?: string } => {
    // Check size
    if (blob.size > AUDIO_CONSTRAINTS.MAX_SIZE_BYTES) {
      return {
        valid: false,
        error: `File audio quá lớn (${Math.round(blob.size / 1024 / 1024)}MB). Tối đa ${AUDIO_CONSTRAINTS.MAX_SIZE_MB}MB`,
      };
    }

    // Check MIME type
    if (!AUDIO_CONSTRAINTS.ALLOWED_MIME_TYPES.includes(blob.type)) {
      return {
        valid: false,
        error: `Định dạng audio không được hỗ trợ. Vui lòng sử dụng: ${AUDIO_CONSTRAINTS.ALLOWED_EXTENSIONS_DISPLAY}`,
      };
    }

    return { valid: true };
  };

  const submitAttempt = useCallback(
    async (
      sessionId: number,
      audioBlob: Blob
    ): Promise<SpeakingPracticeResponse | null> => {
      try {
        // Validate audio blob
        const validation = validateAudioBlob(audioBlob);
        if (!validation.valid) {
          setState((prev) => ({
            ...prev,
            error: validation.error,
            isSubmitting: false,
          }));
          return null;
        }

        // Store for retry
        lastParamsRef.current = { sessionId, audioBlob };

        setState((prev) => ({
          ...prev,
          isSubmitting: true,
          uploadProgress: 0,
          error: undefined,
        }));

        // Simulate upload progress (backend handles actual processing)
        const progressInterval = setInterval(() => {
          setState((prev) => {
            if (prev.uploadProgress < 90) {
              return { ...prev, uploadProgress: prev.uploadProgress + 10 };
            }
            return prev;
          });
        }, 200);

        try {
          // Submit to backend
          const response = await SpeakingService.submitAttempt(sessionId, audioBlob);

          clearInterval(progressInterval);
          setState((prev) => ({
            ...prev,
            isSubmitting: false,
            uploadProgress: 100,
            response,
            error: undefined,
          }));

          return response;
        } catch (error) {
          clearInterval(progressInterval);
          throw error;
        }
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Lỗi khi gửi bài";

        // Parse backend error messages
        let userMessage = errorMessage;
        if (errorMessage.includes("403") || errorMessage.includes("Forbidden")) {
          userMessage = "Bạn không có quyền truy cập phiên này";
        } else if (errorMessage.includes("404") || errorMessage.includes("Not Found")) {
          userMessage = "Phiên luyện tập không tìm thấy";
        } else if (errorMessage.includes("429")) {
          userMessage = "Quá nhiều yêu cầu. Vui lòng chờ một lúc";
        } else if (errorMessage.includes("500") || errorMessage.includes("Internal Server")) {
          userMessage = "Lỗi máy chủ. Vui lòng thử lại sau";
        }

        setState((prev) => ({
          ...prev,
          isSubmitting: false,
          uploadProgress: 0,
          error: userMessage,
          response: null,
        }));

        return null;
      }
    },
    []
  );

  const retrySubmit = useCallback(async (): Promise<SpeakingPracticeResponse | null> => {
    if (!lastParamsRef.current) {
      setState((prev) => ({
        ...prev,
        error: "Không có dữ liệu để thử lại",
      }));
      return null;
    }

    const { sessionId, audioBlob } = lastParamsRef.current;
    return submitAttempt(sessionId, audioBlob);
  }, [submitAttempt]);

  const clearResponse = useCallback(() => {
    setState((prev) => ({
      ...prev,
      response: null,
      uploadProgress: 0,
    }));
  }, []);

  const clearError = useCallback(() => {
    setState((prev) => ({
      ...prev,
      error: undefined,
    }));
  }, []);

  return {
    ...state,
    submitAttempt,
    retrySubmit,
    clearResponse,
    clearError,
  };
}

export default useSpeakingSubmit;
