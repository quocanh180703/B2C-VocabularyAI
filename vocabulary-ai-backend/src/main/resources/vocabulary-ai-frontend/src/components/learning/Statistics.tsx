/**
 * Statistics Component
 * 
 * Displays comprehensive summary of user's learning progress
 * Shows dashboard-style metrics with visual indicators
 * 
 * Displays:
 * - Total words learned (counter)
 * - Learning progress % (progress ring)
 * - Mastery % (progress ring)
 * - Words needing review (counter with urgency)
 * - Overall accuracy rate (percentage)
 * - Total activities count (counter)
 * 
 * All values come from backend and are displayed as-is.
 * Frontend does NOT recalculate these values.
 * 
 * Loading state: Skeleton loaders for each metric
 * Error state: Graceful error message with retry option
 */

import { ProgressStatisticsResponse } from "@/types/learning";
import { formatDuration } from "@/constants/learning";

interface StatisticsProps {
  statistics: ProgressStatisticsResponse | null;
  isLoading: boolean;
  error?: Error;
  onRefresh?: () => void;
  className?: string;
}

/**
 * Circular Progress Ring Component
 * Displays percentage in a circular visual format
 */
function ProgressRing({
  percentage,
  label,
  color,
}: {
  percentage: number;
  label: string;
  color: "green" | "blue" | "purple";
}) {
  const circumference = 2 * Math.PI * 45; // radius = 45
  const offset = circumference - (percentage / 100) * circumference;

  const colorMap = {
    green: "text-green-600",
    blue: "text-blue-600",
    purple: "text-purple-600",
  };

  return (
    <div className="flex flex-col items-center">
      <div className="relative w-32 h-32">
        <svg className="w-full h-full transform -rotate-90">
          {/* Background circle */}
          <circle
            cx="64"
            cy="64"
            r="45"
            stroke="#e5e7eb"
            strokeWidth="8"
            fill="none"
          />
          {/* Progress circle */}
          <circle
            cx="64"
            cy="64"
            r="45"
            stroke="currentColor"
            strokeWidth="8"
            fill="none"
            strokeDasharray={circumference}
            strokeDashoffset={offset}
            strokeLinecap="round"
            className={`transition-all duration-300 ${colorMap[color]}`}
          />
        </svg>
        {/* Center text */}
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-3xl font-bold text-gray-900">
            {percentage.toFixed(1)}%
          </span>
        </div>
      </div>
      <p className="text-sm font-medium text-gray-600 mt-3">{label}</p>
    </div>
  );
}

/**
 * Stat Card Component
 * Displays a single metric with title and value
 */
function StatCard({
  icon,
  label,
  value,
  subtext,
  color,
}: {
  icon: string;
  label: string;
  value: string | number;
  subtext?: string;
  color: "bg-blue" | "bg-green" | "bg-purple" | "bg-amber" | "bg-red";
}) {
  const colorMap = {
    "bg-blue": "bg-blue-100 text-blue-700 border-blue-200",
    "bg-green": "bg-green-100 text-green-700 border-green-200",
    "bg-purple": "bg-purple-100 text-purple-700 border-purple-200",
    "bg-amber": "bg-amber-100 text-amber-700 border-amber-200",
    "bg-red": "bg-red-100 text-red-700 border-red-200",
  };

  return (
    <div
      className={`rounded-lg border-2 p-4 text-center ${colorMap[color]}`}
    >
      <p className="text-3xl mb-1">{icon}</p>
      <p className="text-sm font-medium opacity-75">{label}</p>
      <p className="text-3xl font-bold mt-2">{value}</p>
      {subtext && <p className="text-xs mt-1 opacity-75">{subtext}</p>}
    </div>
  );
}

/**
 * Skeleton Loader for Statistics
 */
function StatisticsSkeleton() {
  return (
    <div className="space-y-6 animate-pulse">
      {/* Header Skeleton */}
      <div className="h-8 bg-gray-200 rounded w-48"></div>

      {/* Stat Cards Skeleton */}
      <div className="grid grid-cols-2 gap-4">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="h-24 bg-gray-200 rounded-lg"></div>
        ))}
      </div>

      {/* Progress Rings Skeleton */}
      <div className="grid grid-cols-2 gap-8 justify-items-center">
        {[...Array(2)].map((_, i) => (
          <div key={i} className="w-32 h-32 bg-gray-200 rounded-full"></div>
        ))}
      </div>
    </div>
  );
}

/**
 * Statistics Component
 * Main dashboard component for learning statistics
 */
export default function Statistics({
  statistics,
  isLoading,
  error,
  onRefresh,
  className = "",
}: StatisticsProps) {
  if (isLoading && !statistics) {
    return (
      <div className={`rounded-lg border border-gray-200 bg-white p-6 ${className}`}>
        <StatisticsSkeleton />
      </div>
    );
  }

  if (error) {
    return (
      <div className={`rounded-lg border-2 border-red-200 bg-red-50 p-6 ${className}`}>
        <div className="text-center">
          <p className="text-lg font-semibold text-red-800 mb-2">
            ❌ Failed to load statistics
          </p>
          <p className="text-sm text-red-600 mb-4">{error.message}</p>
          {onRefresh && (
            <button
              onClick={onRefresh}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors"
            >
              Try Again
            </button>
          )}
        </div>
      </div>
    );
  }

  if (!statistics) {
    return (
      <div className={`rounded-lg border-2 border-gray-200 bg-gray-50 p-6 ${className}`}>
        <p className="text-center text-gray-600">
          Start learning to see your statistics.
        </p>
      </div>
    );
  }

  const hasNoWords = statistics.totalWords === 0;

  return (
    <div className={`rounded-lg border border-gray-200 bg-white p-6 ${className}`}>
      {/* Header */}
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900">📊 Your Progress</h2>
        {onRefresh && (
          <button
            onClick={onRefresh}
            className="text-gray-500 hover:text-gray-700 transition-colors"
            title="Refresh statistics"
          >
            🔄
          </button>
        )}
      </div>

      {hasNoWords ? (
        // Empty state
        <div className="p-8 text-center bg-blue-50 rounded-lg border-2 border-blue-200">
          <p className="text-lg font-semibold text-blue-900 mb-2">
            📚 Start Learning to See Progress
          </p>
          <p className="text-sm text-blue-700">
            Once you learn words, your statistics will appear here.
          </p>
        </div>
      ) : (
        <>
          {/* Summary Stats - 4 Column Grid */}
          <div className="mb-8 grid grid-cols-2 gap-4 md:grid-cols-4">
            <StatCard
              icon="📚"
              label="Total Words"
              value={statistics.totalWords}
              color="bg-blue"
            />
            <StatCard
              icon="🎓"
              label="Learning"
              value={statistics.learningWords}
              subtext={`${statistics.newWords} new`}
              color="bg-purple"
            />
            <StatCard
              icon="⭐"
              label="Mastered"
              value={statistics.masteredWords}
              subtext={`${(
                (statistics.masteredWords / statistics.totalWords) *
                100
              ).toFixed(0)}%`}
              color="bg-green"
            />
            <StatCard
              icon="🔄"
              label="To Review"
              value={statistics.wordsNeedingReview}
              subtext={
                statistics.wordsNeedingReview > 0
                  ? "Review today!"
                  : "All caught up!"
              }
              color={
                statistics.wordsNeedingReview > 0 ? "bg-amber" : "bg-green"
              }
            />
          </div>

          {/* Progress Rings - Learning & Mastery */}
          <div className="mb-8 grid grid-cols-2 gap-8 px-4 py-8 bg-gray-50 rounded-lg">
            <ProgressRing
              percentage={statistics.learningPercentage}
              label="Learning Progress"
              color="blue"
            />
            <ProgressRing
              percentage={statistics.masteryPercentage}
              label="Mastery Progress"
              color="green"
            />
          </div>

          {/* Activity Statistics */}
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            <StatCard
              icon="📊"
              label="Total Reviews"
              value={statistics.totalActivities}
              color="bg-indigo"
            />
            <StatCard
              icon="✓"
              label="Correct Answers"
              value={statistics.correctAnswers}
              subtext={
                statistics.totalActivities > 0
                  ? `${(
                      (statistics.correctAnswers / statistics.totalActivities) *
                      100
                    ).toFixed(1)}%`
                  : "0%"
              }
              color="bg-green"
            />
            <StatCard
              icon="📈"
              label="Overall Accuracy"
              value={`${statistics.overallAccuracyRate.toFixed(1)}%`}
              color="bg-blue"
            />
            <StatCard
              icon="🎯"
              label="New This Week"
              value={statistics.newWords}
              color="bg-purple"
            />
          </div>

          {/* Insights */}
          <div className="mt-8 pt-6 border-t border-gray-200">
            <h3 className="text-sm font-bold text-gray-900 uppercase tracking-wide mb-4">
              💡 Insights
            </h3>

            <div className="space-y-3">
              {statistics.wordsNeedingReview > 0 && (
                <div className="p-3 bg-amber-50 rounded-lg border border-amber-200">
                  <p className="text-sm text-amber-900">
                    <span className="font-semibold">⏰ Time to Review:</span> You
                    have {statistics.wordsNeedingReview} word
                    {statistics.wordsNeedingReview !== 1 ? "s" : ""} due for review
                    today!
                  </p>
                </div>
              )}

              {statistics.masteryPercentage < 20 && statistics.totalWords > 0 && (
                <div className="p-3 bg-blue-50 rounded-lg border border-blue-200">
                  <p className="text-sm text-blue-900">
                    <span className="font-semibold">🎓 Keep Going:</span> You
                    {statistics.masteredWords === 0 ? " haven't" : "'ve"} mastered{" "}
                    {statistics.masteredWords === 0 ? "any" : statistics.masteredWords}{" "}
                    word{statistics.masteredWords !== 1 ? "s" : ""} yet. Keep
                    reviewing!
                  </p>
                </div>
              )}

              {statistics.overallAccuracyRate >= 90 && (
                <div className="p-3 bg-green-50 rounded-lg border border-green-200">
                  <p className="text-sm text-green-900">
                    <span className="font-semibold">🌟 Excellent:</span> Your
                    accuracy is outstanding! Keep up the great work!
                  </p>
                </div>
              )}

              {statistics.masteryPercentage >= 50 && (
                <div className="p-3 bg-purple-50 rounded-lg border border-purple-200">
                  <p className="text-sm text-purple-900">
                    <span className="font-semibold">🏆 Milestone:</span> You've
                    mastered over 50% of your learning words. Incredible progress!
                  </p>
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
