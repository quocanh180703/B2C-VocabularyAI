/**
 * Speaking Tab Component
 * Displays speaking practice features with audio recording
 */

import { useState, useEffect, useRef } from "react";
import {
  useCreateSpeakingSession,
  useSpeakingSessions,
  useSpeakingSession,
  useSubmitSpeakingAnswer,
  useSpeakingFeedback,
  useCompleteSpeakingSession,
} from "@/hooks/useSpeaking";
import { AudioRecorder } from "@/utils/audioRecorder";
import { SkeletonList } from "@/components/common";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";

interface SpeakingTabProps {
  activeSession?: string | null;
}

export default function SpeakingTab({ activeSession }: SpeakingTabProps) {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [formData, setFormData] = useState({
    topic: "",
    difficulty: "B1" as const,
    title: "",
  });

  const [audioState, setAudioState] = useState({
    isRecording: false,
    isPaused: false,
    duration: 0,
    error: undefined as string | undefined,
  });

  const audioRecorderRef = useRef<AudioRecorder | null>(null);

  // API Calls
  const createSessionMutation = useCreateSpeakingSession();
  const sessionsQuery = useSpeakingSessions();
  const sessionQuery = useSpeakingSession(activeSession ?? null);
  const submitAnswerMutation = useSubmitSpeakingAnswer();
  const feedbackQuery = useSpeakingFeedback(activeSession ?? null);
  const completeSessionMutation = useCompleteSpeakingSession();

  // Initialize audio recorder
  useEffect(() => {
    if (!AudioRecorder.isSupported()) {
      setAudioState((prev) => ({
        ...prev,
        error: "Audio recording not supported in this browser",
      }));
    } else {
      audioRecorderRef.current = new AudioRecorder();

      const unsubscribe = audioRecorderRef.current.subscribe((state) => {
        setAudioState((prev) => ({
          ...prev,
          isRecording: state.isRecording,
          isPaused: state.isPaused,
          duration: state.duration,
          error: state.error,
        }));
      });

      return () => {
        unsubscribe();
        audioRecorderRef.current = null;
      };
    }
  }, []);

  const handleCreateSession = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await createSessionMutation.mutateAsync({
        topic: formData.topic,
        difficulty: formData.difficulty,
        title: formData.title,
      });

      setFormData({ topic: "", difficulty: "B1", title: "" });
      setShowCreateForm(false);
    } catch (error) {
      console.error("Failed to create session:", error);
    }
  };

  const handleStartRecording = async () => {
    try {
      if (audioRecorderRef.current) {
        await audioRecorderRef.current.start();
      }
    } catch (error) {
      setAudioState((prev) => ({
        ...prev,
        error: error instanceof Error ? error.message : "Recording failed",
      }));
    }
  };

  const handleStopRecording = () => {
    if (audioRecorderRef.current) {
      const audioBlob = audioRecorderRef.current.stop();

      if (activeSession && sessionQuery.data) {
        submitAnswerMutation.mutate(
          {
            sessionId: activeSession,
            request: {
              questionId: sessionQuery.data.currentQuestion,
              audioBlob,
              duration: audioState.duration,
            },
          },
          {
            onSuccess: () => {
              // Feedback will be fetched automatically
            },
          }
        );
      }
    }
  };

  const handleCompleteSession = async () => {
    if (activeSession) {
      try {
        await completeSessionMutation.mutateAsync(activeSession);
      } catch (error) {
        console.error("Failed to complete session:", error);
      }
    }
  };

  const isLoading = sessionsQuery.isLoading || (activeSession && sessionQuery.isLoading);
  const isError = sessionsQuery.isError || (activeSession && sessionQuery.isError);

  if (isLoading) {
    return <SkeletonList count={3} />;
  }

  if (isError) {
    return (
      <ErrorBoundary
        message="Failed to load speaking data"
        onRetry={() => {
          sessionsQuery.refetch();
          if (activeSession) {
            sessionQuery.refetch();
          }
        }}
      />
    );
  }

  return (
    <ErrorBoundary>
      <div className="w-full space-y-6">
        {/* Header */}
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold text-slate-900 dark:text-white">
                Speaking Practice
              </h2>
              <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
                Practice your speaking skills with AI feedback
              </p>
            </div>
            <button
              onClick={() => setShowCreateForm(!showCreateForm)}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 dark:bg-indigo-500 dark:hover:bg-indigo-600"
              aria-label="Create new speaking session"
            >
              New Session
            </button>
          </div>
        </div>

        {/* Create Session Form */}
        {showCreateForm && (
          <form
            onSubmit={handleCreateSession}
            className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900"
          >
            <h3 className="font-semibold text-slate-900 dark:text-white">
              Create Speaking Session
            </h3>

            <div className="mt-4 space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-900 dark:text-white">
                  Topic
                </label>
                <input
                  type="text"
                  value={formData.topic}
                  onChange={(e) =>
                    setFormData((prev) => ({ ...prev, topic: e.target.value }))
                  }
                  placeholder="e.g., Restaurant ordering, Job interview"
                  className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 placeholder-slate-400 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-800 dark:text-white dark:placeholder-slate-500"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-900 dark:text-white">
                  Difficulty
                </label>
                <select
                  value={formData.difficulty}
                  onChange={(e) =>
                    setFormData((prev) => ({
                      ...prev,
                      difficulty: e.target.value as "A1" | "A2" | "B1" | "B2" | "C1" | "C2",
                    }))
                  }
                  className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                >
                  <option value="A1">A1 - Beginner</option>
                  <option value="A2">A2 - Elementary</option>
                  <option value="B1">B1 - Intermediate</option>
                  <option value="B2">B2 - Upper Intermediate</option>
                  <option value="C1">C1 - Advanced</option>
                  <option value="C2">C2 - Proficiency</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-900 dark:text-white">
                  Title (Optional)
                </label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) =>
                    setFormData((prev) => ({ ...prev, title: e.target.value }))
                  }
                  placeholder="e.g., My first conversation"
                  className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 placeholder-slate-400 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:border-slate-600 dark:bg-slate-800 dark:text-white dark:placeholder-slate-500"
                />
              </div>

              <div className="flex gap-2">
                <button
                  type="submit"
                  disabled={createSessionMutation.isPending}
                  className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50 dark:bg-indigo-500 dark:hover:bg-indigo-600"
                >
                  {createSessionMutation.isPending ? "Creating..." : "Create"}
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreateForm(false)}
                  className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-900 hover:bg-slate-50 dark:border-slate-600 dark:text-white dark:hover:bg-slate-800"
                >
                  Cancel
                </button>
              </div>
            </div>
          </form>
        )}

        {/* Active Session */}
        {activeSession && sessionQuery.data && (
          <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
            <h3 className="font-semibold text-slate-900 dark:text-white">
              {sessionQuery.data.title}
            </h3>

            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <div>
                <p className="text-sm text-slate-600 dark:text-slate-400">Topic</p>
                <p className="mt-1 font-medium text-slate-900 dark:text-white">
                  {sessionQuery.data.topic}
                </p>
              </div>
              <div>
                <p className="text-sm text-slate-600 dark:text-slate-400">Difficulty</p>
                <p className="mt-1 font-medium text-slate-900 dark:text-white">
                  {sessionQuery.data.difficulty}
                </p>
              </div>
              <div>
                <p className="text-sm text-slate-600 dark:text-slate-400">Progress</p>
                <p className="mt-1 font-medium text-slate-900 dark:text-white">
                  {sessionQuery.data.currentQuestion}/{sessionQuery.data.totalQuestions}
                </p>
              </div>
              <div>
                <p className="text-sm text-slate-600 dark:text-slate-400">Status</p>
                <p className="mt-1 font-medium text-slate-900 dark:text-white">
                  {sessionQuery.data.status}
                </p>
              </div>
            </div>

            {/* Recording Controls */}
            <div className="mt-6 space-y-4">
              <div className="rounded-lg bg-slate-50 p-4 dark:bg-slate-800">
                <p className="text-sm font-medium text-slate-900 dark:text-white">
                  Recording Duration: {audioState.duration}s
                </p>
                {audioState.error && (
                  <p className="mt-2 text-sm text-red-600 dark:text-red-400">
                    {audioState.error}
                  </p>
                )}
              </div>

              <div className="flex gap-2">
                {!audioState.isRecording ? (
                  <button
                    onClick={handleStartRecording}
                    className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 dark:bg-red-500 dark:hover:bg-red-600"
                    aria-label="Start recording"
                  >
                    Start Recording
                  </button>
                ) : (
                  <>
                    {audioState.isPaused ? (
                      <button
                        onClick={() => audioRecorderRef.current?.resume()}
                        className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 dark:bg-indigo-500 dark:hover:bg-indigo-600"
                      >
                        Resume
                      </button>
                    ) : (
                      <button
                        onClick={() => audioRecorderRef.current?.pause()}
                        className="rounded-lg bg-amber-600 px-4 py-2 text-sm font-medium text-white hover:bg-amber-700 dark:bg-amber-500 dark:hover:bg-amber-600"
                      >
                        Pause
                      </button>
                    )}
                    <button
                      onClick={handleStopRecording}
                      className="rounded-lg bg-slate-600 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700 dark:bg-slate-500 dark:hover:bg-slate-600"
                    >
                      Stop
                    </button>
                  </>
                )}
              </div>
            </div>

            {/* Feedback */}
            {feedbackQuery.data && (
              <div className="mt-6 space-y-4">
                <h4 className="font-semibold text-slate-900 dark:text-white">Feedback</h4>
                <div className="rounded-lg bg-blue-50 p-4 dark:bg-blue-900">
                  <p className="text-sm text-slate-900 dark:text-white">
                    {feedbackQuery.data.explanation}
                  </p>
                </div>

                <div className="grid gap-4 md:grid-cols-3">
                  <div>
                    <p className="text-sm text-slate-600 dark:text-slate-400">Score</p>
                    <p className="mt-1 text-2xl font-bold text-indigo-600 dark:text-indigo-400">
                      {feedbackQuery.data.score}%
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-slate-600 dark:text-slate-400">Accuracy</p>
                    <p className="mt-1 text-2xl font-bold text-green-600 dark:text-green-400">
                      {feedbackQuery.data.accuracy}%
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-slate-600 dark:text-slate-400">Fluency</p>
                    <p className="mt-1 text-2xl font-bold text-amber-600 dark:text-amber-400">
                      {feedbackQuery.data.fluency}%
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Complete Session Button */}
            <button
              onClick={handleCompleteSession}
              disabled={completeSessionMutation.isPending}
              className="mt-6 w-full rounded-lg bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50 dark:bg-green-500 dark:hover:bg-green-600"
            >
              {completeSessionMutation.isPending ? "Completing..." : "Complete Session"}
            </button>
          </div>
        )}

        {/* Sessions List */}
        <div className="rounded-lg border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-900">
          <div className="border-b border-slate-200 px-6 py-4 dark:border-slate-700">
            <h3 className="font-semibold text-slate-900 dark:text-white">
              Your Sessions
            </h3>
          </div>
          <div className="divide-y divide-slate-200 dark:divide-slate-700">
            {sessionsQuery.data?.sessions && sessionsQuery.data.sessions.length > 0 ? (
              sessionsQuery.data.sessions.map((session) => (
                <div
                  key={session.id}
                  className="flex items-center justify-between px-6 py-4 hover:bg-slate-50 dark:hover:bg-slate-800"
                >
                  <div className="flex-1">
                    <p className="font-medium text-slate-900 dark:text-white">
                      {session.title}
                    </p>
                    <p className="text-sm text-slate-600 dark:text-slate-400">
                      {session.topic} • {session.difficulty} • {session.status}
                    </p>
                  </div>
                  {session.score && (
                    <div className="text-right">
                      <p className="text-lg font-bold text-indigo-600 dark:text-indigo-400">
                        {session.score}%
                      </p>
                    </div>
                  )}
                </div>
              ))
            ) : (
              <div className="px-6 py-8 text-center">
                <p className="text-slate-600 dark:text-slate-400">
                  No sessions yet. Create one to get started!
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </ErrorBoundary>
  );
}
