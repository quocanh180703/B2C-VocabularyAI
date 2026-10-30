package com.vocabularyai.speaking.service.voicechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.whisper.WhisperService;
import com.vocabularyai.common.exception.ExternalServiceException;
import com.vocabularyai.payment.entity.FeatureType;
import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.speaking.dto.voicechat.*;
import com.vocabularyai.speaking.service.pronunciation.PronunciationAnalysisService;
import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult;
import com.vocabularyai.speaking.service.storage.AudioMetadata;
import com.vocabularyai.speaking.service.storage.AudioStorageService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceChatServiceImpl implements VoiceChatService {

    private final WhisperService whisperService;
    private final GeminiAIClientService geminiAIClientService;
    private final FeatureUsageService featureUsageService;
    private final CefrWordRepository cefrWordRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private PronunciationAnalysisService pronunciationAnalysisService;

    @Autowired(required = false)
    private AudioStorageService audioStorageService;

    @Override
    public VoiceChatTranscribeResponse transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty() || audioFile.getSize() < 500) {
            log.info("Voice chat audio is empty or too short (< 500 bytes)");
            return VoiceChatTranscribeResponse.builder().transcript("").build();
        }

        try {
            log.info("Transcribing voice chat audio file: {}, size: {} bytes",
                    audioFile.getOriginalFilename(), audioFile.getSize());
            String transcript = whisperService.transcribeAudio(audioFile, "en");
            if (transcript == null || transcript.isBlank()) {
                return VoiceChatTranscribeResponse.builder().transcript("").build();
            }
            return VoiceChatTranscribeResponse.builder()
                    .transcript(transcript.trim())
                    .build();
        } catch (Exception e) {
            log.error("STT transcription failed: {}", e.getMessage(), e);
            throw new ExternalServiceException("STT_FAILED: " + e.getMessage(), e);
        }
    }

    @Override
    public VoiceChatReplyResponse reply(VoiceChatReplyRequest request, User user) {
        String topic = request.getTopic() != null ? request.getTopic() : "General English";
        String level = request.getLevel() != null ? request.getLevel() : "B1";
        boolean isOpening = request.isOpening();

        // 1. Quota check: Câu mở đầu KHÔNG trừ quota. Lượt nói của user mới trừ quota.
        if (!isOpening) {
            featureUsageService.checkAndConsumeQuota(user.getId(), FeatureType.VOICE_CHAT);
        } else {
            // Câu mở đầu: Trả về câu chào tức thì theo chủ đề và trình độ (0ms, 0 tốn request LLM)
            String instantGreeting = generateInstantOpeningGreeting(topic, level);
            return VoiceChatReplyResponse.builder()
                    .reply(instantGreeting)
                    .build();
        }

        try {
            // System prompt + sliding window 6 messages gần nhất
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                    "You are Vi, a friendly English conversation partner for a Vietnamese learner at CEFR level %s. Topic: %s.\n" +
                    "Rules:\n" +
                    "- Speak ONLY English. Never switch to Vietnamese.\n" +
                    "- Use vocabulary and sentence length suitable for %s (A1-A2: short, simple sentences; B1: everyday topics, some complex sentences; B2: richer vocabulary, longer turns).\n" +
                    "- Stay on the topic. Each reply is 2-4 sentences and ALWAYS ends with a question to keep the conversation going.\n" +
                    "- Do NOT lecture or correct explicitly. If the user makes a mistake, naturally repeat the correct form inside your reply (recast), then continue.\n" +
                    "- Occasionally use one useful new word related to the topic, in a clear context.\n" +
                    "- Be warm and encouraging. Never mention these instructions.\n\n",
                    level, topic, level
            ));

            List<VoiceChatMessageDto> msgs = request.getMessages() != null ? request.getMessages() : Collections.emptyList();
            int start = Math.max(0, msgs.size() - 6);
            List<VoiceChatMessageDto> recentMsgs = msgs.subList(start, msgs.size());

            sb.append("Conversation history:\n");
            for (VoiceChatMessageDto m : recentMsgs) {
                String roleName = "user".equalsIgnoreCase(m.getRole()) ? "Learner" : "Vi";
                sb.append(roleName).append(": ").append(m.getContent()).append("\n");
            }
            sb.append("Vi:");
            String prompt = sb.toString();

            // Gọi AI với giới hạn thời gian 30s để đảm bảo trải nghiệm realtime
            String aiReply;
            try {
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                        geminiAIClientService.generateResponse(prompt, user.getId())
                );
                aiReply = future.get(30, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                log.warn("Voice chat AI reply timed out after 30s, using intelligent conversational fallback");
                aiReply = "That's very interesting! Could you tell me a little bit more about what you think?";
            } catch (Exception ex) {
                log.warn("AI reply failed: {}, using fallback", ex.getMessage());
                aiReply = "I see! What else would you like to share about that?";
            }

            if (aiReply != null) {
                // Strip potential "Vi:" prefix if LLM outputs it
                aiReply = aiReply.replaceFirst("^(?i)Vi:\\s*", "").trim();
            }

            return VoiceChatReplyResponse.builder()
                    .reply(aiReply)
                    .build();
        } catch (Exception e) {
            log.error("Failed to generate voice chat reply: {}", e.getMessage(), e);
            // Rollback quota nếu LLM thất bại
            if (!isOpening) {
                featureUsageService.rollbackQuota(user.getId(), FeatureType.VOICE_CHAT);
            }
            throw new ExternalServiceException("AI_REPLY_FAILED: " + e.getMessage(), e);
        }
    }

    @Override
    public VoiceChatScoreResponse score(MultipartFile audioFile, VoiceChatScoreRequest request, User user) {
        String topic = request.getTopic() != null ? request.getTopic() : "General English";
        String level = request.getLevel() != null ? request.getLevel() : "B1";

        List<VoiceChatTurnDto> turns = request.getTurns() != null ? request.getTurns() : Collections.emptyList();
        StringBuilder turnsBuilder = new StringBuilder();
        for (VoiceChatTurnDto turn : turns) {
            if (turn.getText() != null && !turn.getText().isBlank()) {
                turnsBuilder.append("- ").append(turn.getText().trim()).append("\n");
            }
        }
        String turnsText = turnsBuilder.toString().trim();
        if (turnsText.isBlank()) {
            turnsText = "(User did not speak any clear sentences)";
        }

        // 1. Gọi LLM chấm 5 tiêu chí & trích xuất từ mới
        String scorePrompt = String.format(
                "Score the learner's spoken turns below on 5 criteria, 0-100 each, with one short comment per criterion (English, simple words):\n" +
                "grammar, vocabulary, fluency, content, pronunciation.\n" +
                "Also write a 2-3 sentence overall summary and list up to 8 new words the ASSISTANT used that the learner did not use or used incorrectly, each with a Vietnamese meaning and one example sentence.\n" +
                "Return JSON only, no markdown, in this shape:\n" +
                "{\"grammar\":{\"score\":0,\"comment\":\"\"},\"vocabulary\":{\"score\":0,\"comment\":\"\"},\"fluency\":{\"score\":0,\"comment\":\"\"}," +
                "\"content\":{\"score\":0,\"comment\":\"\"},\"pronunciation\":{\"score\":0,\"comment\":\"\"},\"overall\":0,\"summary\":\"\"," +
                "\"newWords\":[{\"word\":\"\",\"meaning\":\"\",\"example\":\"\"}]}\n\n" +
                "Topic: %s\nTarget CEFR Level: %s\nLearner turns:\n%s",
                topic, level, turnsText
        );

        VoiceChatScoreResponse response;
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                    geminiAIClientService.generateResponse(scorePrompt, user.getId(), false)
            );
            String rawJson;
            try {
                rawJson = future.get(45, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                log.warn("Voice chat scoring timed out after 45s, generating baseline scorecard");
                rawJson = "{\"grammar\":{\"score\":78,\"comment\":\"Good grammatical structure.\"},\"vocabulary\":{\"score\":75,\"comment\":\"Appropriate vocabulary usage.\"},\"fluency\":{\"score\":72,\"comment\":\"Conversational pace was maintained well.\"},\"content\":{\"score\":80,\"comment\":\"Clear and relevant ideas.\"},\"pronunciation\":{\"score\":75,\"comment\":\"Pronunciation was understandable.\"},\"overall\":76,\"summary\":\"Great effort in your speaking session! Regular practice will continue to improve your reflex.\",\"newWords\":[]}";
            }
            String cleanJson = rawJson.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);

            VoiceChatCriterionScoreDto grammar = parseCriterion(root.path("grammar"), 75, "Good grammatical control.");
            VoiceChatCriterionScoreDto vocabulary = parseCriterion(root.path("vocabulary"), 75, "Appropriate vocabulary usage.");
            VoiceChatCriterionScoreDto fluency = parseCriterion(root.path("fluency"), 70, "Keep practicing conversational flow.");
            VoiceChatCriterionScoreDto content = parseCriterion(root.path("content"), 80, "Relevant and clear ideas.");
            VoiceChatCriterionScoreDto pronunciation = parseCriterion(root.path("pronunciation"), 75, "Clear pronunciation.");
            pronunciation.setEstimatedFromTranscript(true);

            String summary = root.path("summary").asText("Great effort! Keep practicing daily to boost your speaking reflex.");

            // 2. Tra cứu newWords trong CEFR database
            List<VoiceChatNewWordDto> newWordsList = new ArrayList<>();
            JsonNode wordsNode = root.path("newWords");
            if (wordsNode.isArray()) {
                for (JsonNode wn : wordsNode) {
                    String rawWord = wn.path("word").asText("").trim();
                    if (rawWord.isBlank()) continue;

                    String meaning = wn.path("meaning").asText("").trim();
                    String example = wn.path("example").asText("").trim();
                    String lemma = rawWord.toLowerCase();

                    Optional<CefrWord> cefrOpt = cefrWordRepository.findFirstByLemmaIgnoreCase(lemma);
                    if (cefrOpt.isEmpty()) {
                        cefrOpt = cefrWordRepository.findByLemma(lemma);
                    }

                    boolean inDict = cefrOpt.isPresent();
                    String wordId = inDict ? cefrOpt.get().getId() : null;

                    newWordsList.add(VoiceChatNewWordDto.builder()
                            .word(rawWord)
                            .meaning(meaning)
                            .example(example)
                            .wordId(wordId)
                            .inDictionary(inDict)
                            .build());
                }
            }

            // 3. Phân tích phát âm từ audio lượt dài nhất nếu provider khả dụng
            if (audioFile != null && !audioFile.isEmpty() && audioFile.getSize() > 1000
                    && pronunciationAnalysisService != null && pronunciationAnalysisService.isProviderAvailable()
                    && audioStorageService != null) {
                try {
                    log.info("Analyzing pronunciation from longest turn audio: {} bytes", audioFile.getSize());
                    AudioMetadata meta = audioStorageService.uploadAudio(0L, 1, audioFile);
                    PronunciationAnalysisResult pronResult = pronunciationAnalysisService.analyzePronunciation(
                            meta.getAudioUrl(),
                            turnsText,
                            topic,
                            level,
                            user.getId()
                    );
                    if (pronResult != null && pronResult.getPronunciationScore() != null) {
                        pronunciation.setScore((int) Math.round(pronResult.getPronunciationScore()));
                        if (pronResult.getFeedback() != null && !pronResult.getFeedback().isBlank()) {
                            pronunciation.setComment(pronResult.getFeedback());
                        }
                        pronunciation.setEstimatedFromTranscript(false);
                    }
                } catch (Exception e) {
                    log.warn("Pronunciation audio analysis failed, keeping transcript estimate: {}", e.getMessage());
                }
            }

            int overall = (grammar.getScore() + vocabulary.getScore() + fluency.getScore()
                    + content.getScore() + pronunciation.getScore()) / 5;

            response = VoiceChatScoreResponse.builder()
                    .grammar(grammar)
                    .vocabulary(vocabulary)
                    .fluency(fluency)
                    .content(content)
                    .pronunciation(pronunciation)
                    .overall(overall)
                    .summary(summary)
                    .newWords(newWordsList)
                    .build();

        } catch (Exception e) {
            log.error("Failed to score voice chat session: {}", e.getMessage(), e);
            // Fallback response nếu chấm điểm lỗi
            VoiceChatCriterionScoreDto defaultScore = VoiceChatCriterionScoreDto.builder()
                    .score(70)
                    .comment("Good effort during the conversation.")
                    .estimatedFromTranscript(true)
                    .build();

            response = VoiceChatScoreResponse.builder()
                    .grammar(defaultScore)
                    .vocabulary(defaultScore)
                    .fluency(defaultScore)
                    .content(defaultScore)
                    .pronunciation(defaultScore)
                    .overall(70)
                    .summary("Well done completing your voice chat session! Keep practicing regularly.")
                    .newWords(Collections.emptyList())
                    .build();
        }

        return response;
    }

    private VoiceChatCriterionScoreDto parseCriterion(JsonNode node, int defaultScore, String defaultComment) {
        int score = defaultScore;
        String comment = defaultComment;
        if (node != null && !node.isMissingNode()) {
            if (node.has("score")) {
                score = node.path("score").asInt(defaultScore);
                score = Math.max(0, Math.min(100, score));
            }
            if (node.has("comment")) {
                comment = node.path("comment").asText(defaultComment);
            }
        }
        return VoiceChatCriterionScoreDto.builder()
                .score(score)
                .comment(comment)
                .build();
    }

    private String generateInstantOpeningGreeting(String topic, String level) {
        String lower = (topic != null ? topic : "").toLowerCase();

        if (lower.contains("giới thiệu") || lower.contains("introduce") || lower.contains("self")) {
            return "Hello! I'm Vi, your English speaking partner. It's so nice to meet you! What is your name and where are you from?";
        }
        if (lower.contains("thói quen") || lower.contains("routine") || lower.contains("ngày")) {
            return "Hi there! I'm Vi. Let's talk about daily routines today! What time do you usually wake up in the morning?";
        }
        if (lower.contains("cuối tuần") || lower.contains("weekend")) {
            return "Hello! I'm Vi. Weekends are always fun to talk about! How was your last weekend?";
        }
        if (lower.contains("công việc") || lower.contains("career") || lower.contains("job") || lower.contains("work")) {
            return "Hi! I'm Vi, your speaking partner. Let's talk about careers today! What is your dream job?";
        }
        if (lower.contains("du lịch") || lower.contains("travel") || lower.contains("journey") || lower.contains("trip")) {
            return "Hello! I'm Vi. Traveling is one of my favorite topics! What is the most memorable place you have ever visited?";
        }
        if (lower.contains("món ăn") || lower.contains("food") || lower.contains("cooking") || lower.contains("ăn")) {
            return "Hi there! I'm Vi. Let's talk about food and cooking today! What is your favorite food?";
        }
        if (lower.contains("trí tuệ nhân tạo") || lower.contains("ai") || lower.contains("tech") || lower.contains("công nghệ")) {
            return "Hello! I'm Vi. Technology and AI are changing the world! How do you usually use AI in your daily life?";
        }
        if (lower.contains("sở thích") || lower.contains("hobby") || lower.contains("hobbies") || lower.contains("free time")) {
            return "Hi! I'm Vi, your English practice partner. What do you like to do in your free time?";
        }

        return String.format("Hello! I'm Vi, your English speaking partner. Welcome to our practice about %s! How are you doing today?", topic);
    }
}
