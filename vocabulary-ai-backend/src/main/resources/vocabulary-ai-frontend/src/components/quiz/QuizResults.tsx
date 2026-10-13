import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import type { QuizResultsProps } from "@/types/quiz";
import { QUIZ_RESULT_CONFIG, getAccuracyFeedback, formatTimeTaken } from "@/constants/quiz";

export function QuizResults({
  attempt,
  onRetry,
  onExit,
  onReview,
  onRetryQuiz,
}: QuizResultsProps) {
  const isPassed = attempt.score >= QUIZ_RESULT_CONFIG.PASS_SCORE;
  const feedback = getAccuracyFeedback(attempt.score);

  return (
    <div className="session-complete">
      <div className="complete-mark">
        <Icon name={isPassed ? "target" : "close"} size="lg" />
      </div>
      <Badge tone={isPassed ? "success" : "warning"}>
        QUIZ {isPassed ? "PASSED" : "COMPLETED"}
      </Badge>
      <h1>You scored {Math.round(attempt.score)}%</h1>
      <p>
        {attempt.correctAnswers} of {attempt.totalQuestions} answers were correct. {feedback}
      </p>
      <Card className="results-card">
        <div>
          <strong>
            {attempt.correctAnswers}/{attempt.totalQuestions}
          </strong>
          <span>Correct</span>
        </div>
        <div>
          <strong>{formatTimeTaken(attempt.timeTaken)}</strong>
          <span>Time</span>
        </div>
        <div>
          <strong>{Math.round(attempt.score)}%</strong>
          <span>Accuracy</span>
        </div>
      </Card>
      <div className="complete-actions">
        {onReview && (
          <Button variant="secondary" onClick={onReview}>
            Review Answers
          </Button>
        )}
        <Button variant="secondary" onClick={onRetry}>
          Back to quizzes
        </Button>
        <Button onClick={onRetryQuiz || onExit}>
          {onRetryQuiz ? "Try Again" : "Exit"}
        </Button>
      </div>
    </div>
  );
}
