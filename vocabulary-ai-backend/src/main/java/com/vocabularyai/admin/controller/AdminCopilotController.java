package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.AdminCopilotChatRequest;
import com.vocabularyai.admin.dto.response.AdminCopilotChatResponse;
import com.vocabularyai.admin.service.AdminCopilotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/copilot")
@Tag(name = "Admin Copilot", description = "AI-powered administrative operations assistant and analytics query engine")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminCopilotController {

    private final AdminCopilotService adminCopilotService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with Admin Copilot", description = "Query system metrics, AI costs, vocabulary health, and revenue using natural Vietnamese")
    public ResponseEntity<AdminCopilotChatResponse> chat(
            @Valid @RequestBody AdminCopilotChatRequest request
    ) {
        log.info("POST /api/admin/copilot/chat - Received message: '{}'", request.getMessage());
        AdminCopilotChatResponse response = adminCopilotService.chatWithCopilot(request);
        return ResponseEntity.ok(response);
    }
}
