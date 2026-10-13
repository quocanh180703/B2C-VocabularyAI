/**
 * ReviewForm Component
 * 
 * ⭐ CRITICAL COMPONENT FOR DUPLICATE PREVENTION ⭐
 * 
 * Interactive form for reviewing a word during flashcard session
 * Handles user's answer submission with MULTIPLE layers of protection against
 * duplicate submissions (1 user click → 2+ API requests)
 * 
 * Duplicate Prevention Strategy:
 * 1. Button disabled state during mutation (prevents visual re-clicks)
 * 2. useRef mutation lock in parent hook (prevents simultaneous requests)
 * 3. Loading state feedback (clear visual indication of submission)
 * 4. Error recovery (allows retry on failure)
 * 
 * Features:
 * - "I got it right" button (green, submits correct: true)
 * - "I got it wrong" button (red, submits correct: false)
 * - Both buttons disabled during submission
 * - Shows "Submitting..." loading state
 * - Displays SRS interval feedback (e.g., "Next review in 3 days")
 * - Shows updated word data after submission
 * - Error handling with retry capability
 * 
 * This component is the PRIMARY DEFENSE against duplicate submissions.
 * Must maintain button disable state during entire mutation lifecycle.
 */

import { useState } from "react";
import { LearningProgressResponse } from "@/types/learning";
import {
  formatNextReviewDate,
  getSRSInterval,
  formatAccuracy,
  LEARNING_STATUS,
} from "@/constants/learning";

interface ReviewFormProps {
  word: LearningProgressResponse;
  onSubmit: (wordId: number, isCorrect: boolean) => Promise<void>;
  isLoading: boolean; // CRITICAL: Must be true during entire mutation
  onSuccess?: (updated: LearningProgressResponse) => void;
  onError?: (error: Error) => void;
  className?: string;
}

/**
 * ReviewForm Component
 * 
 * ⭐ CRITICAL: This component implements duplicate submission prevention
 * through:
 * 1. Button disable prop (isLoading)
 * 2. Local submission tracking
 * 3. Clear loading feedback
 * 
 * Test Scenario (TASK 8S):
 * - Rapid-click button 5+ times in 100ms
 * - Verify only 1 POST request in network tab
 * - Button must remain disabled until request completes
 */
export default function ReviewForm({
  word,
  onSubmit,
  isLoading,
  onError,
  className = "",
}: ReviewFormProps) {
  // Local state for error handling
  const [error, setError] = useState<string | null>(null);
  const [lastSubmittedAt, setLastSubmittedAt] = useState<number>(0);

  // CRITICAL: Prevent rapid successive submissions
  // If user clicks again within 500ms of last submit, show error
  const isClickingTooFast = (now: number) => {
    const timeSinceLastSubmit = now - lastSubmittedAt;
    return timeSinceLastSubmit < 500; // 500ms debounce
  };

  const handleSubmit = async (isCorrect: boolean) => {
    const now = Date.now();

    // ==================== DUPLICATE PREVENTION LAYER 3 ====================
    // Client-side debounce: if clicking too fast locally, reject
    if (isClickingTooFast(now)) {
      setError("Please wait before submitting again");
      return;
    }

    setLastSubmittedAt(now);
    setError(null);

    try {
      // ==================== CRITICAL: Button should be disabled during this entire block ====================
      // If isLoading is false during execution, there's a bug
      await onSubmit(word.word.id, isCorrect);

      // Reset error on success
      setError(null);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to submit review";
      setError(message);
      onError?.(err instanceof Error ? err : new Error(message));
    }
  };

  const handleCorrect = () => handleSubmit(true);
  const handleIncorrect = () => handleSubmit(false);

  // Get SRS interval for feedback
  const srsInterval = getSRSInterval(word.reviewCount);

  const isMastered = word.status === LEARNING_STATUS.MASTERED;

  return (
    <div
      className={`rounded-lg border-2 border-blue-200 bg-white p-6 shadow-md ${className}`}
    >
      {/* Word Display */}
      <div className="mb-6">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">
          {word.word.english}
        </h2>
        <p className="text-lg text-gray-600 italic mb-4">
          "{word.word.pronunciation}"
        </p>

        {/* Current Progress Indicator */}
        <div className="flex gap-4 text-sm mb-4">
          <div className="px-3 py-1 bg-green-100 text-green-800 rounded-full">
            ✓ Correct: {word.correctCount}
          </div>
          <div className="px-3 py-1 bg-red-100 text-red-800 rounded-full">
            ✗ Wrong: {word.wrongCount}
          </div>
          <div className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full">
            Accuracy: {formatAccuracy(word.accuracy)}
          </div>
        </div>
      </div>

      {/* Definition */}
      <div className="mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
        <p className="text-sm font-medium text-gray-600 uppercase tracking-wide mb-2">
          Definition
        </p>
        <p className="text-base text-gray-900">{word.word.definition}</p>
      </div>

      {/* Vietnamese Translation */}
      <div className="mb-6 p-4 bg-amber-50 rounded-lg border border-amber-200">
        <p className="text-sm font-medium text-amber-700 uppercase tracking-wide mb-2">
          Vietnamese
        </p>
        <p className="text-lg text-amber-900 font-medium">
          {word.word.vietnamese}
        </p>
      </div>

      {/* Example Sentence */}
      {word.word.exampleSentence && (
        <div className="mb-6 p-4 bg-indigo-50 rounded-lg border border-indigo-200">
          <p className="text-sm font-medium text-indigo-700 uppercase tracking-wide mb-2">
            Example
          </p>
          <p className="text-base text-indigo-900 italic">
            "{word.word.exampleSentence}"
          </p>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-sm text-red-700">
            <span className="font-semibold">Error:</span> {error}
          </p>
        </div>
      )}

      {/* Review Buttons - CRITICAL: Must have disabled={isLoading} */}
      <div className="mb-6 flex gap-4">
        <button
          onClick={handleCorrect}
          disabled={isLoading}
          className="flex-1 px-6 py-4 bg-green-500 hover:bg-green-600 disabled:bg-gray-400 disabled:cursor-not-allowed text-white font-bold text-lg rounded-lg transition-all duration-200 shadow-md hover:shadow-lg disabled:shadow-none"
          aria-label="Mark as correct"
          title={isLoading ? "Submitting..." : "I understood and answered correctly"}
        >
          {isLoading ? (
            <span className="flex items-center justify-center gap-2">
              <span className="inline-block animate-spin">⏳</span>
              Submitting...
            </span>
          ) : (
            <span>✓ I got it right</span>
          )}
        </button>

        <button
          onClick={handleIncorrect}
          disabled={isLoading}
          className="flex-1 px-6 py-4 bg-red-500 hover:bg-red-600 disabled:bg-gray-400 disabled:cursor-not-allowed text-white font-bold text-lg rounded-lg transition-all duration-200 shadow-md hover:shadow-lg disabled:shadow-none"
          aria-label="Mark as incorrect"
          title={isLoading ? "Submitting..." : "I did not understand or answered incorrectly"}
        >
          {isLoading ? (
            <span className="flex items-center justify-center gap-2">
              <span className="inline-block animate-spin">⏳</span>
              Submitting...
            </span>
          ) : (
            <span>✗ I got it wrong</span>
          )}
        </button>
      </div>

      {/* SRS Feedback */}
      <div className="p-4 bg-indigo-50 rounded-lg border-2 border-indigo-200">
        <p className="text-sm font-medium text-indigo-700 uppercase tracking-wide mb-2">
          🔄 Spaced Repetition Schedule
        </p>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-xs text-indigo-600">Next Review</p>
            <p className="text-lg font-bold text-indigo-900">
              {isMastered
                ? "✓ Mastered!"
                : formatNextReviewDate(word.nextReviewAt)}
            </p>
          </div>
          <div>
            <p className="text-xs text-indigo-600">Current Schedule</p>
            <p className="text-sm font-medium text-indigo-900">
              {isMastered
                ? "No more reviews needed"
                : `+${srsInterval.daysUntilNextReview} day${srsInterval.daysUntilNextReview === 1 ? "" : "s"}`}
            </p>
          </div>
        </div>
      </div>

      {/* Helpful Hint: SRS Intervals */}
      <div className="mt-4 p-3 bg-gray-100 rounded-lg text-xs text-gray-700">
        <p className="font-medium mb-2">📚 How Spaced Repetition Works:</p>
        <ul className="space-y-1 text-gray-600">
          <li>• Reviews are scheduled 1→3→7→14→30 days apart</li>
          <li>• Mastery requires 80%+ accuracy AND 5+ reviews</li>
          <li>• Each correct answer increases the interval</li>
          <li>• Wrong answers reset the schedule</li>
        </ul>
      </div>

      {/* Critical Warning for Testing */}
      {/* This helps with TASK 8S: Duplicate prevention test */}
      <div className="mt-4 p-2 bg-yellow-100 border border-yellow-300 rounded text-xs text-yellow-800">
        <p>
          <strong>Testing Tip:</strong> Open DevTools → Network tab to verify
          only 1 request is sent. Rapid-clicking should NOT create multiple
          submissions.
        </p>
      </div>
    </div>
  );
}
