import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Icon } from "@/components/common/Icon";
import type { QuizAnswerDetailResponse } from "@/types/quiz";

interface AnswerReviewProps {
  answer: QuizAnswerDetailResponse;
  index: number;
}

/**
 * AnswerReview Component
 * 
 * Displays a single answer with:
 * - User's selected answer
 * - Correct answer
 * - Explanation
 * - Visual feedback (correct/incorrect)
 */
export function AnswerReview({ answer, index }: AnswerReviewProps) {
  const options = ["A", "B", "C", "D"];
  const selectedLabel = answer.selectedAnswer !== null ? options[answer.selectedAnswer] : "Not answered";
  const correctLabel = options[answer.correctAnswer] || "N/A";

  return (
    <Card className="answer-review">
      <div className="review-header">
        <div className="review-question">
          <Badge tone="neutral">Question {index + 1}</Badge>
          <h3>{answer.content}</h3>
        </div>
        <div className="review-icon">
          <Icon 
            name={answer.isCorrect ? "check" : "close"} 
            size="lg"
            style={{
              color: answer.isCorrect ? "#10b981" : "#ef4444"
            }}
          />
        </div>
      </div>

      <div className="review-answers">
        <div className="answer-item">
          <Badge tone={answer.isCorrect ? "success" : "danger"}>YOUR ANSWER</Badge>
          <p>{selectedLabel}</p>
        </div>
        {!answer.isCorrect && (
          <div className="answer-item">
            <Badge tone="success">CORRECT ANSWER</Badge>
            <p>{correctLabel}</p>
          </div>
        )}
      </div>

      {answer.explanation && (
        <div className="review-explanation">
          <strong>Explanation</strong>
          <p>{answer.explanation}</p>
        </div>
      )}
    </Card>
  );
}
