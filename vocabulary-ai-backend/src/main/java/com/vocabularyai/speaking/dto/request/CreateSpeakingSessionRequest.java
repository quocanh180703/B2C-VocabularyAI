package com.vocabularyai.speaking.dto.request;

import com.vocabularyai.speaking.entity.SpeakingLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo một phiên speaking mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSpeakingSessionRequest {
    
    /**
     * Chủ đề luyện nói
     * Ví dụ: "Tự giới thiệu", "Mô tả gia đình"
     */
    @NotBlank(message = "Topic không được để trống")
    @Size(min = 3, max = 255, message = "Topic phải có độ dài từ 3-255 ký tự")
    private String topic;
    
    /**
     * Mức độ khó (A1-C2)
     */
    @NotNull(message = "Level không được để trống")
    private SpeakingLevel level;
    
    /**
     * Mô tả/hướng dẫn cho phiên (optional)
     * Ví dụ: "Mô tả một ngày thông thường của bạn"
     */
    @Size(max = 2000, message = "Description không được vượt quá 2000 ký tự")
    private String description;
}
