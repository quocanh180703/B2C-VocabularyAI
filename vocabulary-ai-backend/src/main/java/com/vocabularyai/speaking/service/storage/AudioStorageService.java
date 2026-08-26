package com.vocabularyai.speaking.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * AudioStorageService Interface
 * 
 * Abstraction cho việc upload, download và delete audio files
 * 
 * Hỗ trợ multiple storage providers:
 * - Local filesystem
 * - Cloudinary
 * - AWS S3
 * 
 * Implementation phải đảm bảo:
 * - Audio file validation (size, format, duration)
 * - MIME type checking
 * - Unique filename generation
 * - Metadata extraction (duration nếu có)
 * - Error handling
 * - Rollback nếu upload thất bại
 */
public interface AudioStorageService {
    
    /**
     * Upload audio file và lưu trữ
     * 
     * Flow:
     * 1. Validate audio file (size, format)
     * 2. Generate unique filename
     * 3. Upload file (local/Cloudinary/S3)
     * 4. Extract metadata (duration nếu có)
     * 5. Trả về AudioMetadata với audioUrl
     * 
     * @param sessionId ID phiên luyện nói (để tạo folder/path)
     * @param attemptNumber Số thứ tự attempt
     * @param audioFile MultipartFile được upload
     * @return AudioMetadata chứa audioUrl và thông tin file
     * @throws AudioStorageException nếu validate hoặc upload thất bại
     */
    AudioMetadata uploadAudio(Long sessionId, Integer attemptNumber, MultipartFile audioFile);
    
    /**
     * Delete audio file từ storage
     * 
     * Dùng khi user xóa attempt hoặc session
     * 
     * @param audioUrl URL/path của file cần xóa
     * @throws AudioStorageException nếu delete thất bại
     */
    void deleteAudio(String audioUrl);
    
    /**
     * Download audio file
     * 
     * Để user có thể download lại file audio của chính mình
     * 
     * @param audioUrl URL/path của file
     * @return byte array của file
     * @throws AudioStorageException nếu file không tồn tại hoặc read thất bại
     */
    byte[] downloadAudio(String audioUrl);
    
    /**
     * Check xem audio file tồn tại không
     * 
     * @param audioUrl URL/path của file
     * @return true nếu tồn tại, false nếu không
     */
    boolean audioExists(String audioUrl);
    
    /**
     * Lấy storage type hiện tại
     * 
     * @return Storage type (local, cloudinary, s3)
     */
    String getStorageType();
}
