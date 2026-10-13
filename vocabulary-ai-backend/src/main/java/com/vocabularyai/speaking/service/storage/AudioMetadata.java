package com.vocabularyai.speaking.service.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AudioMetadata
 * 
 * Chứa thông tin metadata về audio file được upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioMetadata {
    
    /**
     * URL hoặc path của audio file sau khi upload
     * - Local: /storage/audio/session-123/attempt-1-1234567890.wav
     * - Cloudinary: https://res.cloudinary.com/demo/video/upload/v1234567890/audio.wav
     * - S3: https://bucket.s3.amazonaws.com/audio/session-123/attempt-1.wav
     */
    @JsonProperty("audioUrl")
    private String audioUrl;
    
    /**
     * Tên file gốc được upload
     */
    @JsonProperty("originalFilename")
    private String originalFilename;
    
    /**
     * Kích thước file (bytes)
     */
    @JsonProperty("fileSize")
    private Long fileSize;
    
    /**
     * MIME type của file
     */
    @JsonProperty("mimeType")
    private String mimeType;
    
    /**
     * Storage provider type được sử dụng (local, cloudinary, s3)
     */
    @JsonProperty("storageType")
    private String storageType;
    
    /**
     * Duration của audio file (seconds) nếu có thể extract
     */
    @JsonProperty("durationSeconds")
    private Integer durationSeconds;
    
    /**
     * Absolute path to audio file on disk (for local storage)
     * Used for reading file after upload
     */
    @JsonProperty("storagePath")
    private String storagePath;
}
