package com.vocabularyai.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.document.entity.UserDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponse {

    private Long id;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private Integer charCount;
    private String status;
    private String errorMessage;
    private String previewText;
    private LocalDateTime createdAt;

    public static DocumentResponse fromEntity(UserDocument doc) {
        if (doc == null) return null;
        String preview = null;
        if (doc.getExtractedText() != null && !doc.getExtractedText().isBlank()) {
            preview = doc.getExtractedText().substring(0, Math.min(doc.getExtractedText().length(), 200)) + "...";
        }
        return DocumentResponse.builder()
                .id(doc.getId())
                .originalName(doc.getOriginalName())
                .fileType(doc.getFileType() != null ? doc.getFileType().name() : null)
                .fileSize(doc.getFileSize())
                .charCount(doc.getCharCount())
                .status(doc.getStatus() != null ? doc.getStatus().name() : null)
                .errorMessage(doc.getErrorMessage())
                .previewText(preview)
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
