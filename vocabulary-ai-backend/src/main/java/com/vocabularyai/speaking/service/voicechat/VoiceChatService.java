package com.vocabularyai.speaking.service.voicechat;

import com.vocabularyai.speaking.dto.voicechat.*;
import com.vocabularyai.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface VoiceChatService {

    /**
     * Chuyển audio (lượt nói của user) thành văn bản qua Whisper STT
     */
    VoiceChatTranscribeResponse transcribe(MultipartFile audioFile);

    /**
     * Nhận phản hồi hội thoại từ AI (Gemini Flash)
     */
    VoiceChatReplyResponse reply(VoiceChatReplyRequest request, User user);

    /**
     * Chấm điểm 5 tiêu chí + trích xuất từ mới sau khi kết thúc phiên
     */
    VoiceChatScoreResponse score(MultipartFile audioFile, VoiceChatScoreRequest request, User user);
}
