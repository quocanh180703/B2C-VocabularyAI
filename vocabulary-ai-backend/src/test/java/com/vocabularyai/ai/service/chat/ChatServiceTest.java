package com.vocabularyai.ai.service.chat;

import com.vocabularyai.ai.repository.AiConversationRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@DisplayName("ChatService Tests")
public class ChatServiceTest {
    
    @MockBean
    private AiConversationRepository conversationRepository;
}
