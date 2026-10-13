/**
 * LearningCard Component
 * 
 * Displays a single word with detailed learning progress information
 * Shows:
 * - Word (English + pronunciation)
 * - Vietnamese meaning
 * - Definition
 * - Example sentence
 * - Progress indicators (correct/wrong counts, accuracy %)
 * - Next review date or "Mastered" badge
 * - Status badge (NEW/LEARNING/MASTERED)
 * 
 * Used in:
 * - Learning page (main learning list)
 * - Review queue page (flashcard style)
 * - Learning history (with metadata)
 */

import { LearningProgressResponse } from "@/types/learning";
import {
  STATUS_DISPLAY_MAP,
  formatAccuracy,
  formatReviewCount,
  formatNextReviewDate,
  MASTERY_CRITERIA,
} from "@/constants/learning";

interface LearningCardProps {
  word: LearningProgressResponse;
  onReview?: (wordId: number, isCorrect: boolean) => Promise<void>;
  isReviewing?: boolean;
  onStartLearning?: (wordId: number) => Promise<void>;
  isStarting?: boolean;
  className?: string;
  variant?: "compact" | "detailed"; // compact = minimal info, detailed = full
}

/**
 * LearningCard Component
 * 
 * Displays word with progress information
 * Can be used in different contexts with variant prop
 */
export default function LearningCard({
  word,
  onReview,
  isReviewing = false,
  onStartLearning,
  isStarting = false,
  className = "",
  variant = "detailed",
}: LearningCardProps) {
  const statusDisplay = STATUS_DISPLAY_MAP[word.status];
  const isNearMastery =
    word.accuracy >= MASTERY_CRITERIA.minAccuracy &&
    word.reviewCount < MASTERY_CRITERIA.minReviewCount;
  const isMastered = word.status === "MASTERED";

  const handleReviewCorrect = async () => {
    if (onReview && !isReviewing) {
      await onReview(word.word.id, true);
    }
  };

  const handleReviewIncorrect = async () => {
    if (onReview && !isReviewing) {
      await onReview(word.word.id, false);
    }
  };

  // Compact variant - minimal info
  if (variant === "compact") {
    return (
      <div
        className={`rounded-lg border border-gray-200 bg-white p-4 shadow-sm hover:shadow-md transition-shadow ${className}`}
      >
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-baseline gap-2 mb-1">
              <h3 className="text-lg font-semibold text-gray-900 truncate">
                {word.word.english}
              </h3>
              <span className="text-sm text-gray-500 flex-shrink-0">
                {word.word.pronunciation}
              </span>
            </div>
            <p className="text-sm text-gray-600 line-clamp-1">
              {word.word.vietnamese}
            </p>
          </div>

          {/* Status Badge */}
          <div
            className={`flex-shrink-0 px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap ${statusDisplay.color}`}
          >
            {statusDisplay.icon} {statusDisplay.label}
          </div>
        </div>

        {/* Progress Bar */}
        <div className="mt-3">
          <div className="flex justify-between items-center mb-1">
            <span className="text-xs font-medium text-gray-600">
              Accuracy: {formatAccuracy(word.accuracy)}
            </span>
            <span className="text-xs text-gray-500">
              {formatReviewCount(word.correctCount, word.wrongCount)}
            </span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className={`h-2 rounded-full transition-all ${
                word.accuracy >= 80
                  ? "bg-green-500"
                  : word.accuracy >= 50
                    ? "bg-yellow-500"
                    : "bg-red-500"
              }`}
              style={{ width: `${word.accuracy}%` }}
            />
          </div>
        </div>
      </div>
    );
  }

  // Detailed variant - full information
  return (
    <div
      className={`rounded-lg border border-gray-200 bg-white shadow-sm hover:shadow-md transition-shadow overflow-hidden ${className}`}
    >
      {/* Header with Status */}
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 px-6 py-4 border-b border-gray-200">
        <div className="flex items-start justify-between gap-4 mb-2">
          <div className="flex-1">
            <h3 className="text-2xl font-bold text-gray-900">
              {word.word.english}
            </h3>
            <p className="text-sm text-gray-600 mt-1">
              {word.word.pronunciation}
            </p>
          </div>
          <div
            className={`flex-shrink-0 px-4 py-2 rounded-lg font-medium text-sm ${statusDisplay.color}`}
          >
            {statusDisplay.icon} {statusDisplay.label}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="px-6 py-4">
        {/* Vietnamese Translation */}
        <div className="mb-4">
          <p className="text-sm font-medium text-gray-500 uppercase tracking-wide">
            Translation
          </p>
          <p className="text-lg text-gray-900 mt-1">{word.word.vietnamese}</p>
        </div>

        {/* Definition */}
        <div className="mb-4">
          <p className="text-sm font-medium text-gray-500 uppercase tracking-wide">
            Definition
          </p>
          <p className="text-base text-gray-700 mt-1">{word.word.definition}</p>
        </div>

        {/* Example Sentence */}
        {word.word.exampleSentence && (
          <div className="mb-4">
            <p className="text-sm font-medium text-gray-500 uppercase tracking-wide">
              Example
            </p>
            <p className="text-sm text-gray-600 italic mt-1">
              "{word.word.exampleSentence}"
            </p>
          </div>
        )}

        {/* Progress Section */}
        <div className="mt-6 pt-4 border-t border-gray-200">
          <div className="grid grid-cols-2 gap-4 mb-4">
            {/* Correct Count */}
            <div>
              <p className="text-xs text-gray-500 uppercase tracking-wide">
                Correct
              </p>
              <p className="text-2xl font-bold text-green-600">
                {word.correctCount}
              </p>
            </div>

            {/* Wrong Count */}
            <div>
              <p className="text-xs text-gray-500 uppercase tracking-wide">
                Incorrect
              </p>
              <p className="text-2xl font-bold text-red-600">
                {word.wrongCount}
              </p>
            </div>
          </div>

          {/* Accuracy Progress Bar */}
          <div className="mb-4">
            <div className="flex justify-between items-center mb-2">
              <p className="text-sm font-medium text-gray-700">
                Accuracy: {formatAccuracy(word.accuracy)}
              </p>
              <p className="text-xs text-gray-500">
                {word.reviewCount} {word.reviewCount === 1 ? "review" : "reviews"}
              </p>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-3">
              <div
                className={`h-3 rounded-full transition-all ${
                  word.accuracy >= 80
                    ? "bg-green-500"
                    : word.accuracy >= 50
                      ? "bg-yellow-500"
                      : "bg-red-500"
                }`}
                style={{ width: `${word.accuracy}%` }}
              />
            </div>
          </div>

          {/* Mastery Progress (if not mastered) */}
          {!isMastered && (
            <div className="mb-4 p-3 bg-blue-50 rounded-lg border border-blue-200">
              <p className="text-xs font-medium text-blue-900 mb-1">
                Mastery Goal
              </p>
              <div className="flex gap-4 text-sm">
                <div>
                  <p className="text-xs text-blue-700">
                    Accuracy:{" "}
                    <span className="font-bold">
                      {word.accuracy >= MASTERY_CRITERIA.minAccuracy
                        ? "✓"
                        : `${MASTERY_CRITERIA.minAccuracy - word.accuracy}% to go`}
                    </span>
                  </p>
                </div>
                <div>
                  <p className="text-xs text-blue-700">
                    Reviews:{" "}
                    <span className="font-bold">
                      {word.reviewCount} /{MASTERY_CRITERIA.minReviewCount}
                    </span>
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* Next Review Date or Mastered */}
          <div
            className={`p-3 rounded-lg border ${
              isMastered
                ? "bg-green-50 border-green-200"
                : "bg-indigo-50 border-indigo-200"
            }`}
          >
            <p className="text-xs font-medium text-gray-600 uppercase tracking-wide mb-1">
              Next Review
            </p>
            <p
              className={`text-sm font-semibold ${
                isMastered
                  ? "text-green-700"
                  : word.needsReview
                    ? "text-orange-600"
                    : "text-indigo-700"
              }`}
            >
              {isMastered && "⭐ "}
              {formatNextReviewDate(word.nextReviewAt)}
            </p>
          </div>
        </div>
      </div>

      {/* Review Buttons (only if onReview provided) */}
      {onReview && (
        <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 flex gap-3">
          <button
            onClick={handleReviewCorrect}
            disabled={isReviewing}
            className="flex-1 px-4 py-2 bg-green-500 hover:bg-green-600 disabled:bg-gray-400 text-white font-medium rounded-lg transition-colors disabled:cursor-not-allowed"
          >
            {isReviewing ? "Submitting..." : "✓ Got it right"}
          </button>
          <button
            onClick={handleReviewIncorrect}
            disabled={isReviewing}
            className="flex-1 px-4 py-2 bg-red-500 hover:bg-red-600 disabled:bg-gray-400 text-white font-medium rounded-lg transition-colors disabled:cursor-not-allowed"
          >
            {isReviewing ? "Submitting..." : "✗ Got it wrong"}
          </button>
        </div>
      )}

      {/* Start Learning Button (only if onStartLearning provided and status is NEW) */}
      {onStartLearning && word.status === "NEW" && (
        <div className="px-6 py-4 bg-gray-50 border-t border-gray-200">
          <button
            onClick={() => onStartLearning(word.word.id)}
            disabled={isStarting}
            className="w-full px-4 py-2 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 text-white font-medium rounded-lg transition-colors disabled:cursor-not-allowed"
          >
            {isStarting ? "Starting..." : "📚 Start Learning"}
          </button>
        </div>
      )}
    </div>
  );
}
