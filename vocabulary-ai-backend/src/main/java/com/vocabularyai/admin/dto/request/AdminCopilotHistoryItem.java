package com.vocabularyai.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCopilotHistoryItem {
    private String sender; // "user" or "ai"
    private String text;
}
