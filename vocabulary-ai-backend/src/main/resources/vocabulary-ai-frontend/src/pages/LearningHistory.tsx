/**
 * Learning History Page
 * 
 * Displays detailed audit log of all learning activities
 * Shows LEARN, REVIEW, QUIZ, SPEAKING activities
 * 
 * Features:
 * - Timeline view of activities
 * - Pagination with load more
 * - Filter by activity type (optional)
 * - Empty state
 * - Loading skeletons
 */

import { useState } from "react";
import { useLearning } from "@/hooks";
import HistoryItem from "@/components/History/HistoryItem";
import { ACTIVITY_TYPE, ALL_ACTIVITY_TYPES, ACTIVITY_TYPE_DISPLAY_MAP } from "@/constants/learning";
import type { ActivityType } from "@/types/learning";

/**
 * Learning History Page Component
 */
export default function LearningHistoryPage() {
  const {
    history,
    historyLoading,
    historyError,
    loadMoreHistory,
    historyPage,
    historyTotalPages,
  } = useLearning();

  // Optional: Filter state (can be implemented later)
  const [selectedActivityType, setSelectedActivityType] = useState<ActivityType | "ALL">(
    "ALL"
  );

  const filteredHistory =
    selectedActivityType === "ALL"
      ? history
      : history.filter((entry) => entry.activityType === selectedActivityType);

  const hasMore = historyPage < (historyTotalPages ?? 0) - 1;

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-2">
          📚 Learning History
        </h1>
        <p className="text-lg text-gray-600">
          Detailed activity log of all your learning sessions
        </p>
        {history.length > 0 && (
          <p className="text-sm text-gray-500 mt-2">
            {history.length} activity{history.length !== 1 ? "ies" : ""}
          </p>
        )}
      </div>

      {/* Error State */}
      {historyError && (
        <div className="mb-6 p-4 bg-red-50 border-2 border-red-200 rounded-lg">
          <p className="text-red-900 font-semibold mb-2">❌ Failed to load history</p>
          <p className="text-red-700 text-sm mb-4">{historyError.message}</p>
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors"
          >
            Try Again
          </button>
        </div>
      )}

      {/* Activity Type Filter (Optional - can enhance later) */}
      {history.length > 0 && (
        <div className="mb-6 flex flex-wrap gap-2">
          <button
            onClick={() => setSelectedActivityType("ALL")}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              selectedActivityType === "ALL"
                ? "bg-blue-600 text-white"
                : "bg-gray-200 text-gray-800 hover:bg-gray-300"
            }`}
          >
            All Activities
          </button>
          {ALL_ACTIVITY_TYPES.map((type) => (
            <button
              key={type}
              onClick={() => setSelectedActivityType(type)}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                selectedActivityType === type
                  ? "bg-blue-600 text-white"
                  : "bg-gray-200 text-gray-800 hover:bg-gray-300"
              }`}
            >
              {ACTIVITY_TYPE_DISPLAY_MAP[type].icon}{" "}
              {ACTIVITY_TYPE_DISPLAY_MAP[type].label}
            </button>
          ))}
        </div>
      )}

      {/* Loading State */}
      {historyLoading && history.length === 0 && (
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <div
              key={i}
              className="h-24 bg-gray-200 rounded-lg animate-pulse"
            ></div>
          ))}
        </div>
      )}

      {/* Empty State */}
      {!historyLoading && history.length === 0 && !historyError && (
        <div className="text-center py-12">
          <div className="text-6xl mb-4">📖</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            No learning history yet
          </h2>
          <p className="text-gray-600 mb-6">
            Start learning words and your activity will appear here as a
            timeline.
          </p>
          <a
            href="/learning"
            className="inline-block px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
          >
            Start Learning
          </a>
        </div>
      )}

      {/* History Timeline */}
      {history.length > 0 && (
        <div>
          {filteredHistory.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-600">
                No activities of this type in your history.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {filteredHistory.map((entry) => (
                <HistoryItem key={entry.id} entry={entry} />
              ))}
            </div>
          )}

          {/* Load More Button */}
          {hasMore && filteredHistory.length > 0 && (
            <div className="text-center pt-6">
              <button
                onClick={loadMoreHistory}
                className="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-medium transition-colors"
              >
                Load More History ({historyPage + 1}/{historyTotalPages})
              </button>
            </div>
          )}
        </div>
      )}

      {/* Stats Summary */}
      {history.length > 0 && (
        <div className="mt-12 grid grid-cols-2 gap-4 md:grid-cols-4">
          <div className="p-4 bg-blue-50 rounded-lg border border-blue-200 text-center">
            <p className="text-2xl font-bold text-blue-900">
              {history.filter((e) => e.activityType === ACTIVITY_TYPE.LEARN)
                .length}
            </p>
            <p className="text-sm text-blue-700">Learned</p>
          </div>
          <div className="p-4 bg-green-50 rounded-lg border border-green-200 text-center">
            <p className="text-2xl font-bold text-green-900">
              {history.filter(
                (e) => e.activityType === ACTIVITY_TYPE.REVIEW && e.isCorrect
              ).length}
            </p>
            <p className="text-sm text-green-700">Correct Reviews</p>
          </div>
          <div className="p-4 bg-red-50 rounded-lg border border-red-200 text-center">
            <p className="text-2xl font-bold text-red-900">
              {history.filter(
                (e) => e.activityType === ACTIVITY_TYPE.REVIEW && e.isCorrect === false
              ).length}
            </p>
            <p className="text-sm text-red-700">Incorrect Reviews</p>
          </div>
          <div className="p-4 bg-purple-50 rounded-lg border border-purple-200 text-center">
            <p className="text-2xl font-bold text-purple-900">
              {history.filter(
                (e) => e.activityType === ACTIVITY_TYPE.REVIEW
              ).length}
            </p>
            <p className="text-sm text-purple-700">Total Reviews</p>
          </div>
        </div>
      )}
    </div>
  );
}
