package com.vocabularyai.document.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChatRequest {

    @NotEmpty(message = "Danh sách tin nhắn không được rỗng")
    private List<ChatMessageItem> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageItem {
        private String role; // "user" or "model" / "assistant"
        private String content;
    }
}
