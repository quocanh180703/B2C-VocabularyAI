package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCopilotChatResponse {

    /**
     * Markdown-formatted response text in Vietnamese
     */
    private String reply;

    /**
     * Optional visual widget type: 'cost_summary', 'vocab_audit', 'revenue_summary', 'sla_status', or 'none'
     */
    private String widgetType;

    /**
     * Dynamic structured data payload for the widget
     */
    private Map<String, Object> widgetData;

    /**
     * Follow-up questions/actions suggested to the admin
     */
    private List<String> suggestedActions;
}
