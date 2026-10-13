/**
 * SpeakingRecorder Component
 * UI cho recording tiếng nói
 * 
 * Features:
 * - Record/pause/stop controls
 * - Duration display
 * - Error display
 * - Audio visualization (optional)
 * - Disabled states
 */

import { useCallback, useEffect, useState } from "react";
import useSpeakingRecorder from "@/hooks/useSpeakingRecorder";
import { RECORDING_CONSTRAINTS } from "@/constants/speaking.constants";
import { MicIcon, PauseIcon, PlayIcon, StopIcon, AlertCircleIcon } from "lucide-react";

interface SpeakingRecorderProps {
  onRecordingComplete?: (blob: Blob, duration: number) => void;
  onError?: (error: string) => void;
  disabled?: boolean;
  maxDuration?: number;
}

export function SpeakingRecorder({
  onRecordingComplete,
  onError,
  disabled = false,
  maxDuration = RECORDING_CONSTRAINTS.MAX_DURATION_SECONDS,
}: SpeakingRecorderProps) {
  const recorder = useSpeakingRecorder();
  const [isProcessing, setIsProcessing] = useState(false);

  // Handle error state
  useEffect(() => {
    if (recorder.error && onError) {
      onError(recorder.error);
    }
  }, [recorder.error, onError]);

  const handleStartRecording = useCallback(async () => {
    await recorder.startRecording();
  }, [recorder]);

  const handleStopRecording = useCallback(async () => {
    setIsProcessing(true);
    const blob = await recorder.stopRecording();
    setIsProcessing(false);

    if (blob && onRecordingComplete) {
      onRecordingComplete(blob, recorder.duration);
    }
  }, [recorder, onRecordingComplete]);

  const handlePauseRecording = useCallback(() => {
    recorder.pauseRecording();
  }, [recorder]);

  const handleResumeRecording = useCallback(() => {
    recorder.resumeRecording();
  }, [recorder]);

  const handleCancelRecording = useCallback(() => {
    recorder.cancelRecording();
  }, [recorder]);

  const formatDuration = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  const isNearMaxDuration = recorder.duration >= maxDuration - 10;
  const isAtMaxDuration = recorder.duration >= maxDuration;

  return (
    <div className="w-full space-y-4">
      {/* Status */}
      <div className="text-center space-y-2">
        {recorder.isRecording ? (
          <>
            <div className="flex items-center justify-center gap-2">
              <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse" />
              <p className="text-sm font-medium text-gray-700">
                {recorder.isPaused ? "Tạm dừng" : "Đang ghi âm"}
              </p>
            </div>
            <p className={`text-3xl font-mono font-bold ${
              isNearMaxDuration ? "text-red-600" : "text-blue-600"
            }`}>
              {formatDuration(recorder.duration)}
            </p>
            {isNearMaxDuration && (
              <p className="text-xs text-red-600">
                ⚠️ Gần đạt tối đa {formatDuration(maxDuration)}
              </p>
            )}
          </>
        ) : recorder.audioBlob ? (
          <>
            <p className="text-sm font-medium text-green-600">✓ Đã ghi âm</p>
            <p className="text-lg font-mono font-bold text-gray-700">
              {formatDuration(recorder.duration)}
            </p>
          </>
        ) : (
          <>
            <p className="text-sm text-gray-600">Nhấn nút bên dưới để bắt đầu ghi âm</p>
            <p className="text-xs text-gray-500">
              {RECORDING_CONSTRAINTS.MIN_DURATION_SECONDS}-{maxDuration} giây
            </p>
          </>
        )}
      </div>

      {/* Error Display */}
      {recorder.error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-3 flex gap-3">
          <AlertCircleIcon className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-sm font-medium text-red-900">{recorder.error}</p>
          </div>
        </div>
      )}

      {/* Controls */}
      <div className="flex gap-2 justify-center flex-wrap">
        {!recorder.isRecording ? (
          // Start button
          <button
            onClick={handleStartRecording}
            disabled={disabled || isProcessing || !!recorder.error}
            className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
          >
            <MicIcon className="w-5 h-5" />
            Bắt đầu ghi âm
          </button>
        ) : (
          <>
            {/* Pause/Resume button */}
            {!recorder.isPaused ? (
              <button
                onClick={handlePauseRecording}
                disabled={isAtMaxDuration}
                className="flex items-center gap-2 px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 disabled:bg-gray-300 transition"
              >
                <PauseIcon className="w-5 h-5" />
                Tạm dừng
              </button>
            ) : (
              <button
                onClick={handleResumeRecording}
                disabled={isAtMaxDuration}
                className="flex items-center gap-2 px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 disabled:bg-gray-300 transition"
              >
                <PlayIcon className="w-5 h-5" />
                Tiếp tục
              </button>
            )}

            {/* Stop button */}
            <button
              onClick={handleStopRecording}
              disabled={isProcessing}
              className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
            >
              <StopIcon className="w-5 h-5" />
              {isProcessing ? "Đang xử lý..." : "Dừng & Gửi"}
            </button>

            {/* Cancel button */}
            <button
              onClick={handleCancelRecording}
              disabled={isProcessing}
              className="flex items-center gap-2 px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 disabled:bg-gray-300 transition"
            >
              Hủy
            </button>
          </>
        )}
      </div>

      {/* Duration Info */}
      {recorder.audioBlob && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-center">
          <p className="text-sm text-blue-900">
            Bài ghi âm: <span className="font-semibold">{formatDuration(recorder.duration)}</span>
            {" "}
            ({Math.round(recorder.audioBlob.size / 1024)}KB)
          </p>
        </div>
      )}

      {/* Constraints Info */}
      <div className="text-xs text-gray-500 text-center space-y-1">
        <p>Định dạng: WAV, MP3, WebM, OGG, FLAC</p>
        <p>Tối đa: 50MB</p>
        <p>Độ dài: {RECORDING_CONSTRAINTS.MIN_DURATION_SECONDS}-{maxDuration} giây</p>
      </div>
    </div>
  );
}

export default SpeakingRecorder;
