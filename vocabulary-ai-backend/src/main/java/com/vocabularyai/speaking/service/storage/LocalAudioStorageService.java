package com.vocabularyai.speaking.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Local file system audio storage implementation.
 * MVP provider for Development/Testing environments.
 *
 * Storage structure:
 * {base-path}/audio/session-{sessionId}/attempt-{attemptNumber}-{uuid}.{extension}
 *
 * Example:
 * /tmp/vocabulary-ai/audio/session-1/attempt-1-a1b2c3d4.wav
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "speaking.storage.type", havingValue = "local")
public class LocalAudioStorageService implements AudioStorageService {

    @Value("${speaking.storage.local-path:/tmp/vocabulary-ai/audio}")
    private String basePath;

    @Value("${speaking.audio.supported-formats:WAV,MP3,M4A,WEBM,OGG,FLAC}")
    private String supportedFormatsStr;

    @Value("${speaking.audio.max-file-size:52428800}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
        "audio/wav",
        "audio/mpeg",
        "audio/mp4",
        "audio/webm",
        "audio/ogg",
        "audio/flac",
        "audio/x-wav",
        "audio/x-m4a"
    ));

    @Override
    public AudioMetadata uploadAudio(Long sessionId, Integer attemptNumber, MultipartFile audioFile) {
        log.info("Uploading audio for session {} attempt {}", sessionId, attemptNumber);

        try {
            // Validate file
            validateAudioFile(audioFile);

            // Create session-specific directory
            String sessionDir = String.format("session-%d", sessionId);
            Path sessionPath = Paths.get(basePath, sessionDir);
            Files.createDirectories(sessionPath);

            // Generate unique filename
            String fileExtension = getFileExtension(audioFile.getOriginalFilename());
            String uniqueFilename = String.format("attempt-%d-%s.%s",
                attemptNumber,
                UUID.randomUUID().toString(),
                fileExtension);

            // Save file
            Path filePath = sessionPath.resolve(uniqueFilename);
            audioFile.transferTo(filePath.toFile());

            log.info("Audio file saved: {}", filePath);

            // Build audio URL (for serving via controller)
            String audioUrl = String.format("/api/v1/speaking/audio/%s/%s", sessionDir, uniqueFilename);

            // Extract metadata
            long fileSize = audioFile.getSize();
            String mimeType = audioFile.getContentType();

            AudioMetadata metadata = AudioMetadata.builder()
                .audioUrl(audioUrl)
                .originalFilename(audioFile.getOriginalFilename())
                .fileSize(fileSize)
                .mimeType(mimeType)
                .storageType("local")
                .storagePath(filePath.toString())  // ✅ Set absolute path for reading
                .durationSeconds(null) // Duration extraction requires ffprobe
                .build();

            log.info("Audio metadata: {}", metadata);
            return metadata;

        } catch (IOException e) {
            log.error("Failed to upload audio file for session {}", sessionId, e);
            throw new AudioStorageException("Failed to upload audio file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAudio(String audioUrl) {
        log.info("Deleting audio: {}", audioUrl);

        try {
            Path filePath = resolveAudioPath(audioUrl);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Audio file deleted: {}", filePath);

                // Try to delete empty session directory
                Files.deleteIfExists(filePath.getParent());
            } else {
                log.warn("Audio file not found: {}", filePath);
            }

        } catch (IOException e) {
            log.error("Failed to delete audio file: {}", audioUrl, e);
            throw new AudioStorageException("Failed to delete audio file: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadAudio(String audioUrl) {
        log.info("Downloading audio: {}", audioUrl);

        try {
            Path filePath = resolveAudioPath(audioUrl);

            if (!Files.exists(filePath)) {
                log.error("Audio file not found: {}", filePath);
                throw new AudioStorageException("Audio file not found: " + audioUrl);
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            log.info("Audio downloaded: {} bytes", fileContent.length);
            return fileContent;

        } catch (IOException e) {
            log.error("Failed to download audio file: {}", audioUrl, e);
            throw new AudioStorageException("Failed to download audio file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean audioExists(String audioUrl) {
        try {
            Path filePath = resolveAudioPath(audioUrl);
            return Files.exists(filePath);
        } catch (Exception e) {
            log.warn("Error checking audio existence: {}", audioUrl, e);
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "local";
    }

    /**
     * Resolve local file path from audioUrl
     * audioUrl format: /api/v1/speaking/audio/session-1/attempt-1-uuid.wav
     * Converts to: {basePath}/session-1/attempt-1-uuid.wav
     */
    private Path resolveAudioPath(String audioUrl) {
        // Extract the relative path from URL
        String relativePath = audioUrl.replace("/api/v1/speaking/audio/", "");
        return Paths.get(basePath, relativePath);
    }

    /**
     * Validate audio file before upload
     */
    private void validateAudioFile(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new AudioStorageException("Audio file is empty");
        }

        // Check file size
        if (audioFile.getSize() > maxFileSize) {
            throw new AudioStorageException(
                String.format("File size %d exceeds maximum %d bytes",
                    audioFile.getSize(), maxFileSize));
        }

        // Check MIME type
        String mimeType = audioFile.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new AudioStorageException(
                String.format("Unsupported MIME type: %s", mimeType));
        }

        // Check file extension
        String fileExtension = getFileExtension(audioFile.getOriginalFilename());
        Set<String> supportedFormats = new HashSet<>(
            Arrays.asList(supportedFormatsStr.split(",")));
        if (!supportedFormats.contains(fileExtension.toUpperCase())) {
            throw new AudioStorageException(
                String.format("Unsupported file format: %s", fileExtension));
        }
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new AudioStorageException("Invalid filename: " + filename);
        }
        return FilenameUtils.getExtension(filename).toLowerCase();
    }
}
