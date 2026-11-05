package com.vocabularyai.report;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.report.dto.ContentReportResponse;
import com.vocabularyai.report.dto.CreateContentReportRequest;
import com.vocabularyai.report.dto.ResolveContentReportRequest;
import com.vocabularyai.report.entity.ContentReport;
import com.vocabularyai.report.entity.ContentReportStatus;
import com.vocabularyai.report.entity.ContentReportType;
import com.vocabularyai.report.repository.ContentReportRepository;
import com.vocabularyai.report.service.ContentReportServiceImpl;
import com.vocabularyai.user.entity.User.UserRole;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminContentReportUnitTest {

    @Mock
    private ContentReportRepository contentReportRepository;

    @Mock
    private CefrWordRepository cefrWordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContentReportServiceImpl contentReportService;

    private User normalUser;
    private User instructorUser;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .id(1L)
                .username("student1")
                .email("student1@example.com")
                .fullName("Student One")
                .role(UserRole.STUDENT)
                .build();

        instructorUser = User.builder()
                .id(2L)
                .username("instructor1")
                .email("instructor1@example.com")
                .fullName("Instructor One")
                .role(UserRole.INSTRUCTOR)
                .build();
    }

    @Test
    @DisplayName("Create content report successfully and capture lemma snapshot")
    void testCreateReport_Success() {
        CreateContentReportRequest request = CreateContentReportRequest.builder()
                .cefrWordId("word-uuid-123")
                .type(ContentReportType.WRONG_MEANING)
                .description("Nghĩa tiếng Việt bị dịch sai ngữ cảnh")
                .build();

        when(contentReportRepository.existsByUserIdAndCefrWordIdAndStatus(1L, "word-uuid-123", ContentReportStatus.PENDING))
                .thenReturn(false);
        when(contentReportRepository.countByUserIdAndStatus(1L, ContentReportStatus.PENDING))
                .thenReturn(2L);
        when(cefrWordRepository.findById("word-uuid-123"))
                .thenReturn(Optional.of(CefrWord.builder().id("word-uuid-123").lemma("abandon").build()));
        when(contentReportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> {
            ContentReport report = invocation.getArgument(0);
            report.setId(10L);
            return report;
        });

        ContentReportResponse response = contentReportService.createReport(normalUser, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("abandon", response.getLemmaSnapshot());
        assertEquals(ContentReportType.WRONG_MEANING, response.getType());
        assertEquals(ContentReportStatus.PENDING, response.getStatus());
        verify(contentReportRepository, times(1)).save(any(ContentReport.class));
    }

    @Test
    @DisplayName("Create content report throws DuplicateResourceException when same pending report exists")
    void testCreateReport_DuplicatePending_ThrowsDuplicate() {
        CreateContentReportRequest request = CreateContentReportRequest.builder()
                .cefrWordId("word-uuid-123")
                .type(ContentReportType.WRONG_EXAMPLE)
                .description("Ví dụ lỗi")
                .build();

        when(contentReportRepository.existsByUserIdAndCefrWordIdAndStatus(1L, "word-uuid-123", ContentReportStatus.PENDING))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> contentReportService.createReport(normalUser, request));
        verify(contentReportRepository, never()).save(any(ContentReport.class));
    }

    @Test
    @DisplayName("Create content report throws BadRequestException when user has >= 10 pending reports")
    void testCreateReport_ExceedPendingLimit_ThrowsBadRequest() {
        CreateContentReportRequest request = CreateContentReportRequest.builder()
                .cefrWordId("word-uuid-456")
                .type(ContentReportType.WRONG_PRONUNCIATION)
                .description("Phát âm lỗi")
                .build();

        when(contentReportRepository.existsByUserIdAndCefrWordIdAndStatus(1L, "word-uuid-456", ContentReportStatus.PENDING))
                .thenReturn(false);
        when(contentReportRepository.countByUserIdAndStatus(1L, ContentReportStatus.PENDING))
                .thenReturn(10L);

        assertThrows(BadRequestException.class, () -> contentReportService.createReport(normalUser, request));
        verify(contentReportRepository, never()).save(any(ContentReport.class));
    }

    @Test
    @DisplayName("Resolve report sets status, note and resolver info")
    void testResolveReport_Success() {
        ContentReport existing = ContentReport.builder()
                .id(5L)
                .userId(1L)
                .cefrWordId("word-1")
                .lemmaSnapshot("cat")
                .type(ContentReportType.WRONG_LEVEL)
                .description("Từ này nên là A1")
                .status(ContentReportStatus.PENDING)
                .build();

        when(contentReportRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(contentReportRepository.save(any(ContentReport.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        ResolveContentReportRequest resolveReq = ResolveContentReportRequest.builder()
                .status(ContentReportStatus.APPROVED)
                .adminNote("Đã xác nhận lỗi level, chuẩn bị cập nhật")
                .build();

        ContentReportResponse response = contentReportService.resolveReport(5L, instructorUser, resolveReq);

        assertNotNull(response);
        assertEquals(ContentReportStatus.APPROVED, response.getStatus());
        assertEquals("Đã xác nhận lỗi level, chuẩn bị cập nhật", response.getAdminNote());
        assertEquals(2L, response.getResolvedBy());
        assertEquals("Instructor One", response.getResolverName());
    }

    @Test
    @DisplayName("Resolve non-existent report throws ResourceNotFoundException")
    void testResolveReport_NotFound() {
        when(contentReportRepository.findById(999L)).thenReturn(Optional.empty());

        ResolveContentReportRequest resolveReq = ResolveContentReportRequest.builder()
                .status(ContentReportStatus.REJECTED)
                .adminNote("Không hợp lệ")
                .build();

        assertThrows(ResourceNotFoundException.class, () ->
                contentReportService.resolveReport(999L, instructorUser, resolveReq));
    }
}
