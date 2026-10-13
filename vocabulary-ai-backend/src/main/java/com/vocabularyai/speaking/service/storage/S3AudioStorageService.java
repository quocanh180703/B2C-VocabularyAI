package com.vocabularyai.speaking.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * AWS S3 audio storage implementation.
 * For Production environments with scalability requirements.
 *
 * S3 storage structure:
 * s3://{bucket}/audio/session-{sessionId}/attempt-{attemptNumber}-{uuid}.{extension}
 *
 * Example:
 * s3://vocabulary-ai-audio/audio/session-1/attempt-1-a1b2c3d4.wav
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "speaking.storage.type", havingValue = "s3")
public class S3AudioStorageService implements AudioStorageService {

    private final S3Client s3Client;

    @Value("${speaking.storage.s3-bucket:vocabulary-ai-audio}")
    private String bucketName;

    @Value("${speaking.storage.s3-region:us-east-1}")
    private String region;

    @Value("${speaking.audio.supported-formats:WAV,MP3,M4A,WEBM,OGG,FLAC}")
    private String supportedFormatsStr;

    @Value("${speaking.audio.max-file-size:52428800}")
    private long maxFileSize;

    @Value("${aws.s3.expiration-hours:24}")
    private int expirationHours;

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
        log.info("Uploading audio to S3 for session {} attempt {}", sessionId, attemptNumber);

        try {
            // Validate file
            validateAudioFile(audioFile);

            // Generate S3 object key
            String fileExtension = getFileExtension(audioFile.getOriginalFilename());
            String objectKey = String.format("audio/session-%d/attempt-%d-%s.%s",
                sessionId,
                attemptNumber,
                UUID.randomUUID().toString(),
                fileExtension);

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(audioFile.getContentType())
                .contentLength(audioFile.getSize())
                .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(audioFile.getInputStream(), audioFile.getSize()));

            log.info("Audio uploaded to S3: s3://{}/{}", bucketName, objectKey);

            // Build audio URL
            String audioUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                objectKey);

            // Extract metadata
            long fileSize = audioFile.getSize();
            String mimeType = audioFile.getContentType();

            AudioMetadata metadata = AudioMetadata.builder()
                .audioUrl(audioUrl)
                .originalFilename(audioFile.getOriginalFilename())
                .fileSize(fileSize)
                .mimeType(mimeType)
                .storageType("s3")
                .durationSeconds(null) // Duration extraction requires ffprobe
                .build();

            log.info("Audio metadata: {}", metadata);
            return metadata;

        } catch (IOException e) {
            log.error("Failed to upload audio file to S3 for session {}", sessionId, e);
            throw new AudioStorageException("Failed to upload audio file to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAudio(String audioUrl) {
        log.info("Deleting audio from S3: {}", audioUrl);

        try {
            // Extract object key from URL
            String objectKey = extractObjectKeyFromUrl(audioUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            DeleteObjectResponse deleteObjectResponse = s3Client.deleteObject(deleteObjectRequest);

            log.info("Audio deleted from S3: s3://{}/{}", bucketName, objectKey);

        } catch (Exception e) {
            log.error("Failed to delete audio file from S3: {}", audioUrl, e);
            throw new AudioStorageException("Failed to delete audio file from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadAudio(String audioUrl) {
        log.info("Downloading audio from S3: {}", audioUrl);

        try {
            // Extract object key from URL
            String objectKey = extractObjectKeyFromUrl(audioUrl);

            // Check if object exists
            if (!audioExists(audioUrl)) {
                log.error("Audio file not found in S3: {}", objectKey);
                throw new AudioStorageException("Audio file not found in S3: " + audioUrl);
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            byte[] fileContent = s3Client.getObject(getObjectRequest).readAllBytes();

            log.info("Audio downloaded from S3: {} bytes", fileContent.length);
            return fileContent;

        } catch (IOException e) {
            log.error("Failed to download audio file from S3: {}", audioUrl, e);
            throw new AudioStorageException("Failed to download audio file from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean audioExists(String audioUrl) {
        try {
            String objectKey = extractObjectKeyFromUrl(audioUrl);

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength() != null && response.contentLength() > 0;

        } catch (NoSuchKeyException e) {
            log.debug("Audio file not found in S3: {}", audioUrl);
            return false;
        } catch (Exception e) {
            log.warn("Error checking audio existence in S3: {}", audioUrl, e);
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "s3";
    }

    /**
     * Extract S3 object key from URL
     * URL format: https://bucket.s3.region.amazonaws.com/audio/session-1/attempt-1-uuid.wav
     * Returns: audio/session-1/attempt-1-uuid.wav
     */
    private String extractObjectKeyFromUrl(String audioUrl) {
        try {
            // Parse URL to extract path after domain
            java.net.URL url = new java.net.URL(audioUrl);
            String path = url.getPath();

            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            return path;

        } catch (java.net.MalformedURLException e) {
            log.error("Invalid URL format: {}", audioUrl, e);
            throw new AudioStorageException("Invalid S3 URL format: " + audioUrl, e);
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
