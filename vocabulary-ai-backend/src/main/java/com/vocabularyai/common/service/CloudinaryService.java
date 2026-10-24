package com.vocabularyai.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.vocabularyai.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service for managing file uploads and deletions via Cloudinary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/webp", "image/gif", "image/jpg"
    );
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );

    /**
     * Upload user avatar image to Cloudinary.
     * Automatically crops to face and resizes to 400x400 square.
     *
     * @param userId ID of the user uploading the avatar
     * @param file   Uploaded image file
     * @return Secure HTTPS URL of the uploaded avatar image
     */
    public String uploadAvatar(Long userId, MultipartFile file) {
        validateAvatarFile(file);

        String publicId = String.format("user_%d_%d", userId, System.currentTimeMillis());
        log.info("Uploading avatar for userId: {} with public_id: {}", userId, publicId);

        try {
            @SuppressWarnings("rawtypes")
            Transformation transformation = new Transformation()
                .width(400)
                .height(400)
                .crop("fill")
                .gravity("face")
                .quality("auto")
                .fetchFormat("auto");

            Map<String, Object> params = ObjectUtils.asMap(
                "folder", "vocabulary-ai/avatars",
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", true,
                "transformation", transformation
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null) {
                secureUrl = (String) uploadResult.get("url");
            }

            log.info("Successfully uploaded avatar to Cloudinary for userId {}: {}", userId, secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Failed to upload avatar to Cloudinary for userId {}: {}", userId, e.getMessage(), e);
            throw new BadRequestException("Không thể tải ảnh lên máy chủ Cloudinary. Vui lòng thử lại sau!");
        }
    }

    /**
     * Delete an existing image from Cloudinary using its secure URL.
     *
     * @param imageUrl The full Cloudinary URL
     */
    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || !imageUrl.contains("cloudinary.com")) {
            return;
        }

        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null && !publicId.isBlank()) {
                log.info("Deleting asset from Cloudinary: {}", publicId);
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            }
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary asset {}: {}", imageUrl, e.getMessage());
        }
    }

    /**
     * Validate avatar image file type and size.
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn tệp ảnh để tải lên.");
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BadRequestException("Kích thước tệp quá lớn. Vui lòng chọn ảnh có dung lượng tối đa 10MB.");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        boolean isContentTypeValid = contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
        boolean isExtensionValid = false;

        if (originalFilename != null) {
            String lowerName = originalFilename.toLowerCase();
            for (String ext : ALLOWED_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    isExtensionValid = true;
                    break;
                }
            }
        }

        if (!isContentTypeValid && !isExtensionValid) {
            throw new BadRequestException("Định dạng tệp không hợp lệ. Vui lòng chọn ảnh định dạng JPG, PNG, WEBP hoặc GIF.");
        }
    }

    /**
     * Extract public_id from Cloudinary URL.
     * Example: https://res.cloudinary.com/demo/image/upload/v1234567/vocabulary-ai/avatars/user_1_123.jpg
     * -> vocabulary-ai/avatars/user_1_123
     */
    private String extractPublicId(String url) {
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) {
            return null;
        }

        String path = url.substring(uploadIndex + 8);

        // Remove version if present (e.g., v123456789/)
        if (path.startsWith("v")) {
            int slashIndex = path.indexOf('/');
            if (slashIndex != -1 && path.substring(1, slashIndex).matches("\\d+")) {
                path = path.substring(slashIndex + 1);
            }
        }

        // Remove file extension
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex != -1) {
            path = path.substring(0, lastDotIndex);
        }

        return path;
    }
}
