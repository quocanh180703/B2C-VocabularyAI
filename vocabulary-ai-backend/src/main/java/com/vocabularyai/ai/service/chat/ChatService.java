package com.vocabularyai.ai.service.chat;

import com.vocabularyai.ai.dto.request.CreateConversationRequest;
import com.vocabularyai.ai.dto.request.SendMessageRequest;
import com.vocabularyai.ai.dto.response.ConversationDetailResponse;
import com.vocabularyai.ai.dto.response.ConversationResponse;
import com.vocabularyai.ai.dto.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ChatService Interface
 * 
 * Quản lý các cuộc trò chuyện AI:
 * - Tạo cuộc trò chuyện mới
 * - Gửi tin nhắn và nhận phản hồi từ AI
 * - Lấy lịch sử trò chuyện
 * - Quản lý cuộc trò chuyện (archive, delete, update title)
 * 
 * Tích hợp với:
 * - GeminiAIClientService (gọi API AI)
 * - PromptEngineeringService (xây dựng prompt)
 * - CacheService (cache context)
 */
public interface ChatService {
    
    /**
     * Tạo một cuộc trò chuyện mới
     * 
     * @param userId ID người dùng
     * @param request CreateConversationRequest chứa type, title
     * @return ConversationResponse của cuộc trò chuyện mới tạo
     */
    ConversationResponse createConversation(Long userId, CreateConversationRequest request);
    
    /**
     * Gửi một tin nhắn và nhận phản hồi từ AI
     * 
     * Flow:
     * 1. Kiểm tra authorization (user owns conversation)
     * 2. Lưu user message vào database
     * 3. Lấy conversation history (từ cache hoặc DB)
     * 4. Xây dựng prompt với context
     * 5. Gọi Gemini API để lấy phản hồi
     * 6. Lưu AI response vào database
     * 7. Cập nhật cache conversation
     * 8. Trả về response
     * 
     * @param conversationId ID cuộc trò chuyện
     * @param userId ID người dùng (để verify ownership)
     * @param request SendMessageRequest chứa content, context
     * @return MessageResponse của AI response
     * @throws com.vocabularyai.common.exception.RateLimitExceededException nếu vượt giới hạn
     * @throws com.vocabularyai.common.exception.ExternalServiceException nếu Gemini API lỗi
     * @throws IllegalArgumentException nếu conversation không tồn tại hoặc user không sở hữu
     */
    MessageResponse sendMessage(Long conversationId, Long userId, SendMessageRequest request);
    
    /**
     * Lấy chi tiết một cuộc trò chuyện (bao gồm tất cả messages)
     * 
     * @param conversationId ID cuộc trò chuyện
     * @param userId ID người dùng (để verify ownership)
     * @return ConversationDetailResponse chứa conversation info + messages
     * @throws IllegalArgumentException nếu conversation không tồn tại hoặc user không sở hữu
     */
    ConversationDetailResponse getConversationDetail(Long conversationId, Long userId);
    
    /**
     * Lấy danh sách tất cả cuộc trò chuyện của user (active)
     * 
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<ConversationResponse> danh sách cuộc trò chuyện chưa archive
     */
    Page<ConversationResponse> listActiveConversations(Long userId, Pageable pageable);
    
    /**
     * Lấy danh sách cuộc trò chuyện đã archive
     * 
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page<ConversationResponse> danh sách cuộc trò chuyện đã archive
     */
    Page<ConversationResponse> listArchivedConversations(Long userId, Pageable pageable);
    
    /**
     * Archive một cuộc trò chuyện
     * 
     * @param conversationId ID cuộc trò chuyện
     * @param userId ID người dùng
     * @throws IllegalArgumentException nếu conversation không tồn tại hoặc user không sở hữu
     */
    void archiveConversation(Long conversationId, Long userId);
    
    /**
     * Unarchive một cuộc trò chuyện
     * 
     * @param conversationId ID cuộc trò chuyện
     * @param userId ID người dùng
     * @throws IllegalArgumentException nếu conversation không tồn tại hoặc user không sở hữu
     */
    void unarchiveConversation(Long conversationId, Long userId);
    
    /**
     * Cập nhật tiêu đề cuộc trò chuyện
     * 
     * @param conversationId ID cuộc trò chuyện
     * @param userId ID người dùng
     * @param newTitle Tiêu đề mới
     * @throws IllegalArgumentException nếu conversation không tồn tại hoặc user không sở hữu
     */
    void updateConversationTitle(Long conversationId, Long userId, String newTitle);
    
    /**
     * Xóa một cuộc trò chuyện (xóa tất cả messages)
     * 
     * @param conversationId ID cuộc trò chuyện
     * @param userId ID người dùng
     * @throws IllegalArgumentException nếu conversation không tồn tại hoặc user không sở hữu
     */
    void deleteConversation(Long conversationId, Long userId);
    
    /**
     * Lấy số lượng cuộc trò chuyện của user
     * 
     * @param userId ID người dùng
     * @return Số lượng cuộc trò chuyện
     */
    long getConversationCount(Long userId);
}
