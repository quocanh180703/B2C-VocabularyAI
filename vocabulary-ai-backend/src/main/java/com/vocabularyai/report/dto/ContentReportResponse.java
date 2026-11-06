package com.vocabularyai.report.dto;

import com.vocabularyai.report.entity.ContentReportStatus;
import com.vocabularyai.report.entity.ContentReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReportResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String cefrWordId;
    private String lemmaSnapshot;
    private ContentReportType type;
    private String description;
    private ContentReportStatus status;
    private String adminNote;
    private Long resolvedBy;
    private String resolverName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
