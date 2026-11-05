package com.vocabularyai.vocabulary.custom.repository;

import com.vocabularyai.vocabulary.custom.entity.UserCustomWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCustomWordRepository extends JpaRepository<UserCustomWord, Long> {

    boolean existsByUserIdAndLemmaIgnoreCase(Long userId, String lemma);

    Optional<UserCustomWord> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT w FROM UserCustomWord w WHERE w.userId = :userId " +
           "AND (:level IS NULL OR w.cefrLevel = :level) " +
           "AND (:topic IS NULL OR w.topic = :topic) " +
           "AND (:search IS NULL OR LOWER(w.lemma) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(w.vietnameseMeaning) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserCustomWord> searchUserWords(
            @Param("userId") Long userId,
            @Param("level") String level,
            @Param("topic") String topic,
            @Param("search") String search,
            Pageable pageable
    );
}
