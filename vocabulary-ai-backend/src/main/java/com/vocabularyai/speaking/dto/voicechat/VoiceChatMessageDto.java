package com.vocabularyai.speaking.dto.voicechat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceChatMessageDto {
    private String role; // "user" or "assistant"
    private String content;
}
