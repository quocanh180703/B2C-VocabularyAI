import { useEffect, useState } from "react";
import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { useExplainWord } from "@/hooks/useAI";
import type { ExplanationResponse } from "@/services/api/ai.types";

interface ExplanationTabProps {
  wordId?: number;  // Changed from: word?: string
  context?: string;
}

export function ExplanationTab({ wordId, context }: ExplanationTabProps) {
  const [explanation, setExplanation] = useState<ExplanationResponse | null>(null);
  const { mutate, isPending, isError, error } = useExplainWord();

  useEffect(() => {
    // Only fetch if wordId is provided
    if (wordId) {
      mutate(
        { wordId, context },
        {
          onSuccess: (data) => {
            setExplanation(data);
          },
        }
      );
    }
  }, [wordId, context, mutate]);

  if (isPending) {
    return (
      <Card className="ai-tool-card">
        <div className="ai-loading">
          <Icon name="spark" />
          <p>Loading explanation...</p>
        </div>
      </Card>
    );
  }

  if (isError) {
    return (
      <Card className="ai-tool-card">
        <div className="ai-error">
          <Icon name="alert" />
          <p>Failed to load explanation</p>
          <p className="error-detail">{error?.message}</p>
        </div>
      </Card>
    );
  }

  if (!explanation) {
    return (
      <Card className="ai-tool-card">
        <div className="ai-empty">
          <p>Select a word to get an AI explanation</p>
        </div>
      </Card>
    );
  }

  return (
    <Card className="ai-tool-card">
      <div className="ai-tool-heading">
        <span className="ai-icon">
          <Icon name="spark" />
        </span>
        <div>
          <Badge tone="primary">AI EXPLANATION</Badge>
          <h2>{explanation.word}</h2>
          <p>{explanation.partOfSpeech}{context ? ` · ${context}` : ""}</p>
        </div>
      </div>
      <div className="ai-content-grid">
        <div>
          <p className="eyebrow">EXPLANATION</p>
          <h3>{explanation.explanation}</h3>
          {explanation.etymology && (
            <>
              <p className="eyebrow">ETYMOLOGY</p>
              <p>{explanation.etymology}</p>
            </>
          )}
          {explanation.examples && explanation.examples.length > 0 && (
            <>
              <p className="eyebrow">EXAMPLES</p>
              <ol>
                {explanation.examples.map((example, idx) => (
                  <li key={idx}>
                    {example}
                  </li>
                ))}
              </ol>
            </>
          )}
        </div>
        <aside>
          {explanation.relatedWords && explanation.relatedWords.length > 0 && (
            <>
              <p className="eyebrow">RELATED WORDS</p>
              <div className="synonyms">
                {explanation.relatedWords.map((relatedWord, idx) => (
                  <Badge key={idx}>{relatedWord}</Badge>
                ))}
              </div>
            </>
          )}
        </aside>
      </div>
    </Card>
  );
}
