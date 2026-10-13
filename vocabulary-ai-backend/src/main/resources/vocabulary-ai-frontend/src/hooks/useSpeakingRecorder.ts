/**
 * useSpeakingRecorder Hook
 * MediaRecorder wrapper để ghi âm tiếng nói
 * 
 * Features:
 * - Start/pause/stop recording
 * - Duration tracking
 * - Audio blob handling
 * - Error handling
 * - Constraints validation
 */

import { useState, useRef, useCallback, useEffect } from "react";
import { RECORDING_CONSTRAINTS, AUDIO_CONSTRAINTS } from "@/constants/speaking.constants";

export interface UseRecorderState {
  isRecording: boolean;
  isPaused: boolean;
  duration: number;
  error?: string;
  audioBlob?: Blob;
}

export interface UseRecorderActions {
  startRecording: () => Promise<void>;
  pauseRecording: () => void;
  resumeRecording: () => void;
  stopRecording: () => Promise<Blob | null>;
  cancelRecording: () => void;
  resetRecorder: () => void;
}

export function useSpeakingRecorder(): UseRecorderState & UseRecorderActions {
  const [state, setState] = useState<UseRecorderState>({
    isRecording: false,
    isPaused: false,
    duration: 0,
  });

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const durationIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const streamRef = useRef<MediaStream | null>(null);

  // Cleanup interval
  useEffect(() => {
    return () => {
      if (durationIntervalRef.current) {
        clearInterval(durationIntervalRef.current);
      }
    };
  }, []);

  const startRecording = useCallback(async () => {
    try {
      setState((prev) => ({ ...prev, error: undefined }));

      // Request microphone permission
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          sampleRate: RECORDING_CONSTRAINTS.SAMPLE_RATE,
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
      });

      streamRef.current = stream;
      audioChunksRef.current = [];

      // Create MediaRecorder
      const mediaRecorder = new MediaRecorder(stream, {
        mimeType: "audio/webm;codecs=opus",
      });

      mediaRecorder.ondataavailable = (event) => {
        audioChunksRef.current.push(event.data);
      };

      mediaRecorder.onerror = (event) => {
        setState((prev) => ({
          ...prev,
          error: `Recording error: ${event.error}`,
          isRecording: false,
        }));
      };

      mediaRecorderRef.current = mediaRecorder;
      mediaRecorder.start();

      // Start duration tracking
      let startTime = Date.now();
      durationIntervalRef.current = setInterval(() => {
        const elapsed = Math.floor((Date.now() - startTime) / 1000);
        setState((prev) => ({ ...prev, duration: elapsed }));

        // Auto-stop if exceeded max duration
        if (elapsed >= RECORDING_CONSTRAINTS.MAX_DURATION_SECONDS) {
          stopRecording();
        }
      }, 100);

      setState((prev) => ({
        ...prev,
        isRecording: true,
        isPaused: false,
        duration: 0,
      }));
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Failed to start recording";
      
      // Handle specific errors
      let userMessage = errorMessage;
      if (errorMessage.includes("NotAllowedError")) {
        userMessage = "Bạn đã từ chối quyền truy cập microphone";
      } else if (errorMessage.includes("NotFoundError")) {
        userMessage = "Thiết bị ghi âm không tìm thấy";
      } else if (errorMessage.includes("NotReadableError")) {
        userMessage = "Thiết bị ghi âm đang bị sử dụng bởi ứng dụng khác";
      }

      setState((prev) => ({
        ...prev,
        isRecording: false,
        error: userMessage,
      }));
    }
  }, []);

  const pauseRecording = useCallback(() => {
    if (mediaRecorderRef.current && state.isRecording && !state.isPaused) {
      mediaRecorderRef.current.pause();
      if (durationIntervalRef.current) {
        clearInterval(durationIntervalRef.current);
      }
      setState((prev) => ({ ...prev, isPaused: true }));
    }
  }, [state.isRecording, state.isPaused]);

  const resumeRecording = useCallback(() => {
    if (mediaRecorderRef.current && state.isRecording && state.isPaused) {
      mediaRecorderRef.current.resume();

      // Resume duration tracking
      let startTime = Date.now();
      let offset = state.duration;
      durationIntervalRef.current = setInterval(() => {
        const elapsed = offset + Math.floor((Date.now() - startTime) / 1000);
        setState((prev) => ({ ...prev, duration: elapsed }));

        if (elapsed >= RECORDING_CONSTRAINTS.MAX_DURATION_SECONDS) {
          stopRecording();
        }
      }, 100);

      setState((prev) => ({ ...prev, isPaused: false }));
    }
  }, [state.isRecording, state.isPaused, state.duration]);

  const stopRecording = useCallback(
    async (): Promise<Blob | null> => {
      return new Promise((resolve) => {
        if (!mediaRecorderRef.current) {
          resolve(null);
          return;
        }

        if (durationIntervalRef.current) {
          clearInterval(durationIntervalRef.current);
        }

        const recorder = mediaRecorderRef.current;

        recorder.onstop = () => {
          // Validate duration
          if (state.duration < RECORDING_CONSTRAINTS.MIN_DURATION_SECONDS) {
            setState((prev) => ({
              ...prev,
              isRecording: false,
              error: `Bài ghi âm phải dài ít nhất ${RECORDING_CONSTRAINTS.MIN_DURATION_SECONDS} giây`,
              audioBlob: undefined,
            }));
            // Stop all tracks
            streamRef.current?.getTracks().forEach((track) => track.stop());
            resolve(null);
            return;
          }

          // Create blob
          const audioBlob = new Blob(audioChunksRef.current, { type: "audio/webm" });

          // Validate blob size
          if (audioBlob.size > AUDIO_CONSTRAINTS.MAX_SIZE_BYTES) {
            setState((prev) => ({
              ...prev,
              isRecording: false,
              error: `File audio quá lớn (${Math.round(audioBlob.size / 1024 / 1024)}MB). Tối đa ${AUDIO_CONSTRAINTS.MAX_SIZE_MB}MB`,
              audioBlob: undefined,
            }));
            resolve(null);
          } else {
            setState((prev) => ({
              ...prev,
              isRecording: false,
              isPaused: false,
              audioBlob,
            }));
            resolve(audioBlob);
          }

          // Stop all tracks
          streamRef.current?.getTracks().forEach((track) => track.stop());
        };

        recorder.stop();
      });
    },
    [state.duration]
  );

  const cancelRecording = useCallback(() => {
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
    }
    if (durationIntervalRef.current) {
      clearInterval(durationIntervalRef.current);
    }
    streamRef.current?.getTracks().forEach((track) => track.stop());

    setState({
      isRecording: false,
      isPaused: false,
      duration: 0,
      audioBlob: undefined,
    });
    audioChunksRef.current = [];
  }, []);

  const resetRecorder = useCallback(() => {
    cancelRecording();
    setState({
      isRecording: false,
      isPaused: false,
      duration: 0,
    });
  }, [cancelRecording]);

  return {
    ...state,
    startRecording,
    pauseRecording,
    resumeRecording,
    stopRecording,
    cancelRecording,
    resetRecorder,
  };
}

export default useSpeakingRecorder;
