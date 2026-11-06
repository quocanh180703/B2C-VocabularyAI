package com.vocabularyai.report.service;

import com.vocabularyai.report.dto.CreateContentReportRequest;
import com.vocabularyai.report.dto.ContentReportResponse;
import com.vocabularyai.report.dto.ResolveContentReportRequest;
import com.vocabularyai.report.entity.ContentReportStatus;
import com.vocabularyai.report.entity.ContentReportType;
import com.vocabularyai.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContentReportService {

    ContentReportResponse createReport(User user, CreateContentReportRequest request);

    Page<ContentReportResponse> listReports(ContentReportStatus status, ContentReportType type, Pageable pageable);

    ContentReportResponse resolveReport(Long reportId, User resolver, ResolveContentReportRequest request);
}
