/**
 * Learning Main Page
 * 
 * Hub for all learning activities
 * Displays:
 * - Statistics dashboard
 * - Quick stats (words to review, learning progress)
 * - Navigation to Review Queue / History
 * - Learning progress list with pagination
 * 
 * Layout:
 * - Tab 1: Statistics & Overview
 * - Tab 2: Learning Progress (all words)
 * - Tab 3: Quick Links
 */

import { useState } from "react";
import { useLearning } from "@/hooks";
import Statistics from "@/components/Learning/Statistics";
import LearningCard from "@/components/Learning/LearningCard";
import LearningCardSkeleton from "@/components/Learning/LearningCardSkeleton";

/**
 * Learning Main Page
 */
export default function LearningPage() {
  const {
    statistics,
    statsLoading,
    statsError,
    progress,
    progressLoading,
    progressError,
    loadMoreProgress,
    progressTotalPages,
    progressPage,
    refetch,
  } = useLearning();

  const [activeTab, setActiveTab] = useState<"overview" | "progress">(
    "overview"
  );

  const hasMore = progressPage < (progressTotalPages ?? 0) - 1;

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-2">
          📚 Learning & Flashcard
        </h1>
        <p className="text-lg text-gray-600">
          Master English vocabulary with Spaced Repetition System
        </p>
      </div>

      {/* Navigation Tabs */}
      <div className="mb-6 flex gap-2 border-b border-gray-200">
        <button
          onClick={() => setActiveTab("overview")}
          className={`px-6 py-3 font-medium border-b-2 transition-colors ${
            activeTab === "overview"
              ? "border-blue-600 text-blue-600"
              : "border-transparent text-gray-600 hover:text-gray-900"
          }`}
        >
          📊 Statistics
        </button>
        <button
          onClick={() => setActiveTab("progress")}
          className={`px-6 py-3 font-medium border-b-2 transition-colors ${
            activeTab === "progress"
              ? "border-blue-600 text-blue-600"
              : "border-transparent text-gray-600 hover:text-gray-900"
          }`}
        >
          📖 My Learning ({progress.length})
        </button>
      </div>

      {/* Tab 1: Overview & Statistics */}
      {activeTab === "overview" && (
        <div className="space-y-6">
          {/* Statistics Dashboard */}
          <Statistics
            statistics={statistics}
            isLoading={statsLoading}
            error={statsError}
            onRefresh={refetch}
          />

          {/* Quick Links */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Review Queue Link */}
            <a
              href="/learning/review-queue"
              className="p-6 bg-gradient-to-br from-orange-50 to-amber-50 rounded-lg border-2 border-orange-200 hover:shadow-lg transition-shadow hover:border-orange-300"
            >
              <div className="text-4xl mb-2">🔄</div>
              <h3 className="text-lg font-bold text-orange-900 mb-1">Review Queue</h3>
              <p className="text-sm text-orange-700">
                {statistics?.wordsNeedingReview || 0} words due for review
              </p>
              <p className="text-xs text-orange-600 mt-2">→ Start reviewing</p>
            </a>

            {/* Learning Progress Link */}
            <a
              href="#"
              onClick={() => setActiveTab("progress")}
              className="p-6 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-lg border-2 border-blue-200 hover:shadow-lg transition-shadow hover:border-blue-300 cursor-pointer"
            >
              <div className="text-4xl mb-2">📚</div>
              <h3 className="text-lg font-bold text-blue-900 mb-1">My Progress</h3>
              <p className="text-sm text-blue-700">
                {statistics?.learningWords || 0} words in progress
              </p>
              <p className="text-xs text-blue-600 mt-2">→ View all</p>
            </a>

            {/* History Link */}
            <a
              href="/learning/history"
              className="p-6 bg-gradient-to-br from-purple-50 to-pink-50 rounded-lg border-2 border-purple-200 hover:shadow-lg transition-shadow hover:border-purple-300"
            >
              <div className="text-4xl mb-2">📝</div>
              <h3 className="text-lg font-bold text-purple-900 mb-1">History</h3>
              <p className="text-sm text-purple-700">
                {statistics?.totalActivities || 0} activities
              </p>
              <p className="text-xs text-purple-600 mt-2">→ View timeline</p>
            </a>
          </div>

          {/* Learning Tips */}
          <div className="p-6 bg-gradient-to-r from-indigo-50 to-blue-50 rounded-lg border-2 border-indigo-200">
            <h3 className="text-lg font-bold text-indigo-900 mb-4">
              💡 How to Learn Effectively
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-indigo-800">
              <div>
                <p className="font-semibold mb-2">🎯 Spaced Repetition</p>
                <p>
                  Review words at optimal intervals: 1, 3, 7, 14, 30 days apart
                </p>
              </div>
              <div>
                <p className="font-semibold mb-2">✅ Consistency</p>
                <p>
                  Daily practice is key. Review at least a few words each day
                </p>
              </div>
              <div>
                <p className="font-semibold mb-2">⚡ Active Recall</p>
                <p>
                  Test yourself before checking the answer for better retention
                </p>
              </div>
              <div>
                <p className="font-semibold mb-2">🔄 Mastery</p>
                <p>
                  A word is mastered with 80%+ accuracy and 5+ successful reviews
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: Learning Progress */}
      {activeTab === "progress" && (
        <div className="space-y-6">
          {/* Filter/Sort Bar */}
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-bold text-gray-900">
              All Learning Progress
            </h2>
            {progress.length > 0 && (
              <p className="text-sm text-gray-600">
                Showing {progress.length} of {statistics?.totalWords || 0} words
              </p>
            )}
          </div>

          {/* Error State */}
          {progressError && (
            <div className="p-4 bg-red-50 border-2 border-red-200 rounded-lg">
              <p className="text-red-900 font-semibold mb-2">
                ❌ Failed to load progress
              </p>
              <p className="text-red-700 text-sm mb-4">{progressError.message}</p>
              <button
                onClick={() => window.location.reload()}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors"
              >
                Try Again
              </button>
            </div>
          )}

          {/* Loading State */}
          {progressLoading && progress.length === 0 && (
            <LearningCardSkeleton count={3} />
          )}

          {/* Empty State */}
          {!progressLoading && progress.length === 0 && !progressError && (
            <div className="text-center py-12">
              <div className="text-6xl mb-4">🌱</div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">
                Start Learning!
              </h2>
              <p className="text-gray-600 mb-6">
                Browse vocabulary to add words to your learning queue.
              </p>
              <a
                href="/vocabulary"
                className="inline-block px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
              >
                Browse Vocabulary
              </a>
            </div>
          )}

          {/* Learning Cards */}
          {progress.length > 0 && (
            <div className="space-y-4">
              {progress.map((word) => (
                <LearningCard
                  key={word.id}
                  word={word}
                  variant="compact"
                />
              ))}

              {/* Load More Button */}
              {hasMore && (
                <div className="text-center pt-4">
                  <button
                    onClick={loadMoreProgress}
                    className="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-medium transition-colors"
                  >
                    Load More ({progressPage + 1}/{progressTotalPages})
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
