import { useState } from "react";
import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { useCheckGrammar } from "@/hooks/useAI";
import type { GrammarCheckResponse } from "@/services/api/ai.types";

interface GrammarCheckTabProps {
  onCheck?: () => void;
}

export function GrammarCheckTab({ onCheck }: GrammarCheckTabProps) {
  const [textToCheck, setTextToCheck] = useState("I have went to school yesterday.");
  const [result, setResult] = useState<GrammarCheckResponse | null>(null);
  const { mutate, isPending, isError, error } = useCheckGrammar();

  const handleCheck = () => {
    mutate(
      { text: textToCheck },
      {
        onSuccess: (data) => {
          setResult(data);
          onCheck?.();
        },
      }
    );
  };

  const calculateScore = () => {
    if (!result) return 0;
    const errorPenalty = result.errors.length * 10;
    return Math.max(0, 100 - errorPenalty);
  };

  const getScoreBadge = (score: number) => {
    if (score >= 90) return "PERFECT";
    if (score >= 75) return "GOOD";
    if (score >= 60) return "NEEDS A SMALL FIX";
    return "NEEDS IMPROVEMENT";
  };

  const getScoreTone = (score: number) => {
    if (score >= 90) return "success" as const;
    if (score >= 75) return "primary" as const;
    if (score >= 60) return "warning" as const;
    return "critical" as const;
  };

  return (
    <Card className="grammar-card">
      <div>
        <h2>Check your English</h2>
        <p>Write a sentence and get clear, actionable feedback.</p>
        <textarea
          value={textToCheck}
          onChange={(e) => setTextToCheck(e.target.value)}
          aria-label="Sentence to check"
          placeholder="Enter text to check..."
        />
        <Button icon="spark" onClick={handleCheck} disabled={isPending}>
          {isPending ? "Checking..." : "Check grammar"}
        </Button>
        {isError && (
          <div className="error-message">
            <Icon name="alert" />
            <p>Failed to check grammar: {error?.message}</p>
          </div>
        )}
      </div>
      {result && (
        <div className="grammar-result">
          <div className="score-ring">
            <strong>{calculateScore()}</strong>
            <span>/ 100</span>
          </div>
          <Badge tone={getScoreTone(calculateScore())}>{getScoreBadge(calculateScore())}</Badge>
          
          {result.correctedText && (
            <>
              <p className="eyebrow">CORRECTED</p>
              <h3>{result.correctedText}</h3>
            </>
          )}

          {result.errors && result.errors.length > 0 && (
            <>
              <p className="eyebrow">ERRORS FOUND</p>
              <div className="errors-list">
                {result.errors.map((error, idx) => (
                  <div key={idx} className="error-item">
                    <div className="correction">
                      <span>
                        <Icon name="check" /> {error.correction}
                      </span>
                      <span>
                        <Icon name="close" /> {error.original}
                      </span>
                    </div>
                    <p className="error-explanation">{error.explanation}</p>
                  </div>
                ))}
              </div>
            </>
          )}

          {result.suggestions && result.suggestions.length > 0 && (
            <>
              <p className="eyebrow">SUGGESTIONS</p>
              <div className="suggestions-list">
                {result.suggestions.map((suggestion, idx) => (
                  <div key={idx} className="suggestion-item">
                    <strong>{suggestion.title}</strong>
                    <p>{suggestion.content}</p>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      )}
    </Card>
  );
}
