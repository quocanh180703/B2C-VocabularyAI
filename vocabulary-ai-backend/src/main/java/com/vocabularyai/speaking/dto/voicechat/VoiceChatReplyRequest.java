package com.vocabularyai.speaking.dto.voicechat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceChatReplyRequest {
    private String topic;
    private String level;
    private List<VoiceChatMessageDto> messages;
    private boolean opening;
}
