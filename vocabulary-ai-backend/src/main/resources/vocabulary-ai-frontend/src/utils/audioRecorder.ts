/**
 * Audio Recorder Utility
 * Handles Web Audio API recording with proper error handling and state management
 */

export interface AudioRecorderOptions {
  mimeType?: string;
  audioBitsPerSecond?: number;
}

export interface RecordingState {
  isRecording: boolean;
  isPaused: boolean;
  duration: number;
  error?: string;
}

export class AudioRecorder {
  private mediaRecorder: MediaRecorder | null = null;
  private audioStream: MediaStream | null = null;
  private recordedChunks: Blob[] = [];
  private startTime: number = 0;
  private pauseTime: number = 0;
  private pausedDuration: number = 0;
  private durationInterval: number | null = null;
  private state: RecordingState = {
    isRecording: false,
    isPaused: false,
    duration: 0,
  };
  private listeners: Map<string, Set<(state: RecordingState) => void>> = new Map();

  /**
   * Initialize audio recording
   */
  async start(options?: AudioRecorderOptions): Promise<void> {
    try {
      // Check browser support
      if (!navigator.mediaDevices?.getUserMedia) {
        throw new Error("Audio recording not supported in this browser");
      }

      // Stop any existing recording
      if (this.mediaRecorder?.state === "recording") {
        this.stop();
      }

      // Request microphone access
      this.audioStream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
      });

      // Create media recorder
      const mimeType = options?.mimeType || this.getSupportedMimeType();
      this.mediaRecorder = new MediaRecorder(this.audioStream, {
        mimeType,
        audioBitsPerSecond: options?.audioBitsPerSecond || 128000,
      });

      this.recordedChunks = [];

      // Handle data available
      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.recordedChunks.push(event.data);
        }
      };

      // Handle stop
      this.mediaRecorder.onstop = () => {
        this.cleanup();
      };

      // Handle error
      this.mediaRecorder.onerror = (event) => {
        this.setState({
          error: `Recording error: ${event.error}`,
        });
      };

      // Start recording
      this.mediaRecorder.start();

      // Start duration timer
      this.startTime = Date.now();
      this.pausedDuration = 0;
      this.durationInterval = window.setInterval(() => {
        const elapsed = Date.now() - this.startTime - this.pausedDuration;
        this.setState({
          duration: Math.floor(elapsed / 1000),
        });
      }, 100);

      this.setState({
        isRecording: true,
        isPaused: false,
        error: undefined,
      });
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Unknown error occurred";
      this.setState({
        error: errorMessage,
        isRecording: false,
      });
      throw error;
    }
  }

  /**
   * Pause recording
   */
  pause(): void {
    if (!this.mediaRecorder || this.mediaRecorder.state !== "recording") {
      return;
    }

    this.mediaRecorder.pause();
    this.pauseTime = Date.now();

    this.setState({
      isPaused: true,
    });
  }

  /**
   * Resume recording
   */
  resume(): void {
    if (!this.mediaRecorder || this.mediaRecorder.state !== "paused") {
      return;
    }

    this.mediaRecorder.resume();
    this.pausedDuration += Date.now() - this.pauseTime;

    this.setState({
      isPaused: false,
    });
  }

  /**
   * Stop recording and get blob
   */
  stop(): Blob {
    if (this.mediaRecorder && this.mediaRecorder.state !== "inactive") {
      this.mediaRecorder.stop();
    }

    if (this.durationInterval !== null) {
      clearInterval(this.durationInterval);
      this.durationInterval = null;
    }

    const audioBlob = new Blob(this.recordedChunks, { type: "audio/webm;codecs=opus" });

    this.setState({
      isRecording: false,
      isPaused: false,
    });

    return audioBlob;
  }

  /**
   * Cancel recording
   */
  cancel(): void {
    this.stop();
    this.recordedChunks = [];
    this.setState({
      duration: 0,
    });
  }

  /**
   * Get current state
   */
  getState(): RecordingState {
    return { ...this.state };
  }

  /**
   * Subscribe to state changes
   */
  subscribe(callback: (state: RecordingState) => void): () => void {
    if (!this.listeners.has("state")) {
      this.listeners.set("state", new Set());
    }

    this.listeners.get("state")!.add(callback);

    return () => {
      this.listeners.get("state")?.delete(callback);
    };
  }

  /**
   * Clean up resources
   */
  private cleanup(): void {
    // Stop all tracks
    if (this.audioStream) {
      this.audioStream.getTracks().forEach((track) => track.stop());
      this.audioStream = null;
    }

    // Clear recorder
    this.mediaRecorder = null;

    // Clear interval
    if (this.durationInterval !== null) {
      clearInterval(this.durationInterval);
      this.durationInterval = null;
    }
  }

  /**
   * Update state and notify listeners
   */
  private setState(newState: Partial<RecordingState>): void {
    this.state = { ...this.state, ...newState };

    const listeners = this.listeners.get("state");
    if (listeners) {
      listeners.forEach((callback) => callback({ ...this.state }));
    }
  }

  /**
   * Get supported MIME type
   */
  private getSupportedMimeType(): string {
    const types = [
      "audio/webm;codecs=opus",
      "audio/webm",
      "audio/mp4",
      "audio/ogg",
    ];

    for (const type of types) {
      if (MediaRecorder.isTypeSupported(type)) {
        return type;
      }
    }

    // Fallback to default
    return "audio/webm";
  }

  /**
   * Check if audio recording is supported
   */
  static isSupported(): boolean {
    return !!(
      navigator.mediaDevices &&
      navigator.mediaDevices.getUserMedia &&
      window.MediaRecorder
    );
  }

  /**
   * Get list of supported MIME types
   */
  static getSupportedMimeTypes(): string[] {
    const types = [
      "audio/webm;codecs=opus",
      "audio/webm",
      "audio/mp4",
      "audio/ogg",
    ];

    return types.filter((type) => MediaRecorder.isTypeSupported(type));
  }
}

export default AudioRecorder;
