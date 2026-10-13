package com.vocabularyai.speaking.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Cloudinary audio storage implementation.
 * For Production environments with CDN distribution needs.
 *
 * Cloudinary storage structure:
 * https://res.cloudinary.com/{cloud-name}/video/upload/v{version}/vocabulary-ai/session-{sessionId}/attempt-{attemptNumber}-{uuid}.{extension}
 *
 * Example:
 * https://res.cloudinary.com/demo/video/upload/v1234567890/vocabulary-ai/session-1/attempt-1-a1b2c3d4.wav
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "speaking.storage.type", havingValue = "cloudinary")
public class CloudinaryAudioStorageService implements AudioStorageService {

    private final Cloudinary cloudinary;

    @Value("${speaking.audio.supported-formats:WAV,MP3,M4A,WEBM,OGG,FLAC}")
    private String supportedFormatsStr;

    @Value("${speaking.audio.max-file-size:52428800}")
    private long maxFileSize;

    @Value("${cloudinary.folder:vocabulary-ai}")
    private String cloudinaryFolder;

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
        log.info("Uploading audio to Cloudinary for session {} attempt {}", sessionId, attemptNumber);

        try {
            // Validate file
            validateAudioFile(audioFile);

            // Generate Cloudinary public ID
            String fileExtension = getFileExtension(audioFile.getOriginalFilename());
            String publicId = String.format("%s/session-%d/attempt-%d-%s",
                cloudinaryFolder,
                sessionId,
                attemptNumber,
                UUID.randomUUID().toString());

            // Upload to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                audioFile.getInputStream(),
                ObjectUtils.asMap(
                    "resource_type", "raw", // Use "raw" for audio files, "video" would work too
                    "public_id", publicId,
                    "format", fileExtension,
                    "folder", cloudinaryFolder,
                    "use_filename", false,
                    "unique_filename", false
                ));

            String audioUrl = (String) uploadResult.get("secure_url");
            log.info("Audio uploaded to Cloudinary: {}", audioUrl);

            // Extract metadata
            long fileSize = audioFile.getSize();
            String mimeType = audioFile.getContentType();
            Object duration = uploadResult.get("duration");
            Integer durationSeconds = duration != null ? ((Number) duration).intValue() : null;

            AudioMetadata metadata = AudioMetadata.builder()
                .audioUrl(audioUrl)
                .originalFilename(audioFile.getOriginalFilename())
                .fileSize(fileSize)
                .mimeType(mimeType)
                .storageType("cloudinary")
                .durationSeconds(durationSeconds)
                .build();

            log.info("Audio metadata: {}", metadata);
            return metadata;

        } catch (IOException e) {
            log.error("Failed to upload audio file to Cloudinary for session {}", sessionId, e);
            throw new AudioStorageException("Failed to upload audio file to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAudio(String audioUrl) {
        log.info("Deleting audio from Cloudinary: {}", audioUrl);

        try {
            // Extract public ID from URL
            String publicId = extractPublicIdFromUrl(audioUrl);

            // Delete from Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", "raw"));

            String resultCode = (String) deleteResult.get("result");
            if ("ok".equals(resultCode)) {
                log.info("Audio deleted from Cloudinary: {}", publicId);
            } else {
                log.warn("Audio deletion returned: {}", deleteResult);
            }

        } catch (Exception e) {
            log.error("Failed to delete audio file from Cloudinary: {}", audioUrl, e);
            throw new AudioStorageException("Failed to delete audio file from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadAudio(String audioUrl) {
        log.info("Downloading audio from Cloudinary: {}", audioUrl);

        try {
            // Download file from Cloudinary URL
            java.net.URL url = new java.net.URL(audioUrl);
            java.net.URLConnection connection = url.openConnection();
            connection.connect();

            java.io.InputStream inputStream = connection.getInputStream();
            byte[] fileContent = inputStream.readAllBytes();
            inputStream.close();

            log.info("Audio downloaded from Cloudinary: {} bytes", fileContent.length);
            return fileContent;

        } catch (IOException e) {
            log.error("Failed to download audio file from Cloudinary: {}", audioUrl, e);
            throw new AudioStorageException("Failed to download audio file from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean audioExists(String audioUrl) {
        try {
            java.net.URL url = new java.net.URL(audioUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200;

        } catch (Exception e) {
            log.warn("Error checking audio existence in Cloudinary: {}", audioUrl, e);
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "cloudinary";
    }

    /**
     * Extract public ID from Cloudinary URL
     * URL format: https://res.cloudinary.com/{cloud}/video/upload/v{version}/{path}/{publicId}.{format}
     * Returns: vocabulary-ai/session-1/attempt-1-uuid
     */
    private String extractPublicIdFromUrl(String audioUrl) {
        try {
            // Example: https://res.cloudinary.com/demo/video/upload/v1234/vocabulary-ai/session-1/attempt-1-uuid.wav
            // Extract path between last "/v" and the file extension
            int versionIndex = audioUrl.lastIndexOf("/v");
            if (versionIndex == -1) {
                versionIndex = audioUrl.lastIndexOf("/upload/");
            }

            String path = audioUrl.substring(versionIndex + 2); // Skip "/v" or "/upload/"

            // Remove the version number (e.g., "1234/")
            int slashIndex = path.indexOf("/");
            if (slashIndex > 0) {
                path = path.substring(slashIndex + 1);
            }

            // Remove file extension
            int dotIndex = path.lastIndexOf(".");
            if (dotIndex > 0) {
                path = path.substring(0, dotIndex);
            }

            return path;

        } catch (Exception e) {
            log.error("Failed to extract public ID from Cloudinary URL: {}", audioUrl, e);
            throw new AudioStorageException("Invalid Cloudinary URL format: " + audioUrl, e);
        }
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
