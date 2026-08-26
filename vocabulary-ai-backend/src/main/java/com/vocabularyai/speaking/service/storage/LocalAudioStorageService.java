package com.vocabularyai.speaking.service.storage;

import com.vocabularyai.speaking.config.SpeakingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * LocalAudioStorageService Implementation
 * 
 * Lưu audio files trên local filesystem
 * 
 * Storage path: ${AUDIO_STORAGE_LOCAL_PATH}/session-{sessionId}/attempt-{attemptNumber}-{uniqueId}.{ext}
 * Ví dụ: /tmp/vocabulary-ai/audio/session-123/attempt-1-abc123def456.wav
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "speaking.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalAudioStorageService implements AudioStorageService {
    
    private final SpeakingProperties speakingProperties;
    
    @Override
    public AudioMetadata uploadAudio(Long sessionId, Integer attemptNumber, MultipartFile audioFile) {
        log.info("Upload audio file locally: sessionId={}, attemptNumber={}, filename={}", 
                sessionId, attemptNumber, audioFile.getOriginalFilename());
        
        // Validate file
        validateAudioFile(audioFile);
        
        try {
            // Prepare storage path
            String basePath = speakingProperties.getStorage().getLocalPath();
            Path sessionDirectory = Paths.get(basePath, "session-" + sessionId);
            
            // Create directories if not exist
            Files.createDirectories(sessionDirectory);
            
            // Generate unique filename
            String originalFilename = audioFile.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String filename = String.format("attempt-%d-%d-%s.%s", attemptNumber, System.currentTimeMillis(), uniqueId, extension);
            
            // Save file
            Path filePath = sessionDirectory.resolve(filename);
            audioFile.transferTo(filePath.toFile());
            
            log.info("Audio file uploaded successfully: {}", filePath);
            
            // Build relative URL path
            String audioUrl = String.format("/storage/audio/session-%d/%s", sessionId, filename);
            
            // Return metadata
            return AudioMetadata.builder()
                    .audioUrl(audioUrl)
                    .originalFilename(originalFilename)
                    .fileSize(audioFile.getSize())
                    .mimeType(audioFile.getContentType())
                    .storageType("local")
                    .build();
            
        } catch (IOException e) {
            log.error("Error uploading audio file: {}", e.getMessage(), e);
            throw new AudioStorageException("Failed to upload audio file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteAudio(String audioUrl) {
        log.info("Delete audio file: {}", audioUrl);
        
        if (audioUrl == null || audioUrl.isEmpty()) {
            log.warn("Audio URL is empty, skip delete");
            return;
        }
        
        try {
            // Convert URL back to file path
            String basePath = speakingProperties.getStorage().getLocalPath();
            String relativePath = extractRelativePath(audioUrl);
            Path filePath = Paths.get(basePath, relativePath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Audio file deleted: {}", filePath);
                
                // Try to delete empty directory
                Path parentDir = filePath.getParent();
                if (Files.exists(parentDir) && Files.isDirectory(parentDir)) {
                    try {
                        Files.delete(parentDir);
                        log.debug("Empty session directory deleted: {}", parentDir);
                    } catch (IOException e) {
                        // Directory not empty, ignore
                        log.debug("Directory not empty, keep it: {}", parentDir);
                    }
                }
            } else {
                log.warn("Audio file not found: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error deleting audio file: {}", e.getMessage(), e);
            throw new AudioStorageException("Failed to delete audio file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] downloadAudio(String audioUrl) {
        log.info("Download audio file: {}", audioUrl);
        
        try {
            // Convert URL back to file path
            String basePath = speakingProperties.getStorage().getLocalPath();
            String relativePath = extractRelativePath(audioUrl);
            Path filePath = Paths.get(basePath, relativePath);
            
            if (!Files.exists(filePath)) {
                throw new AudioStorageException("Audio file not found: " + audioUrl);
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            log.info("Audio file downloaded successfully: {} bytes", fileContent.length);
            
            return fileContent;
            
        } catch (IOException e) {
            log.error("Error downloading audio file: {}", e.getMessage(), e);
            throw new AudioStorageException("Failed to download audio file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean audioExists(String audioUrl) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            return false;
        }
        
        try {
            String basePath = speakingProperties.getStorage().getLocalPath();
            String relativePath = extractRelativePath(audioUrl);
            Path filePath = Paths.get(basePath, relativePath);
            
            return Files.exists(filePath);
        } catch (Exception e) {
            log.error("Error checking audio existence: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getStorageType() {
        return "local";
    }
    
    /**
     * Validate audio file
     * 
     * Kiểm tra:
     * - File không trống
     * - Kích thước file <= max-file-size
     * - Format hợp lệ
     * - MIME type hợp lệ
     */
    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AudioStorageException("Audio file is empty");
        }
        
        // Check file size
        long maxFileSize = speakingProperties.getAudio().getMaxFileSize();
        if (file.getSize() > maxFileSize) {
            throw new AudioStorageException(
                    String.format("File size exceeds limit: %d MB (max: %d MB)", 
                            file.getSize() / (1024 * 1024), maxFileSize / (1024 * 1024))
            );
        }
        
        // Check file format
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new AudioStorageException("Original filename is missing");
        }
        
        String extension = FilenameUtils.getExtension(originalFilename).toUpperCase();
        if (!speakingProperties.getAudio().getSupportedFormatsList().contains(extension)) {
            throw new AudioStorageException(
                    String.format("Unsupported audio format: %s (supported: %s)", 
                            extension, speakingProperties.getAudio().getSupportedFormats())
            );
        }
        
        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !speakingProperties.getAudio().getSupportedMimeTypesList().contains(mimeType)) {
            log.warn("Unexpected MIME type: {} (expected one of: {})", 
                    mimeType, speakingProperties.getAudio().getSupportedMimeTypes());
            // Không throw, chỉ warning vì MIME type có thể không chính xác từ client
        }
        
        log.debug("Audio file validation passed: {} ({} bytes)", originalFilename, file.getSize());
    }
    
    /**
     * Extract relative path từ URL
     * 
     * Input: /storage/audio/session-123/attempt-1-1234567890-abc123.wav
     * Output: session-123/attempt-1-1234567890-abc123.wav
     */
    private String extractRelativePath(String audioUrl) {
        if (audioUrl.startsWith("/storage/audio/")) {
            return audioUrl.substring("/storage/audio/".length());
        }
        return audioUrl;
    }
}
