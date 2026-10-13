import { useState } from "react";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { QuizHistory, AnswerReview } from "@/components/quiz";
import { useQuiz } from "@/hooks/useQuiz";

type HistoryStage = "list" | "detail";

/**
 * QuizHistoryPage
 * 
 * TASK 9U: Quiz History
 * Displays user's quiz attempt history with pagination and review capability
 */
export function QuizHistoryPage() {
  const [stage, setStage] = useState<HistoryStage>("list");
  const [selectedAttemptId, setSelectedAttemptId] = useState<number | undefined>();
  const [reviewIndex, setReviewIndex] = useState(0);

  const {
    quizHistory,
    historyLoading,
    historyError,
    historyCurrentPage,
    historyTotalPages,
    loadMoreHistory,
    attemptDetails,
    detailsLoading,
  } = useQuiz();

  const handleViewAttempt = (attemptId: number) => {
    setSelectedAttemptId(attemptId);
    setReviewIndex(0);
    setStage("detail");
  };

  const handleBackToList = () => {
    setStage("list");
    setSelectedAttemptId(undefined);
    setReviewIndex(0);
  };

  // ============================================
  // RENDER: DETAIL STAGE (ANSWER REVIEW)
  // ============================================

  if (stage === "detail" && attemptDetails) {
    const currentAnswer = attemptDetails.answers[reviewIndex];
    const totalAnswers = attemptDetails.answers.length;

    return (
      <div className="page">
        <div className="page-heading">
          <button onClick={handleBackToList} style={{ marginBottom: "1rem" }}>
            ← Back to History
          </button>
          <h1>Review Attempt</h1>
        </div>

        <AnswerReview 
          answer={currentAnswer}
          index={reviewIndex}
        />

        <div className="quiz-nav">
          <Button 
            variant="secondary" 
            onClick={() => setReviewIndex(reviewIndex - 1)}
            disabled={reviewIndex === 0}
          >
            Previous
          </Button>
          <span style={{ textAlign: "center", color: "#6b7280" }}>
            {reviewIndex + 1} of {totalAnswers}
          </span>
          <Button 
            onClick={() => setReviewIndex(reviewIndex + 1)}
            disabled={reviewIndex === totalAnswers - 1}
          >
            Next <Icon name="arrow" />
          </Button>
        </div>
      </div>
    );
  }

  // ============================================
  // RENDER: LIST STAGE (HISTORY LIST)
  // ============================================

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">YOUR PROGRESS</p>
          <h1>Quiz History</h1>
          <p>Review your past quiz attempts and track your progress.</p>
        </div>
      </div>

      {historyError && !historyLoading && (
        <div className="error-state">
          <Icon name="close" size="lg" />
          <h2>Failed to load history</h2>
          <p>{historyError.message}</p>
          <Button onClick={() => window.location.reload()}>Retry</Button>
        </div>
      )}

      {!historyError && (
        <QuizHistory
          attempts={quizHistory}
          isLoading={historyLoading}
          hasMore={historyCurrentPage < historyTotalPages - 1}
          onLoadMore={loadMoreHistory}
          onViewAttempt={handleViewAttempt}
        />
      )}
    </div>
  );
}
