package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.AdminCopilotChatRequest;
import com.vocabularyai.admin.dto.response.AdminCopilotChatResponse;

public interface AdminCopilotService {

    /**
     * Process natural language query from admin with safe context injection and intelligent synthesis
     * 
     * @param request User's prompt and conversation history
     * @return AI reply with analysis, optional visual widget, and suggestions
     */
    AdminCopilotChatResponse chatWithCopilot(AdminCopilotChatRequest request);
}
