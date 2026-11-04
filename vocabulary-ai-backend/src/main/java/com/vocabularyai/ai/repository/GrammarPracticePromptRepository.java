package com.vocabularyai.ai.repository;

import com.vocabularyai.ai.entity.GrammarPracticePrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrammarPracticePromptRepository extends JpaRepository<GrammarPracticePrompt, Long> {
    List<GrammarPracticePrompt> findByWordIdAndMode(Long wordId, String mode);
    long countByWordIdAndMode(Long wordId, String mode);
}

