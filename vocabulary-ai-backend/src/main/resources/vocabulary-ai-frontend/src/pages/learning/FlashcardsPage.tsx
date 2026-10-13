import { useEffect, useState } from "react";
import { Page } from "@/types";
import {
  Flashcard,
  LearningHeader,
  AnswerZone,
  SessionComplete,
  KeyboardHints,
} from "@/components/learning";
import { MOCK_WORDS } from "@/utils";

interface FlashcardsPageProps {
  onNavigate: (page: Page) => void;
}

export function FlashcardsPage({ onNavigate }: FlashcardsPageProps) {
  const reviewWords = MOCK_WORDS.slice(0, 4);
  const [index, setIndex] = useState(0);
  const [revealed, setRevealed] = useState(false);
  const [feedback, setFeedback] = useState<"known" | "again" | null>(null);
  const [complete, setComplete] = useState(false);
  const [timeElapsed, setTimeElapsed] = useState("06:42");

  const answer = (result: "known" | "again") => {
    if (!revealed || feedback) return;
    setFeedback(result);
    window.setTimeout(() => {
      if (index === reviewWords.length - 1) {
        setComplete(true);
      } else {
        setIndex((current) => current + 1);
        setRevealed(false);
        setFeedback(null);
      }
    }, 700);
  };

  useEffect(() => {
    const keydown = (event: KeyboardEvent) => {
      if (event.code === "Space") {
        event.preventDefault();
        setRevealed(true);
      }
      if (event.key === "ArrowLeft") answer("again");
      if (event.key === "ArrowRight") answer("known");
    };
    window.addEventListener("keydown", keydown);
    return () => window.removeEventListener("keydown", keydown);
  });

  if (complete) {
    return (
      <SessionComplete
        wordsReviewed={12}
        wordsRemembered={9}
        accuracy={75}
        streakDays={7}
        onReviewDifficult={() => {
          setComplete(false);
          setIndex(0);
          setRevealed(false);
        }}
        onViewProgress={() => onNavigate("progress")}
      />
    );
  }

  const word = reviewWords[index];

  return (
    <div className="learn-page">
      <LearningHeader
        current={index}
        total={reviewWords.length}
        timeElapsed={timeElapsed}
        onExit={() => onNavigate("dashboard")}
      />
      <div className="flashcard-wrap">
        <p className="review-label">
          {revealed ? "MEANING REVEALED" : "DO YOU REMEMBER THIS WORD?"}
        </p>
        <Flashcard
          revealed={revealed}
          word={word.word}
          phonetic={word.phonetic}
          level={word.level}
          translation={word.translation}
          meaning={word.meaning}
          example={word.meaning}
          similarWords={["leave", "give up", "desert"]}
          onClick={() => setRevealed(true)}
        />
        <AnswerZone
          revealed={revealed}
          feedback={feedback}
          onKnown={() => answer("known")}
          onAgain={() => answer("again")}
          onReveal={() => setRevealed(true)}
        />
      </div>
      <KeyboardHints reveal={revealed} />
    </div>
  );
}
