package com.vocabularyai.document.service;

import com.vocabularyai.document.dto.DocumentResponse;
import com.vocabularyai.document.entity.UserDocument;
import com.vocabularyai.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    DocumentResponse uploadDocument(User user, MultipartFile file);

    Page<DocumentResponse> getUserDocuments(Long userId, Pageable pageable);

    UserDocument getUserDocumentEntity(Long id, Long userId);

    DocumentResponse getDocument(Long id, Long userId);

    void deleteDocument(Long id, Long userId);
}
