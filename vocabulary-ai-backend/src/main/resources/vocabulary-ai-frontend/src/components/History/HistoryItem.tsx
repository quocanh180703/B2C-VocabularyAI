/**
 * HistoryItem Component
 * 
 * Displays a single entry in the Learning History (activity log)
 * Shows:
 * - Activity type (LEARN, REVIEW, QUIZ, SPEAKING)
 * - Word information
 * - Result indicator (✓ correct, ✗ incorrect, or N/A for non-review)
 * - Timestamp
 * - Visual styling based on activity type
 */

import { LearningHistoryResponse } from "@/types/learning";
import { ACTIVITY_TYPE_DISPLAY_MAP } from "@/constants/learning";

interface HistoryItemProps {
  entry: LearningHistoryResponse;
  className?: string;
}

/**
 * Format timestamp to relative time (e.g., "2 hours ago")
 */
function formatRelativeTime(dateString: string): string {
  const now = new Date();
  const date = new Date(dateString);
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (seconds < 60) return "just now";
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`;

  // Fallback to formatted date
  return date.toLocaleDateString();
}

/**
 * Get background color based on activity type and result
 */
function getBackgroundColor(
  activityType: string,
  isCorrect?: boolean
): string {
  if (activityType === "LEARN") {
    return "bg-gradient-to-r from-blue-50 to-indigo-50 border-blue-200";
  }
  if (activityType === "REVIEW") {
    if (isCorrect === true) {
      return "bg-gradient-to-r from-green-50 to-emerald-50 border-green-200";
    }
    if (isCorrect === false) {
      return "bg-gradient-to-r from-red-50 to-rose-50 border-red-200";
    }
  }
  if (activityType === "QUIZ") {
    if (isCorrect === true) {
      return "bg-gradient-to-r from-green-50 to-emerald-50 border-green-200";
    }
    if (isCorrect === false) {
      return "bg-gradient-to-r from-red-50 to-rose-50 border-red-200";
    }
  }
  if (activityType === "SPEAKING") {
    return "bg-gradient-to-r from-purple-50 to-pink-50 border-purple-200";
  }

  return "bg-gray-50 border-gray-200";
}

/**
 * Get text color based on activity type and result
 */
function getTextColor(
  activityType: string,
  isCorrect?: boolean
): string {
  if (activityType === "LEARN") {
    return "text-blue-900";
  }
  if (activityType === "REVIEW") {
    if (isCorrect === true) {
      return "text-green-900";
    }
    if (isCorrect === false) {
      return "text-red-900";
    }
  }
  if (activityType === "QUIZ") {
    if (isCorrect === true) {
      return "text-green-900";
    }
    if (isCorrect === false) {
      return "text-red-900";
    }
  }
  if (activityType === "SPEAKING") {
    return "text-purple-900";
  }

  return "text-gray-900";
}

/**
 * Get result indicator for review activities
 */
function getResultIndicator(activityType: string, isCorrect?: boolean): JSX.Element | null {
  if (activityType === "REVIEW" || activityType === "QUIZ") {
    if (isCorrect === true) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-green-200 text-green-900 text-xs font-semibold">
          ✓ Correct
        </span>
      );
    }
    if (isCorrect === false) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-red-200 text-red-900 text-xs font-semibold">
          ✗ Incorrect
        </span>
      );
    }
  }
  return null;
}

/**
 * HistoryItem Component
 * Displays a single learning history entry
 */
export default function HistoryItem({
  entry,
  className = "",
}: HistoryItemProps) {
  const activityDisplay = ACTIVITY_TYPE_DISPLAY_MAP[entry.activityType];
  const backgroundColor = getBackgroundColor(entry.activityType, entry.isCorrect);
  const textColor = getTextColor(entry.activityType, entry.isCorrect);
  const resultIndicator = getResultIndicator(entry.activityType, entry.isCorrect);

  return (
    <div
      className={`border-2 ${backgroundColor} rounded-lg p-4 transition-all hover:shadow-md ${className}`}
    >
      <div className="flex items-start justify-between gap-4">
        {/* Left: Icon + Activity Type */}
        <div className="flex items-start gap-3 flex-1">
          <span className="text-2xl flex-shrink-0">{activityDisplay.icon}</span>

          <div className="flex-1 min-w-0">
            {/* Activity Type Label */}
            <p className={`text-xs font-bold uppercase tracking-wide ${textColor} opacity-75`}>
              {activityDisplay.label}
            </p>

            {/* Word */}
            <h3 className={`text-lg font-bold ${textColor} mt-1 break-words`}>
              {entry.word.english}
            </h3>

            {/* Definition */}
            <p className={`text-sm ${textColor} opacity-75 mt-1`}>
              {entry.word.vietnamese}
            </p>

            {/* Result Indicator (for review activities) */}
            {resultIndicator && <div className="mt-2">{resultIndicator}</div>}
          </div>
        </div>

        {/* Right: Timestamp */}
        <div className="flex flex-col items-end flex-shrink-0">
          <p className={`text-xs font-medium ${textColor} opacity-75`}>
            {formatRelativeTime(entry.createdAt)}
          </p>
          <p className={`text-xs ${textColor} opacity-50 mt-1`}>
            {new Date(entry.createdAt).toLocaleDateString()}
          </p>
        </div>
      </div>

      {/* Bottom: Activity-specific details */}
      {entry.activityType === "REVIEW" && (
        <div className={`mt-3 text-xs ${textColor} opacity-75 border-t border-current border-opacity-20 pt-2`}>
          <p>
            Part of speech:{" "}
            <span className="font-semibold">{entry.word.partOfSpeech}</span>
          </p>
        </div>
      )}

      {entry.activityType === "SPEAK" && (
        <div className={`mt-3 text-xs ${textColor} opacity-75 border-t border-current border-opacity-20 pt-2`}>
          <p>Proficiency level: <span className="font-semibold">{entry.word.proficiencyLevel || "N/A"}</span></p>
        </div>
      )}
    </div>
  );
}
