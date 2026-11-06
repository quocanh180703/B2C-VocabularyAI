package com.vocabularyai.report.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ConflictException;
import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.report.dto.ContentReportResponse;
import com.vocabularyai.report.dto.CreateContentReportRequest;
import com.vocabularyai.report.dto.ResolveContentReportRequest;
import com.vocabularyai.report.entity.ContentReport;
import com.vocabularyai.report.entity.ContentReportStatus;
import com.vocabularyai.report.entity.ContentReportType;
import com.vocabularyai.report.repository.ContentReportRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentReportServiceImpl implements ContentReportService {

    private final ContentReportRepository contentReportRepository;
    private final CefrWordRepository cefrWordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ContentReportResponse createReport(User user, CreateContentReportRequest request) {
        log.info("Creating content report: userId={}, cefrWordId={}, type={}",
                user.getId(), request.getCefrWordId(), request.getType());

        // 1. Check duplicate pending report for same user and word
        if (contentReportRepository.existsByUserIdAndCefrWordIdAndStatus(
                user.getId(), request.getCefrWordId(), ContentReportStatus.PENDING)) {
            throw new DuplicateResourceException("Bạn đã có một báo cáo đang chờ xử lý cho từ vựng này.");
        }

        // 2. Check maximum pending reports per user (anti-spam, max 10)
        long pendingCount = contentReportRepository.countByUserIdAndStatus(user.getId(), ContentReportStatus.PENDING);
        if (pendingCount >= 10) {
            throw new BadRequestException("Bạn chỉ được gửi tối đa 10 báo cáo đang chờ duyệt cùng lúc.");
        }

        // 3. Capture lemma snapshot
        String lemmaSnapshot = cefrWordRepository.findById(request.getCefrWordId())
                .map(CefrWord::getLemma)
                .orElse(request.getCefrWordId());

        // 4. Save report
        ContentReport report = ContentReport.builder()
                .userId(user.getId())
                .cefrWordId(request.getCefrWordId())
                .lemmaSnapshot(lemmaSnapshot)
                .type(request.getType())
                .description(request.getDescription().trim())
                .status(ContentReportStatus.PENDING)
                .build();

        ContentReport saved = contentReportRepository.save(report);
        return mapToResponse(saved, user, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentReportResponse> listReports(ContentReportStatus status, ContentReportType type, Pageable pageable) {
        Page<ContentReport> page;
        if (status != null && type != null) {
            page = contentReportRepository.findByStatusAndType(status, type, pageable);
        } else if (status != null) {
            page = contentReportRepository.findByStatus(status, pageable);
        } else if (type != null) {
            page = contentReportRepository.findByType(type, pageable);
        } else {
            page = contentReportRepository.findAll(pageable);
        }

        // Collect all userIds and resolverIds
        Set<Long> userIds = page.getContent().stream()
                .map(ContentReport::getUserId)
                .collect(Collectors.toSet());
        Set<Long> resolverIds = page.getContent().stream()
                .map(ContentReport::getResolvedBy)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        userIds.addAll(resolverIds);

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return page.map(report -> {
            User reporter = userMap.get(report.getUserId());
            User resolver = report.getResolvedBy() != null ? userMap.get(report.getResolvedBy()) : null;
            return mapToResponse(report, reporter, resolver);
        });
    }

    @Override
    @Transactional
    public ContentReportResponse resolveReport(Long reportId, User resolver, ResolveContentReportRequest request) {
        log.info("Resolving content report: reportId={}, status={}, resolvedBy={}",
                reportId, request.getStatus(), resolver.getId());

        ContentReport report = contentReportRepository.findById(reportId)
                .orElseThrow(() -> ResourceNotFoundException.of("Báo cáo nội dung", "id", reportId));

        if (report.getStatus() != ContentReportStatus.PENDING) {
            throw new ConflictException("Báo cáo này đã được xử lý trước đó");
        }
        if (request.getStatus() == ContentReportStatus.PENDING) {
            throw new BadRequestException("Không thể đặt lại trạng thái về PENDING");
        }
        if (request.getStatus() == ContentReportStatus.REJECTED && (request.getAdminNote() == null || request.getAdminNote().trim().isBlank())) {
            throw new BadRequestException("Lý do từ chối (adminNote) không được để trống khi từ chối báo cáo");
        }

        report.setStatus(request.getStatus());
        if (request.getAdminNote() != null) {
            report.setAdminNote(request.getAdminNote().trim());
        }
        report.setResolvedBy(resolver.getId());
        report.setResolvedAt(LocalDateTime.now());

        ContentReport saved = contentReportRepository.save(report);

        User reporter = userRepository.findById(saved.getUserId()).orElse(null);
        return mapToResponse(saved, reporter, resolver);
    }

    private ContentReportResponse mapToResponse(ContentReport report, User reporter, User resolver) {
        return ContentReportResponse.builder()
                .id(report.getId())
                .userId(report.getUserId())
                .userFullName(reporter != null ? reporter.getFullName() : null)
                .userEmail(reporter != null ? reporter.getEmail() : null)
                .cefrWordId(report.getCefrWordId())
                .lemmaSnapshot(report.getLemmaSnapshot())
                .type(report.getType())
                .description(report.getDescription())
                .status(report.getStatus())
                .adminNote(report.getAdminNote())
                .resolvedBy(report.getResolvedBy())
                .resolverName(resolver != null ? (resolver.getFullName() != null ? resolver.getFullName() : resolver.getUsername()) : null)
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
