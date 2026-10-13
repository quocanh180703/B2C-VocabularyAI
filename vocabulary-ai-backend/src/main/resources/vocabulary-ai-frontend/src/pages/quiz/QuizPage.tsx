import { useState, useEffect, useRef } from "react";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { QuizCard, QuestionCard, QuizResults, AnswerReview } from "@/components/quiz";
import { useQuiz } from "@/hooks/useQuiz";
import { formatTimeLimit, QUIZ_LEVEL_DISPLAY_MAP, QUIZ_EMPTY_MESSAGES } from "@/constants/quiz";
import type { QuizResponse, QuizAttemptResponse, QuizWithQuestionsResponse, SubmitQuizRequest, QuizAttemptWithAnswersResponse } from "@/types/quiz";

type QuizStage = "list" | "detail" | "active" | "result" | "review";

export function QuizPage() {
  const [stage, setStage] = useState<QuizStage>("list");
  const [selectedQuizId, setSelectedQuizId] = useState<number | undefined>();
  const [quizDetail, setQuizDetail] = useState<QuizWithQuestionsResponse | undefined>();
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answer, setAnswer] = useState<number | null>(null);
  const [currentAttemptId, setCurrentAttemptId] = useState<number | undefined>();
  const [currentAttempt, setCurrentAttempt] = useState<QuizAttemptResponse | undefined>();
  const [selectedAnswers, setSelectedAnswers] = useState<Map<number, number | null>>(new Map());
  const [attemptWithAnswers, setAttemptWithAnswers] = useState<QuizAttemptWithAnswersResponse | undefined>();
  
  // Duplicate submission prevention (TASK 9R)
  const isSubmittingRef = useRef(false);

  // ============================================
  // HOOKS
  // ============================================

  const {
    quizzes,
    quizzesLoading,
    quizzesError,
    currentQuiz,
    quizLoading,
    quizError,
    startQuiz: startQuizMutation,
    isStartingQuiz,
    submitQuiz: submitQuizMutation,
    isSubmittingQuiz,
  } = useQuiz();

  // ============================================
  // EFFECTS
  // ============================================

  /**
   * Fetch quiz detail when selectedQuizId changes
   * Simulates useQuery with manual fetch
   */
  useEffect(() => {
    if (!selectedQuizId) return;

    const fetchQuizDetail = async () => {
      try {
        const response = await fetch(`/api/quizzes/${selectedQuizId}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('access_token')}`,
          },
        });
        const data = await response.json();
        setQuizDetail(data.data);
      } catch (error) {
        console.error("Error fetching quiz detail:", error);
      }
    };

    fetchQuizDetail();
  }, [selectedQuizId]);

  /**
   * Fetch attempt details with answers for review
   * TASK 9T: Answer Review - Fetch answers with correctAnswer and explanation
   */
  useEffect(() => {
    if (stage !== "result" || !currentAttemptId) return;

    const fetchAttemptDetails = async () => {
      try {
        const response = await fetch(`/api/quizzes/attempts/${currentAttemptId}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('access_token')}`,
          },
        });
        const data = await response.json();
        setAttemptWithAnswers(data.data);
      } catch (error) {
        console.error("Error fetching attempt details:", error);
      }
    };

    fetchAttemptDetails();
  }, [stage, currentAttemptId]);

  // ============================================
  // EVENT HANDLERS
  // ============================================

  const handleViewQuizDetail = (quizId: number) => {
    setSelectedQuizId(quizId);
    setStage("detail");
    setCurrentQuestionIndex(0);
    setAnswer(null);
    setSelectedAnswers(new Map());
  };

  const handleBackToList = () => {
    setStage("list");
    setSelectedQuizId(undefined);
    setQuizDetail(undefined);
  };

  const handleStartQuiz = async () => {
    if (!selectedQuizId) return;

    try {
      const attempt = await startQuizMutation(selectedQuizId);
      setCurrentAttempt(attempt);
      setCurrentAttemptId(attempt.id);
      setStage("active");
      setCurrentQuestionIndex(0);
      setAnswer(null);
      setSelectedAnswers(new Map());
    } catch (error) {
      console.error("Error starting quiz:", error);
      alert("Failed to start quiz. Please try again.");
    }
  };

  const handleSelectAnswer = (index: number) => {
    setAnswer(index);
    
    // TASK 9L: Store answer in map for persistence across navigation
    if (quizDetail && currentQuestionIndex < quizDetail.questions.length) {
      const questionId = quizDetail.questions[currentQuestionIndex].id;
      const newAnswers = new Map(selectedAnswers);
      newAnswers.set(questionId, index);
      setSelectedAnswers(newAnswers);
    }
  };

  const handleNextQuestion = () => {
    if (!quizDetail) return;
    
    if (currentQuestionIndex < quizDetail.questions.length - 1) {
      // Navigate to next question
      setCurrentQuestionIndex(currentQuestionIndex + 1);
      
      // Restore previous answer if it exists (TASK 9L: Answer persistence)
      const nextQuestion = quizDetail.questions[currentQuestionIndex + 1];
      const previousAnswer = selectedAnswers.get(nextQuestion.id) ?? null;
      setAnswer(previousAnswer);
    } else {
      // Last question - submit quiz
      handleSubmitQuiz();
    }
  };

  const handlePreviousQuestion = () => {
    if (currentQuestionIndex > 0) {
      // Navigate to previous question
      setCurrentQuestionIndex(currentQuestionIndex - 1);
      
      // Restore previous answer if it exists (TASK 9L: Answer persistence)
      const previousQuestion = quizDetail?.questions[currentQuestionIndex - 1];
      if (previousQuestion) {
        const previousAnswer = selectedAnswers.get(previousQuestion.id) ?? null;
        setAnswer(previousAnswer);
      }
    }
  };

  // TASK 9Q: Submit Quiz - collect all answers and call backend
  const handleSubmitQuiz = async () => {
    if (!currentAttemptId || !quizDetail) return;
    
    // TASK 9R: Duplicate Protection - Layer 1: useRef lock
    if (isSubmittingRef.current || isSubmittingQuiz) return;
    isSubmittingRef.current = true;

    try {
      // Convert selectedAnswers Map to SubmitQuizRequest format
      const answers: SubmitQuizRequest = {
        answers: quizDetail.questions.map((question) => ({
          questionId: question.id,
          answer: selectedAnswers.get(question.id) ?? null,
        })),
      };

      // Call backend mutation
      const result = await submitQuizMutation(currentAttemptId, answers);
      
      // Transition to result stage
      setCurrentAttempt(result);
      setStage("result");
    } catch (error) {
      console.error("Error submitting quiz:", error);
      alert("Failed to submit quiz. Please try again.");
    } finally {
      isSubmittingRef.current = false;
    }
  };

  const handleRetry = () => {
    setStage("list");
    setAnswer(null);
    setSelectedQuizId(undefined);
    setQuizDetail(undefined);
    setCurrentAttemptId(undefined);
    setCurrentAttempt(undefined);
    setAttemptWithAnswers(undefined);
    setSelectedAnswers(new Map());
    setCurrentQuestionIndex(0);
  };

  const handleExit = () => {
    setStage("list");
    setAnswer(null);
    setSelectedQuizId(undefined);
    setQuizDetail(undefined);
    setCurrentAttemptId(undefined);
    setCurrentAttempt(undefined);
    setAttemptWithAnswers(undefined);
    setSelectedAnswers(new Map());
    setCurrentQuestionIndex(0);
  };

  // TASK 9W: Retry Quiz - Start same quiz again with fresh attempt
  const handleRetryQuiz = async () => {
    if (!selectedQuizId) return;
    
    try {
      const attempt = await startQuizMutation(selectedQuizId);
      setCurrentAttempt(attempt);
      setCurrentAttemptId(attempt.id);
      setStage("active");
      setCurrentQuestionIndex(0);
      setAnswer(null);
      setSelectedAnswers(new Map());
    } catch (error) {
      console.error("Error retrying quiz:", error);
      alert("Failed to start quiz. Please try again.");
    }
  };

  // TASK 9T: View Answer Review
  const handleViewAnswers = () => {
    setStage("review");
    setCurrentQuestionIndex(0);
  };

  // ============================================
  // RENDER: REVIEW STAGE (ANSWER REVIEW)
  // ============================================

  if (stage === "review" && attemptWithAnswers) {
    const currentAnswer = attemptWithAnswers.answers[currentQuestionIndex];
    const totalAnswers = attemptWithAnswers.answers.length;

    return (
      <div className="page">
        <div className="page-heading">
          <button onClick={() => setStage("result")} style={{ marginBottom: "1rem" }}>
            ← Back to Results
          </button>
          <h1>Review Your Answers</h1>
        </div>

        <AnswerReview 
          answer={currentAnswer}
          index={currentQuestionIndex}
        />

        <div className="quiz-nav">
          <Button 
            variant="secondary" 
            onClick={() => setCurrentQuestionIndex(currentQuestionIndex - 1)}
            disabled={currentQuestionIndex === 0}
          >
            Previous
          </Button>
          <span style={{ textAlign: "center", color: "#6b7280" }}>
            {currentQuestionIndex + 1} of {totalAnswers}
          </span>
          <Button 
            onClick={() => setCurrentQuestionIndex(currentQuestionIndex + 1)}
            disabled={currentQuestionIndex === totalAnswers - 1}
          >
            Next <Icon name="arrow" />
          </Button>
        </div>
      </div>
    );
  }

  // ============================================
  // RENDER: RESULT STAGE
  // ============================================

  if (stage === "result" && currentAttempt) {
    return (
      <QuizResults
        attempt={currentAttempt}
        onRetry={handleRetry}
        onExit={handleExit}
        onReview={attemptWithAnswers ? handleViewAnswers : undefined}
        onRetryQuiz={handleRetryQuiz}
      />
    );
  }

  // ============================================
  // RENDER: ACTIVE STAGE (TAKING QUIZ)
  // ============================================

  if (stage === "active" && quizDetail && currentAttempt) {
    const currentQuestion = quizDetail.questions[currentQuestionIndex];
    const totalQuestions = quizDetail.questions.length;
    const isLastQuestion = currentQuestionIndex === totalQuestions - 1;

    return (
      <div className="page quiz-focus">
        <QuestionCard
          current={currentQuestionIndex + 1}
          total={totalQuestions}
          question={currentQuestion.content}
          options={currentQuestion.options}
          selectedAnswer={answer}
          onSelectAnswer={handleSelectAnswer}
        />
        <div className="quiz-nav">
          <Button 
            variant="secondary" 
            onClick={handlePreviousQuestion}
            disabled={currentQuestionIndex === 0}
          >
            Previous
          </Button>
          <Button
            onClick={isLastQuestion ? handleSubmitQuiz : handleNextQuestion}
            disabled={answer === null || isStartingQuiz || isSubmittingQuiz}
          >
            {isLastQuestion ? "Submit Quiz" : "Next question"} <Icon name="arrow" />
          </Button>
        </div>
      </div>
    );
  }

  // ============================================
  // RENDER: DETAIL STAGE (QUIZ PREVIEW)
  // ============================================

  if (stage === "detail" && quizDetail) {
    const quiz = quizDetail.quiz;
    return (
      <div className="page">
        <div className="page-heading">
          <button onClick={handleBackToList} style={{ marginBottom: "1rem" }}>
            ← Back
          </button>
          <div>
            <p className="eyebrow">{quiz.level ? QUIZ_LEVEL_DISPLAY_MAP[quiz.level] : "Quiz"}</p>
            <h1>{quiz.title}</h1>
            <p>{quiz.description}</p>
          </div>
        </div>

        <div className="quiz-detail-info">
          <div className="info-item">
            <Icon name="quiz" />
            <span>{quizDetail.questions.length} Questions</span>
          </div>
          <div className="info-item">
            <Icon name="clock" />
            <span>{formatTimeLimit(quiz.timeLimit)}</span>
          </div>
        </div>

        <Button onClick={handleStartQuiz} disabled={isStartingQuiz} style={{ width: "100%", marginTop: "2rem" }}>
          {isStartingQuiz ? "Starting..." : "Start Quiz"}
        </Button>
      </div>
    );
  }

  // ============================================
  // RENDER: LIST STAGE (QUIZ LIST)
  // ============================================

  return (
    <div className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">CHECK YOUR KNOWLEDGE</p>
          <h1>Quizzes</h1>
          <p>Short, focused challenges based on what you've learned.</p>
        </div>
      </div>

      {/* Loading State */}
      {quizzesLoading && (
        <div className="quiz-grid">
          {[1, 2, 3].map((i) => (
            <div key={i} className="quiz-card skeleton">
              <div style={{ height: "200px", background: "#f0f0f0" }} />
            </div>
          ))}
        </div>
      )}

      {/* Error State */}
      {quizzesError && !quizzesLoading && (
        <div className="error-state">
          <Icon name="close" size="lg" />
          <h2>Failed to load quizzes</h2>
          <p>{quizzesError.message}</p>
          <Button onClick={() => window.location.reload()}>Retry</Button>
        </div>
      )}

      {/* Empty State */}
      {!quizzesLoading && !quizzesError && quizzes.length === 0 && (
        <div className="empty-state">
          <Icon name="quiz" size="lg" />
          <h2>No quizzes available</h2>
          <p>{QUIZ_EMPTY_MESSAGES.NO_QUIZZES}</p>
        </div>
      )}

      {/* Quiz List */}
      {!quizzesLoading && !quizzesError && quizzes.length > 0 && (
        <div className="quiz-grid">
          {quizzes.map((quiz, index) => (
            <div 
              key={quiz.id}
              onClick={() => handleViewQuizDetail(quiz.id)}
              style={{ cursor: "pointer" }}
            >
              <QuizCard
                title={quiz.title}
                tag={quiz.level ? QUIZ_LEVEL_DISPLAY_MAP[quiz.level] : "Quiz"}
                questions={`${quizzes.length || 0} Questions`}
                duration={formatTimeLimit(quiz.timeLimit)}
                icon="book"
                isPrimary={index === 0}
                onStart={(e) => {
                  e?.stopPropagation?.();
                  handleViewQuizDetail(quiz.id);
                }}
              />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
