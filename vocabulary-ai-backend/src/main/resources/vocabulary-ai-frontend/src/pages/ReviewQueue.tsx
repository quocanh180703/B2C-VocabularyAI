/**
 * Review Queue Page
 * 
 * Displays words that are due for review (Spaced Repetition Queue)
 * Primary page for daily learning sessions
 * 
 * Features:
 * - List of words sorted by due date (due soonest first)
 * - Inline review form for each word (or modal)
 * - Pagination with load more button
 * - Empty state with encouragement message
 * - Loading skeletons
 * - Error handling with retry
 */

import { useState } from "react";
import { useLearning } from "@/hooks";
import LearningCard from "@/components/Learning/LearningCard";
import LearningCardSkeleton from "@/components/Learning/LearningCardSkeleton";
import ReviewForm from "@/components/Learning/ReviewForm";

/**
 * ReviewQueue Page Component
 */
export default function ReviewQueuePage() {
  const {
    reviewQueue,
    reviewQueueLoading,
    reviewQueueError,
    reviewWord,
    reviewingWord,
    loadMoreReviewQueue,
    reviewQueuePage,
    reviewQueueTotalPages,
  } = useLearning();

  // State for expanded review form
  const [expandedWordId, setExpandedWordId] = useState<number | null>(null);

  const handleReviewWord = async (wordId: number, isCorrect: boolean) => {
    await reviewWord(wordId, isCorrect);
    // Auto-collapse form after successful review
    setExpandedWordId(null);
  };

  const hasMore = reviewQueuePage < (reviewQueueTotalPages ?? 0) - 1;

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-2">🔄 Review Queue</h1>
        <p className="text-lg text-gray-600">
          Words scheduled for review based on Spaced Repetition System
        </p>
        {reviewQueue.length > 0 && (
          <p className="text-sm text-gray-500 mt-2">
            {reviewQueue.length} word{reviewQueue.length !== 1 ? "s" : ""} ready to review
          </p>
        )}
      </div>

      {/* Error State */}
      {reviewQueueError && (
        <div className="mb-6 p-4 bg-red-50 border-2 border-red-200 rounded-lg">
          <p className="text-red-900 font-semibold mb-2">❌ Failed to load review queue</p>
          <p className="text-red-700 text-sm mb-4">{reviewQueueError.message}</p>
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors"
          >
            Try Again
          </button>
        </div>
      )}

      {/* Loading State */}
      {reviewQueueLoading && reviewQueue.length === 0 && (
        <LearningCardSkeleton count={3} />
      )}

      {/* Empty State */}
      {!reviewQueueLoading && reviewQueue.length === 0 && !reviewQueueError && (
        <div className="text-center py-12">
          <div className="text-6xl mb-4">🎉</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            All caught up!
          </h2>
          <p className="text-gray-600 mb-6">
            No words need review right now. Come back later to continue learning.
          </p>
          <a
            href="/learning"
            className="inline-block px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
          >
            Start Learning New Words
          </a>
        </div>
      )}

      {/* Review Queue List */}
      {reviewQueue.length > 0 && (
        <div className="space-y-4">
          {reviewQueue.map((word) => (
            <div key={word.id} className="transition-all">
              {expandedWordId === word.id ? (
                // Expanded Review Form
                <div className="bg-white rounded-lg border-2 border-blue-300 shadow-lg p-6">
                  <button
                    onClick={() => setExpandedWordId(null)}
                    className="mb-4 text-gray-600 hover:text-gray-900 text-sm font-medium"
                  >
                    ← Back to list
                  </button>
                  <ReviewForm
                    word={word}
                    onSubmit={handleReviewWord}
                    isLoading={reviewingWord}
                  />
                </div>
              ) : (
                // Compact Card with Click to Review
                <div
                  onClick={() => setExpandedWordId(word.id)}
                  className="cursor-pointer transition-all hover:shadow-lg"
                >
                  <LearningCard
                    word={word}
                    variant="compact"
                    className="hover:border-blue-400"
                  />
                  <p className="text-center text-sm text-blue-600 font-medium mt-2">
                    Click to review →
                  </p>
                </div>
              )}
            </div>
          ))}

          {/* Load More Button */}
          {hasMore && (
            <div className="text-center pt-4">
              <button
                onClick={loadMoreReviewQueue}
                className="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-medium transition-colors"
              >
                Load More ({reviewQueuePage + 1}/{reviewQueueTotalPages})
              </button>
            </div>
          )}
        </div>
      )}

      {/* Tips Section */}
      {reviewQueue.length > 0 && (
        <div className="mt-12 p-6 bg-blue-50 rounded-lg border border-blue-200">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            💡 Spaced Repetition Tips
          </h3>
          <ul className="text-sm text-blue-800 space-y-2">
            <li>• Review words regularly to strengthen long-term memory</li>
            <li>• Words are scheduled 1, 3, 7, 14, 30 days apart</li>
            <li>• Achieving 80% accuracy + 5 reviews = word mastered</li>
            <li>• Incorrect answers reset the review schedule</li>
          </ul>
        </div>
      )}
    </div>
  );
}
