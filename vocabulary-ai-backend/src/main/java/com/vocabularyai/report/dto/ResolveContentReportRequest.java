package com.vocabularyai.report.dto;

import com.vocabularyai.report.entity.ContentReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveContentReportRequest {

    @NotNull(message = "Trạng thái xử lý không được để trống")
    private ContentReportStatus status;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String adminNote;
}
