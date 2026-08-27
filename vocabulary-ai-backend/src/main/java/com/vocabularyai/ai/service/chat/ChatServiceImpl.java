package com.vocabularyai.ai.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.dto.request.CreateConversationRequest;
import com.vocabularyai.ai.dto.request.SendMessageRequest;
import com.vocabularyai.ai.dto.response.ConversationDetailResponse;
import com.vocabularyai.ai.dto.response.ConversationResponse;
import com.vocabularyai.ai.dto.response.MessageResponse;
import com.vocabularyai.ai.entity.AiConversation;
import com.vocabularyai.ai.entity.AiConversationType;
import com.vocabularyai.ai.entity.AiMessage;
import com.vocabularyai.ai.entity.AiMessageRole;
import com.vocabularyai.ai.mapper.ConversationMapper;
import com.vocabularyai.ai.mapper.MessageMapper;
import com.vocabularyai.ai.repository.AiConversationRepository;
import com.vocabularyai.ai.repository.AiMessageRepository;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ChatService Implementation
 * 
 * Triển khai quản lý trò chuyện AI:
 * - Tạo/xóa/archive cuộc trò chuyện
 * - Xử lý gửi tin nhắn từ user
 * - Tích hợp với AI service để tạo phản hồi
 * - Quản lý context từ cache
 * - Lưu trữ message history
 * 
 * Dependencies:
 * - AiConversationRepository: Quản lý conversation database
 * - AiMessageRepository: Quản lý message database
 * - UserRepository: Xác thực người dùng
 * - GeminiAIClientService: Gọi AI API
 * - PromptEngineeringService: Xây dựng prompt
 * - CacheService: Cache conversation context
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GeminiAIClientService geminiService;
    private final PromptEngineeringService promptService;
    private final CacheService cacheService;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.conversation.context-window-size:10}")
    private int contextWindowSize;
    
    @Value("${ai.conversation.auto-title-tokens:50}")
    private int autoTitleTokens;
    
    // ==================== Tạo Cuộc Trò Chuyện ====================
    
    @Override
    @Transactional
    public ConversationResponse createConversation(Long userId, CreateConversationRequest request) {
        log.info("Creating conversation for user: {}, type: {}", userId, request.getType());
        
        // Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found: {}", userId);
                return new IllegalArgumentException("User not found: " + userId);
            });
        
        // Tạo conversation entity
        AiConversation conversation = AiConversation.builder()
            .user(user)
            .type(request.getType())
            .title(request.getTitle() != null ? request.getTitle() : generateDefaultTitle(request.getType()))
            .isArchived(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // Lưu vào database
        AiConversation savedConversation = conversationRepository.save(conversation);
        
        log.info("Conversation created successfully, ID: {}", savedConversation.getId());
        
        return conversationMapper.toConversationResponse(savedConversation);
    }
    
    // ==================== Gửi Tin Nhắn ====================
    
    @Override
    @Transactional
    public MessageResponse sendMessage(Long conversationId, Long userId, SendMessageRequest request) {
        log.info("Sending message to conversation: {}, user: {}", conversationId, userId);
        
        long startTime = System.currentTimeMillis();
        
        // 1. Kiểm tra authorization
        AiConversation conversation = getConversationAndVerifyOwnership(conversationId, userId);
        
        // 2. Lưu user message
        AiMessage userMessage = AiMessage.builder()
            .conversation(conversation)
            .role(AiMessageRole.USER)
            .content(request.getContent())
            .tokensUsed(promptService.estimateTokenCount(request.getContent()))
            .responseTimeMs(0L)
            .createdAt(LocalDateTime.now())
            .metadata(serializeMetadata(request.getContext()))
            .build();
        
        AiMessage savedUserMessage = messageRepository.save(userMessage);
        log.debug("User message saved, ID: {}", savedUserMessage.getId());
        
        // 3. Lấy conversation context
        List<AiMessage> recentMessages = messageRepository.findRecentMessages(conversationId, contextWindowSize);
        
        // 4. Xây dựng prompt
        String fullPrompt = promptService.buildChatPrompt(
            recentMessages,
            request.getContent(),
            conversation.getType()
        );
        
        log.debug("Built prompt, estimated tokens: {}", promptService.estimateTokenCount(fullPrompt));
        
        // 5. Gọi Gemini API để lấy phản hồi
        String aiResponse;
        try {
            aiResponse = geminiService.generateResponse(fullPrompt, userId);
            log.debug("AI response received, length: {}", aiResponse.length());
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            throw e;
        }
        
        // 6. Lưu AI response
        long responseTime = System.currentTimeMillis() - startTime;
        int responseTokens = promptService.estimateTokenCount(aiResponse);
        
        AiMessage assistantMessage = AiMessage.builder()
            .conversation(conversation)
            .role(AiMessageRole.ASSISTANT)
            .content(aiResponse)
            .tokensUsed(responseTokens)
            .responseTimeMs(responseTime)
            .createdAt(LocalDateTime.now())
            .build();
        
        AiMessage savedAssistantMessage = messageRepository.save(assistantMessage);
        log.debug("Assistant message saved, ID: {}", savedAssistantMessage.getId());
        
        // 7. Cập nhật conversation lastUpdated time
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        // 8. Cache conversation context (cho lần tới)
        cacheService.cacheConversationContext(conversationId, recentMessages, 30 * 60);
        
        // 9. Invalidate cache để refresh conversation detail
        cacheService.invalidateConversationContext(conversationId);
        
        log.info("Message sent successfully, response time: {}ms", responseTime);
        
        // Trả về response
        return messageMapper.toMessageResponse(savedAssistantMessage);
    }
    
    // ==================== Lấy Cuộc Trò Chuyện ====================
    
    @Override
    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversationDetail(Long conversationId, Long userId) {
        log.debug("Getting conversation detail: {}, user: {}", conversationId, userId);
        
        AiConversation conversation = getConversationAndVerifyOwnership(conversationId, userId);
        
        // Lấy tất cả messages (theo thứ tự thời gian)
        List<AiMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        
        ConversationDetailResponse response = ConversationDetailResponse.builder()
            .id(conversation.getId())
            .type(conversation.getType())
            .title(conversation.getTitle())
            .summary(conversation.getSummary())
            .isArchived(conversation.getIsArchived())
            .createdAt(conversation.getCreatedAt())
            .updatedAt(conversation.getUpdatedAt())
            .messages(messages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList()))
            .build();
        
        log.debug("Conversation detail retrieved, message count: {}", messages.size());
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> listActiveConversations(Long userId, Pageable pageable) {
        log.debug("Listing active conversations for user: {}", userId);
        
        verifyUserExists(userId);
        
        return conversationRepository.findActiveConversationsByUserId(userId, pageable)
            .map(conversationMapper::toConversationResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> listArchivedConversations(Long userId, Pageable pageable) {
        log.debug("Listing archived conversations for user: {}", userId);
        
        verifyUserExists(userId);
        
        return conversationRepository.findArchivedConversationsByUserId(userId, pageable)
            .map(conversationMapper::toConversationResponse);
    }
    
    // ==================== Quản Lý Cuộc Trò Chuyện ====================
    
    @Override
    @Transactional
    public void archiveConversation(Long conversationId, Long userId) {
        log.info("Archiving conversation: {}, user: {}", conversationId, userId);
        
        AiConversation conversation = getConversationAndVerifyOwnership(conversationId, userId);
        
        conversation.setIsArchived(true);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        // Invalidate cache
        cacheService.invalidateConversationContext(conversationId);
        
        log.info("Conversation archived successfully");
    }
    
    @Override
    @Transactional
    public void unarchiveConversation(Long conversationId, Long userId) {
        log.info("Unarchiving conversation: {}, user: {}", conversationId, userId);
        
        AiConversation conversation = getConversationAndVerifyOwnership(conversationId, userId);
        
        conversation.setIsArchived(false);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        // Invalidate cache
        cacheService.invalidateConversationContext(conversationId);
        
        log.info("Conversation unarchived successfully");
    }
    
    @Override
    @Transactional
    public void updateConversationTitle(Long conversationId, Long userId, String newTitle) {
        log.info("Updating conversation title: {}, user: {}, new title: {}", conversationId, userId, newTitle);
        
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        
        AiConversation conversation = getConversationAndVerifyOwnership(conversationId, userId);
        
        conversation.setTitle(newTitle);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        // Invalidate cache
        cacheService.invalidateConversationContext(conversationId);
        
        log.info("Conversation title updated successfully");
    }
    
    @Override
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        log.info("Deleting conversation: {}, user: {}", conversationId, userId);
        
        AiConversation conversation = getConversationAndVerifyOwnership(conversationId, userId);
        
        // Xóa tất cả messages (cascade delete)
        messageRepository.deleteByConversationId(conversationId);
        
        // Xóa conversation
        conversationRepository.delete(conversation);
        
        // Invalidate cache
        cacheService.invalidateConversationContext(conversationId);
        
        log.info("Conversation deleted successfully");
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getConversationCount(Long userId) {
        log.debug("Getting conversation count for user: {}", userId);
        
        verifyUserExists(userId);
        
        return conversationRepository.countByUserId(userId);
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Lấy conversation và kiểm tra user sở hữu nó (IDOR prevention)
     * 
     * ✅ TASK 9: Fixed to throw ForbiddenException for proper 403 status
     */
    private AiConversation getConversationAndVerifyOwnership(Long conversationId, Long userId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> {
                log.warn("IDOR prevention: Unauthorized access attempt: conversationId={}, userId={}", 
                    conversationId, userId);
                return new com.vocabularyai.common.exception.ForbiddenException(
                    "You don't have permission to access this conversation"
                );
            });
    }
    
    /**
     * Kiểm tra user tồn tại
     */
    private void verifyUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("User not found: {}", userId);
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }
    
    /**
     * Tạo tiêu đề mặc định dựa trên loại cuộc trò chuyện
     */
    private String generateDefaultTitle(AiConversationType type) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format("%02d:%02d", now.getHour(), now.getMinute());
        
        return switch (type) {
            case GENERAL -> "General Chat - " + timestamp;
            case VOCABULARY -> "Vocabulary Practice - " + timestamp;
            case GRAMMAR -> "Grammar Check - " + timestamp;
            case LEARNING -> "Learning Session - " + timestamp;
        };
    }
    
    /**
     * Serialize context metadata thành JSON
     */
    private String serializeMetadata(String context) {
        if (context == null || context.trim().isEmpty()) {
            return null;
        }
        
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("context", context);
            metadata.put("timestamp", LocalDateTime.now());
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Error serializing metadata: {}", e.getMessage());
            return null;
        }
    }
}
