import { useState, useEffect } from "react";
import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { Skeleton } from "@/components/common/Skeleton";
import { ErrorState } from "@/components/common/ErrorState";
import * as vocabularyService from "@/services/vocabularyService";
import { CefrWordResponse, ExplanationResponse } from "@/services/api/types";

interface WordDetailPageProps {
  wordId: string;
  onBack: () => void;
}

/**
 * WordDetailPage Component
 * 
 * Features:
 * - Fetch word details from backend by ID
 * - Fetch AI explanation for the word
 * - Display favorite status and allow toggle
 * - Display learning status
 * - "Start Learning" button integration
 * - Loading/error states
 */
export function WordDetailPage({ wordId, onBack }: WordDetailPageProps) {
  const [word, setWord] = useState<CefrWordResponse | null>(null);
  const [explanation, setExplanation] = useState<ExplanationResponse | null>(null);
  const [isFavorite, setIsFavorite] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [toggleFavLoading, setToggleFavLoading] = useState(false);

  /**
   * Load word detail and explanation data
   */
  const loadWordData = async () => {
    try {
      setLoading(true);
      setError(null);

      console.log(`📖 Loading word detail: ${wordId}`);

      const detailData = await vocabularyService.getVocabularyDetailData(wordId);
      
      setWord(detailData.word);
      setExplanation(detailData.explanation);
      setIsFavorite(detailData.isFavorite);

      console.log(`✅ Loaded word: ${detailData.word.lemma}`);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load word details';
      const detailError = new Error(errorMessage);
      setError(detailError);
      console.error('Word detail load error:', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Toggle favorite status
   */
  const handleToggleFavorite = async () => {
    if (!word) return;

    try {
      setToggleFavLoading(true);
      const wordNumId = parseInt(word.id) || 0;

      if (isFavorite) {
        await vocabularyService.removeFromFavorites(wordNumId);
        console.log(`⭐ Removed from favorites: ${word.lemma}`);
      } else {
        await vocabularyService.addToFavorites(wordNumId);
        console.log(`⭐ Added to favorites: ${word.lemma}`);
      }

      setIsFavorite(!isFavorite);
    } catch (err) {
      console.error('Failed to toggle favorite:', err);
      alert('Failed to update favorites. Please try again.');
    } finally {
      setToggleFavLoading(false);
    }
  };

  /**
   * Start learning this word
   */
  const handleStartLearning = async () => {
    if (!word) return;

    try {
      const wordNumId = parseInt(word.id) || 0;
      console.log(`📚 Starting to learn: ${word.lemma}`);
      
      await vocabularyService.startLearningWord(wordNumId);
      alert('Added to your learning list!');
    } catch (err) {
      console.error('Failed to start learning:', err);
      alert('Failed to add to learning list. Please try again.');
    }
  };

  /**
   * Load data on mount
   */
  useEffect(() => {
    loadWordData();
  }, [wordId]);

  // Render loading state
  if (loading) {
    return (
      <div className="page narrow-page">
        <Button variant="ghost" onClick={onBack}>
          ← Back to vocabulary
        </Button>
        <Card className="detail-card">
          <div className="detail-hero">
            <div>
              <div className="detail-badges">
                <Skeleton width="50px" height="20px" />
                <Skeleton width="80px" height="20px" />
              </div>
              <h1 style={{ marginTop: "12px", marginBottom: "8px" }}>
                <Skeleton width="200px" height="32px" />
              </h1>
              <p className="phonetic large">
                <Skeleton width="150px" height="16px" />
              </p>
            </div>
            <Skeleton width="100px" height="36px" />
          </div>
          <div className="detail-grid">
            <div className="detail-main">
              <div className="detail-section">
                <Skeleton width="80px" height="16px" style={{ marginBottom: "12px" }} />
                <Skeleton width="100%" height="60px" />
              </div>
            </div>
            <aside className="ai-explanation">
              <Skeleton width="150px" height="16px" style={{ marginBottom: "12px" }} />
              <Skeleton width="100%" height="80px" />
            </aside>
          </div>
        </Card>
      </div>
    );
  }

  // Render error state
  if (error || !word) {
    return (
      <ErrorState
        title="Unable to load word details"
        error={error}
        onRetry={loadWordData}
      />
    );
  }

  // Render word detail
  return (
    <div className="page narrow-page">
      <Button variant="ghost" onClick={onBack}>
        ← Back to vocabulary
      </Button>
      <Card className="detail-card">
        <div className="detail-hero">
          <div>
            <div className="detail-badges">
              <Badge tone="primary">{word.cefrLevel}</Badge>
              <Badge>{word.partOfSpeech}</Badge>
            </div>
            <h1>{word.lemma}</h1>
            <p className="phonetic large">{word.pronunciation}</p>
          </div>
          <Button variant="secondary" icon="volume">
            Listen
          </Button>
        </div>
        <div className="detail-grid">
          <div className="detail-main">
            <div className="detail-section">
              <p className="eyebrow">DEFINITION</p>
              <h2>{word.definition}</h2>
              <p className="translation">
                Source: <strong>{word.source}</strong>
              </p>
            </div>

            {explanation && (
              <>
                <div className="detail-section">
                  <p className="eyebrow">EXAMPLES</p>
                  {explanation.examples && explanation.examples.length > 0 ? (
                    <div>
                      {explanation.examples.map((example, idx) => (
                        <blockquote key={idx} style={{ marginBottom: "12px" }}>
                          "{example}"
                        </blockquote>
                      ))}
                    </div>
                  ) : (
                    <p style={{ fontStyle: "italic", color: "#999" }}>
                      No examples available
                    </p>
                  )}
                </div>

                {explanation.relatedWords && explanation.relatedWords.length > 0 && (
                  <div className="detail-section">
                    <p className="eyebrow">RELATED WORDS</p>
                    <div className="synonyms">
                      {explanation.relatedWords.map((relatedWord) => (
                        <Badge key={relatedWord}>{relatedWord}</Badge>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}
          </div>

          <aside className="ai-explanation">
            <span className="ai-icon">
              <Icon name="spark" />
            </span>
            <p className="eyebrow">AI EXPLANATION</p>
            <h3>Understand it naturally</h3>

            {explanation ? (
              <>
                <p>{explanation.explanation}</p>
                {explanation.etymology && (
                  <div className="usage-tip">
                    <strong>Etymology</strong>
                    <p>{explanation.etymology}</p>
                  </div>
                )}
              </>
            ) : (
              <p style={{ fontStyle: "italic", color: "#999" }}>
                Loading AI explanation...
              </p>
            )}
          </aside>
        </div>

        <div className="detail-actions">
          <Button 
            variant="secondary" 
            icon="star"
            onClick={handleToggleFavorite}
            disabled={toggleFavLoading}
          >
            {isFavorite ? "Remove from favorites" : "Add to favorites"}
          </Button>
          <Button 
            onClick={handleStartLearning} 
            icon="cards"
          >
            Start learning
          </Button>
        </div>
      </Card>
    </div>
  );
}
