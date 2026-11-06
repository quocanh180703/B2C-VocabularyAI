package com.vocabularyai.document.service;

import com.vocabularyai.document.dto.DocumentAnalysisResponse;
import com.vocabularyai.document.dto.DocumentChatRequest;
import com.vocabularyai.document.dto.DocumentChatResponse;
import com.vocabularyai.document.dto.GenerateQuizFromDocumentRequest;
import com.vocabularyai.quiz.dto.QuizResponse;

public interface DocumentExerciseAiService {

    /**
     * Preview analysis of an uploaded document using Gemini (does not consume quota).
     */
    DocumentAnalysisResponse analyzeDocument(Long documentId, Long userId);

    /**
     * Conversational refinement for exercise drafting based on document (does not consume quota).
     */
    DocumentChatResponse chatWithDocument(Long documentId, Long userId, DocumentChatRequest request);

    /**
     * Generate quiz questions from document, run self-verification, and persist to database.
     * Consumes 1 quota for EXERCISE_GENERATION (refunded on AI failure).
     */
    QuizResponse generateQuiz(Long documentId, Long userId, GenerateQuizFromDocumentRequest request);
}
