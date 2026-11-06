package com.vocabularyai.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageResponse {

    private List<NotificationResponse> content;
    private long unreadCount;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
}

