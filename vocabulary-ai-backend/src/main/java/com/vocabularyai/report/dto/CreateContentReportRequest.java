package com.vocabularyai.report.dto;

import com.vocabularyai.report.entity.ContentReportType;
import jakarta.validation.constraints.NotBlank;
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
public class CreateContentReportRequest {

    @NotBlank(message = "ID từ vựng không được để trống")
    @Size(max = 100, message = "ID từ vựng tối đa 100 ký tự")
    private String cefrWordId;

    @NotNull(message = "Loại lỗi không được để trống")
    private ContentReportType type;

    @NotBlank(message = "Mô tả lỗi không được để trống")
    @Size(max = 500, message = "Mô tả lỗi tối đa 500 ký tự")
    private String description;
}
