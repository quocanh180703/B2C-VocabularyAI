package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicTagQueueDto {

    private Long id;
    private String cefrWordId;
    private String lemma;
    private String suggestedTopic;
    private Double confidence;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
