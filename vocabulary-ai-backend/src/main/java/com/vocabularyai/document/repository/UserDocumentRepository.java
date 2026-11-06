package com.vocabularyai.document.repository;

import com.vocabularyai.document.entity.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {

    Page<UserDocument> findByUserId(Long userId, Pageable pageable);

    Optional<UserDocument> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
