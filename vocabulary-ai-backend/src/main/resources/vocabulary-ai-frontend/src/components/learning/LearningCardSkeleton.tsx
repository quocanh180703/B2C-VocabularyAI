/**
 * LearningCardSkeleton Component
 * 
 * Skeleton loader / placeholder for LearningCard
 * Shows shimmer animation while card is loading
 * Matches LearningCard dimensions for smooth layout
 * 
 * Used when:
 * - Initial page load (fetching progress)
 * - Pagination load more (fetching next batch)
 * - Any loading state in progress/review queue/history
 */

interface LearningCardSkeletonProps {
  count?: number; // Number of skeleton cards to render
  variant?: "compact" | "detailed";
  className?: string;
}

/**
 * Single Skeleton Card
 */
function SingleSkeleton({ variant = "detailed" }: { variant?: "compact" | "detailed" }) {
  if (variant === "compact") {
    return (
      <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm animate-pulse">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="h-6 bg-gray-200 rounded w-32 mb-2"></div>
            <div className="h-4 bg-gray-200 rounded w-24 mb-3"></div>
          </div>
          <div className="h-6 bg-gray-200 rounded w-20 flex-shrink-0"></div>
        </div>

        <div className="mt-3 space-y-2">
          <div className="h-3 bg-gray-200 rounded"></div>
          <div className="h-2 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  // Detailed variant
  return (
    <div className="rounded-lg border border-gray-200 bg-white shadow-sm animate-pulse overflow-hidden">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 px-6 py-4 border-b border-gray-200">
        <div className="flex items-start justify-between gap-4 mb-2">
          <div className="flex-1">
            <div className="h-8 bg-gray-300 rounded w-40 mb-2"></div>
            <div className="h-4 bg-gray-200 rounded w-32"></div>
          </div>
          <div className="h-10 bg-gray-200 rounded w-24 flex-shrink-0"></div>
        </div>
      </div>

      {/* Content */}
      <div className="px-6 py-4 space-y-4">
        {/* Translation */}
        <div>
          <div className="h-3 bg-gray-200 rounded w-24 mb-2"></div>
          <div className="h-6 bg-gray-200 rounded w-48"></div>
        </div>

        {/* Definition */}
        <div>
          <div className="h-3 bg-gray-200 rounded w-24 mb-2"></div>
          <div className="space-y-2">
            <div className="h-4 bg-gray-200 rounded w-full"></div>
            <div className="h-4 bg-gray-200 rounded w-5/6"></div>
          </div>
        </div>

        {/* Example */}
        <div>
          <div className="h-3 bg-gray-200 rounded w-24 mb-2"></div>
          <div className="space-y-2">
            <div className="h-4 bg-gray-200 rounded w-full"></div>
            <div className="h-4 bg-gray-200 rounded w-3/4"></div>
          </div>
        </div>

        {/* Progress Section */}
        <div className="pt-4 border-t border-gray-200">
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div className="h-12 bg-gray-200 rounded"></div>
            <div className="h-12 bg-gray-200 rounded"></div>
          </div>
          <div className="h-4 bg-gray-200 rounded w-full mb-2"></div>
          <div className="h-3 bg-gray-200 rounded w-full"></div>
        </div>
      </div>

      {/* Buttons */}
      <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 flex gap-3">
        <div className="flex-1 h-10 bg-gray-200 rounded-lg"></div>
        <div className="flex-1 h-10 bg-gray-200 rounded-lg"></div>
      </div>
    </div>
  );
}

/**
 * LearningCardSkeleton Component
 * Renders multiple skeleton cards
 */
export default function LearningCardSkeleton({
  count = 1,
  variant = "detailed",
  className = "",
}: LearningCardSkeletonProps) {
  return (
    <div className={`space-y-4 ${className}`}>
      {[...Array(count)].map((_, i) => (
        <SingleSkeleton key={i} variant={variant} />
      ))}
    </div>
  );
}
