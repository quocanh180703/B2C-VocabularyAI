package com.vocabularyai.document.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.document.dto.DocumentResponse;
import com.vocabularyai.document.entity.DocumentFileType;
import com.vocabularyai.document.entity.DocumentStatus;
import com.vocabularyai.document.entity.UserDocument;
import com.vocabularyai.document.repository.UserDocumentRepository;
import com.vocabularyai.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final UserDocumentRepository userDocumentRepository;
    private final DocumentExtractorService documentExtractorService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String UPLOAD_DIR = "uploads/documents";

    @Override
    @Transactional
    public DocumentResponse uploadDocument(User user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn tập tin tài liệu.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Kích thước tập tin vượt quá giới hạn cho phép (tối đa 10MB).");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BadRequestException("Tên tập tin không hợp lệ hoặc thiếu phần mở rộng.");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        DocumentFileType fileType;
        try {
            fileType = DocumentFileType.fromExtension(ext);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Định dạng tập tin ." + ext + " không được hỗ trợ. Chỉ chấp nhận .txt, .md, .pdf, .docx.");
        }

        // Save file to disk
        String storedPath;
        try {
            Path userDir = Paths.get(UPLOAD_DIR, String.valueOf(user.getId()));
            Files.createDirectories(userDir);

            String safeName = UUID.randomUUID() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path targetFile = userDir.resolve(safeName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
            storedPath = targetFile.toString();
        } catch (Exception e) {
            log.error("Failed to store file for user {}", user.getId(), e);
            throw new BadRequestException("Không thể lưu tập tin vào hệ thống: " + e.getMessage());
        }

        // Extract text
        String extractedText = null;
        DocumentStatus status = DocumentStatus.READY;
        String errorMessage = null;
        int charCount = 0;

        try (InputStream in = file.getInputStream()) {
            extractedText = documentExtractorService.extractText(in, fileType);
            charCount = extractedText.length();
        } catch (Exception e) {
            log.error("Failed to extract text from file {} for user {}", originalFilename, user.getId(), e);
            status = DocumentStatus.FAILED;
            errorMessage = e.getMessage();
        }

        UserDocument doc = UserDocument.builder()
                .user(user)
                .originalName(originalFilename)
                .storedPath(storedPath)
                .fileType(fileType)
                .fileSize(file.getSize())
                .extractedText(extractedText)
                .charCount(charCount)
                .status(status)
                .errorMessage(errorMessage)
                .build();

        UserDocument saved = userDocumentRepository.save(doc);
        log.info("Uploaded document id={} for user {}, status={}, charCount={}",
                saved.getId(), user.getId(), status, charCount);

        return DocumentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getUserDocuments(Long userId, Pageable pageable) {
        return userDocumentRepository.findByUserId(userId, pageable)
                .map(DocumentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDocument getUserDocumentEntity(Long id, Long userId) {
        return userDocumentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Tài liệu", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id, Long userId) {
        UserDocument doc = getUserDocumentEntity(id, userId);
        return DocumentResponse.fromEntity(doc);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id, Long userId) {
        UserDocument doc = getUserDocumentEntity(id, userId);

        // Delete physical file
        try {
            if (doc.getStoredPath() != null) {
                File file = new File(doc.getStoredPath());
                if (file.exists()) {
                    boolean deleted = file.delete();
                    log.info("Deleted physical file {}: {}", doc.getStoredPath(), deleted);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to delete physical file {}", doc.getStoredPath(), e);
        }

        userDocumentRepository.delete(doc);
        log.info("Deleted user document id={} for user {}", id, userId);
    }
}
