package com.vocabularyai.document;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.document.dto.DocumentResponse;
import com.vocabularyai.document.entity.DocumentFileType;
import com.vocabularyai.document.entity.DocumentStatus;
import com.vocabularyai.document.entity.UserDocument;
import com.vocabularyai.document.repository.UserDocumentRepository;
import com.vocabularyai.document.service.DocumentExtractorService;
import com.vocabularyai.document.service.DocumentServiceImpl;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceUnitTest {

    @Mock
    private UserDocumentRepository userDocumentRepository;

    @Mock
    private DocumentExtractorService documentExtractorService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(10L)
                .username("student1")
                .email("student1@example.com")
                .role(User.UserRole.STUDENT)
                .build();
    }

    @Test
    @DisplayName("Upload file hợp lệ (.txt) lưu DB thành công")
    void testUploadValidDocumentSuccessfully() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "Sample lesson text".getBytes(StandardCharsets.UTF_8)
        );

        when(documentExtractorService.extractText(any(InputStream.class), eq(DocumentFileType.TXT)))
                .thenReturn("Sample lesson text");

        when(userDocumentRepository.save(any(UserDocument.class)))
                .thenAnswer(inv -> {
                    UserDocument doc = inv.getArgument(0);
                    doc.setId(100L);
                    return doc;
                });

        DocumentResponse resp = documentService.uploadDocument(testUser, file);
        assertNotNull(resp);
        assertEquals(100L, resp.getId());
        assertEquals("sample.txt", resp.getOriginalName());
        assertEquals("TXT", resp.getFileType());
        assertEquals("READY", resp.getStatus());
        verify(userDocumentRepository, times(1)).save(any(UserDocument.class));
    }

    @Test
    @DisplayName("Upload file quá 10MB ném BadRequestException")
    void testUploadFileTooLargeThrowsBadRequest() {
        byte[] largeBytes = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.txt",
                "text/plain",
                largeBytes
        );

        assertThrows(BadRequestException.class, () -> documentService.uploadDocument(testUser, file));
        verify(userDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Upload file không đúng định dạng ném BadRequestException")
    void testUploadUnsupportedExtensionThrowsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "malicious content".getBytes()
        );

        assertThrows(BadRequestException.class, () -> documentService.uploadDocument(testUser, file));
        verify(userDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lấy danh sách tài liệu của user phân trang")
    void testGetUserDocuments() {
        UserDocument doc = UserDocument.builder()
                .id(1L)
                .user(testUser)
                .originalName("grammar.pdf")
                .fileType(DocumentFileType.PDF)
                .fileSize(1024L)
                .extractedText("PDF content")
                .charCount(11)
                .status(DocumentStatus.READY)
                .build();

        when(userDocumentRepository.findByUserId(eq(10L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(doc)));

        Page<DocumentResponse> result = documentService.getUserDocuments(10L, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("grammar.pdf", result.getContent().get(0).getOriginalName());
    }

    @Test
    @DisplayName("Xóa tài liệu của chính mình thành công")
    void testDeleteDocument() {
        UserDocument doc = UserDocument.builder()
                .id(1L)
                .user(testUser)
                .originalName("grammar.pdf")
                .storedPath("uploads/documents/10/test.pdf")
                .build();

        when(userDocumentRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(doc));

        documentService.deleteDocument(1L, 10L);
        verify(userDocumentRepository, times(1)).delete(doc);
    }
}
