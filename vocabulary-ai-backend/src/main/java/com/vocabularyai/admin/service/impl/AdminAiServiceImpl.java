package com.vocabularyai.admin.service.impl;

import com.vocabularyai.admin.dto.response.AiConversationDetailResponse;
import com.vocabularyai.admin.dto.response.AiConversationListResponse;
import com.vocabularyai.admin.dto.response.AiStatsResponse;
import com.vocabularyai.admin.repository.AdminAiRepository;
import com.vocabularyai.admin.service.AdminAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin AI Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAiServiceImpl implements AdminAiService {
    
    private final AdminAiRepository adminAiRepository;
    
    /**
     * Convert map result to AiConversationListResponse
     */
    private AiConversationListResponse mapToListResponse(Map<String, Object> result) {
        return AiConversationListResponse.builder()
                .id(((Number) result.get("id")).longValue())
                .userId(result.get("userId") != null ? ((Number) result.get("userId")).longValue() : null)
                .userEmail((String) result.get("userEmail"))
                .conversationType((String) result.get("conversationType"))
                .title((String) result.get("title"))
                .messageCount(result.get("messageCount") != null ? ((Number) result.get("messageCount")).intValue() : 0)
                .isArchived((Boolean) result.get("isArchived"))
                .createdAt((java.time.LocalDateTime) result.get("createdAt"))
                .updatedAt((java.time.LocalDateTime) result.get("updatedAt"))
                .build();
    }
    
    /**
     * Convert map result to AiConversationDetailResponse
     */
    private AiConversationDetailResponse mapToDetailResponse(Map<String, Object> result) {
        return AiConversationDetailResponse.builder()
                .id(((Number) result.get("id")).longValue())
                .userId(result.get("userId") != null ? ((Number) result.get("userId")).longValue() : null)
                .userEmail((String) result.get("userEmail"))
                .userFirstName((String) result.get("userFullName"))
                .userLastName(null)
                .conversationType((String) result.get("conversationType"))
                .title((String) result.get("title"))
                .summary((String) result.get("summary"))
                .messageCount(result.get("messageCount") != null ? ((Number) result.get("messageCount")).intValue() : 0)
                .isArchived((Boolean) result.get("isArchived"))
                .createdAt((java.time.LocalDateTime) result.get("createdAt"))
                .updatedAt((java.time.LocalDateTime) result.get("updatedAt"))
                .build();
    }
    
    @Override
    public Page<AiConversationListResponse> listConversations(Pageable pageable) {
        log.debug("Fetching AI conversations - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<?> results = adminAiRepository.getConversationList(pageable);
        
        List<AiConversationListResponse> conversations = results.getContent().stream()
                .map(item -> mapToListResponse((Map<String, Object>) item))
                .collect(Collectors.toList());
        
        return new PageImpl<>(conversations, pageable, results.getTotalElements());
    }
    
    @Override
    public Page<AiConversationListResponse> getConversationsByUser(Long userId, Pageable pageable) {
        log.debug("Fetching AI conversations for user: {} - page: {}", userId, pageable.getPageNumber());
        
        Page<?> results = adminAiRepository.getConversationsByUser(userId, pageable);
        
        List<AiConversationListResponse> conversations = results.getContent().stream()
                .map(item -> mapToListResponse((Map<String, Object>) item))
                .collect(Collectors.toList());
        
        return new PageImpl<>(conversations, pageable, results.getTotalElements());
    }
    
    @Override
    public Page<AiConversationListResponse> getConversationsByType(String conversationType, Pageable pageable) {
        log.debug("Fetching AI conversations by type: {} - page: {}", conversationType, pageable.getPageNumber());
        
        Page<?> results = adminAiRepository.getConversationsByType(conversationType, pageable);
        
        List<AiConversationListResponse> conversations = results.getContent().stream()
                .map(item -> mapToListResponse((Map<String, Object>) item))
                .collect(Collectors.toList());
        
        return new PageImpl<>(conversations, pageable, results.getTotalElements());
    }
    
    @Override
    public Page<AiConversationListResponse> searchConversations(String searchTerm, Pageable pageable) {
        log.debug("Searching AI conversations with term: {} - page: {}", searchTerm, pageable.getPageNumber());
        
        Page<?> results = adminAiRepository.searchConversations(searchTerm, pageable);
        
        List<AiConversationListResponse> conversations = results.getContent().stream()
                .map(item -> mapToListResponse((Map<String, Object>) item))
                .collect(Collectors.toList());
        
        return new PageImpl<>(conversations, pageable, results.getTotalElements());
    }
    
    @Override
    public AiConversationDetailResponse getConversationDetail(Long conversationId) {
        log.debug("Fetching AI conversation detail for ID: {}", conversationId);
        
        List<?> results = adminAiRepository.getConversationDetail(conversationId);
        
        if (results.isEmpty()) {
            log.warn("AI conversation not found with ID: {}", conversationId);
            return null;
        }
        
        Map<String, Object> result = (Map<String, Object>) results.get(0);
        return mapToDetailResponse(result);
    }
    
    @Override
    public AiStatsResponse getStats() {
        log.debug("Fetching AI statistics");
        
        // Fetch overall stats
        List<?> statsResults = adminAiRepository.getConversationStats();
        long totalConversations = 0;
        long activeUsers = 0;
        
        if (!statsResults.isEmpty()) {
            Map<String, Object> statsMap = (Map<String, Object>) statsResults.get(0);
            totalConversations = ((Number) statsMap.getOrDefault("totalConversations", 0)).longValue();
            activeUsers = ((Number) statsMap.getOrDefault("activeUsers", 0)).longValue();
        }
        
        // Fetch conversation type distribution
        List<?> typeResults = adminAiRepository.getConversationCountByType();
        Map<String, Long> typeDistribution = new HashMap<>();
        
        typeResults.forEach(item -> {
            Map<String, Object> typeMap = (Map<String, Object>) item;
            String type = (String) typeMap.get("conversationType");
            Long count = ((Number) typeMap.get("count")).longValue();
            typeDistribution.put(type, count);
        });
        
        return AiStatsResponse.builder()
                .totalConversations(totalConversations)
                .activeUsers(activeUsers)
                .totalMessages(0)
                .avgMessagesPerConversation(0.0)
                .conversationTypeDistribution(typeDistribution)
                .build();
    }
}
