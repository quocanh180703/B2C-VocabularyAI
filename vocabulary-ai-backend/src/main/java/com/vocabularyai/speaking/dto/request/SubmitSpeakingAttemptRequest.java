package com.vocabularyai.speaking.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO để submit một lần ghi âm speaking
 * 
 * Sử dụng multipart/form-data
 */
public class SubmitSpeakingAttemptRequest {
    
    /**
     * File audio ghi âm
     * Format: WAV, MP3, M4A, WEBM, OGG, FLAC
     * Max size: 50MB
     * Max duration: 10 minutes
     */
    @NotNull(message = "Audio file không được để trống")
    private MultipartFile audio;
    
    public MultipartFile getAudio() {
        return audio;
    }
    
    public void setAudio(MultipartFile audio) {
        this.audio = audio;
    }
}
