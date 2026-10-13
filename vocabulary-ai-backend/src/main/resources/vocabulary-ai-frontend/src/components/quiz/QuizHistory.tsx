import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import type { QuizAttemptResponse } from "@/types/quiz";
import { formatTimeTaken, formatAccuracy, QUIZ_RESULT_CONFIG } from "@/constants/quiz";

interface QuizHistoryProps {
  attempts: QuizAttemptResponse[];
  isLoading: boolean;
  hasMore: boolean;
  onLoadMore: () => void;
  onViewAttempt: (attemptId: number) => void;
}

/**
 * QuizHistory Component
 * 
 * Displays user's quiz attempt history with:
 * - Quiz scores
 * - Time taken
 * - Pass/Fail status
 * - Pagination
 */
export function QuizHistory({
  attempts,
  isLoading,
  hasMore,
  onLoadMore,
  onViewAttempt,
}: QuizHistoryProps) {
  if (isLoading && attempts.length === 0) {
    return (
      <div className="history-skeleton">
        {[1, 2, 3].map((i) => (
          <div key={i} className="history-item skeleton" style={{ height: "100px" }} />
        ))}
      </div>
    );
  }

  if (attempts.length === 0) {
    return (
      <div className="empty-state">
        <Icon name="book" size="lg" />
        <h2>No quiz history yet</h2>
        <p>Take a quiz to see your history here.</p>
      </div>
    );
  }

  return (
    <div className="quiz-history">
      <div className="history-list">
        {attempts.map((attempt) => {
          const isPassed = attempt.score >= QUIZ_RESULT_CONFIG.PASS_SCORE;
          const accuracy = formatAccuracy(attempt.correctAnswers, attempt.totalQuestions);
          const date = new Date(attempt.startedAt).toLocaleDateString();

          return (
            <Card key={attempt.id} className="history-item">
              <div className="history-content">
                <div className="history-info">
                  <div className="history-score">
                    <h3>{Math.round(attempt.score)}%</h3>
                    <Badge tone={isPassed ? "success" : "warning"}>
                      {isPassed ? "PASSED" : "COMPLETED"}
                    </Badge>
                  </div>
                  <div className="history-details">
                    <p>
                      <strong>{attempt.correctAnswers}/{attempt.totalQuestions}</strong> correct
                    </p>
                    <p>{formatTimeTaken(attempt.timeTaken)} • {date}</p>
                  </div>
                </div>
                <Button
                  variant="secondary"
                  onClick={() => onViewAttempt(attempt.id)}
                >
                  Review
                </Button>
              </div>
            </Card>
          );
        })}
      </div>

      {hasMore && (
        <div style={{ marginTop: "2rem", textAlign: "center" }}>
          <Button variant="secondary" onClick={onLoadMore} disabled={isLoading}>
            {isLoading ? "Loading..." : "Load More"}
          </Button>
        </div>
      )}
    </div>
  );
}
