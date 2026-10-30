package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVocabTagBackfillStatusResponse {

    private String jobId;
    private String scope;
    private int total;
    private int processed;
    private int autoSaved;
    private int queued;
    private int skipped; // Từ chức năng chủ đích bỏ qua
    private int skippedIntentional; // Từ chung chung hoặc từ chức năng chủ đích bỏ qua
    private int failed;  // Lỗi mạng/AI thực sự
    private String lastError; // Chi tiết lỗi gần nhất để UI hiển thị trực quan
    private String status; // IDLE, RUNNING, COMPLETED, STOPPED, FAILED
    private String message;
}
