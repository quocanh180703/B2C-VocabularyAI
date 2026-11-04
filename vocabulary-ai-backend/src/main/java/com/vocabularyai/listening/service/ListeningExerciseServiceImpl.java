package com.vocabularyai.listening.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.listening.dto.*;
import com.vocabularyai.listening.entity.ListeningExercise;
import com.vocabularyai.listening.repository.ListeningExerciseRepository;
import com.vocabularyai.payment.entity.FeatureType;
import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.speaking.service.tts.XahTextToSpeechService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningExerciseServiceImpl implements ListeningExerciseService {

    private final ListeningExerciseRepository repository;
    private final GeminiAIClientService geminiAIClientService;
    private final XahTextToSpeechService xahTextToSpeechService;
    private final FeatureUsageService featureUsageService;
    private final ObjectMapper objectMapper;

    @Value("${speaking.storage.local-path:/tmp/vocabulary-ai/audio}")
    private String basePath;

    private static final Set<String> ALLOWED_LEVELS = new HashSet<>(Arrays.asList("A1", "A2", "B1", "B2", "C1"));
    private static final Set<String> ALLOWED_VOICES = new HashSet<>(Arrays.asList("alloy", "echo", "fable", "onyx", "nova", "coral"));

    @Override
    @Transactional
    public CreateListeningResponse createExercise(Long userId, CreateListeningRequest request) {
        if (request == null || request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            throw new IllegalArgumentException("Chủ đề không được để trống");
        }

        String rawLevel = request.getLevel() != null ? request.getLevel().trim().toUpperCase(Locale.ROOT) : "B1";
        String level = ALLOWED_LEVELS.contains(rawLevel) ? rawLevel : "B1";

        String rawVoice = request.getVoice() != null ? request.getVoice().trim().toLowerCase(Locale.ROOT) : "alloy";
        String voice = ALLOWED_VOICES.contains(rawVoice) ? rawVoice : "alloy";

        String topic = request.getTopic().trim();

        log.info("Creating listening exercise - userId: {}, topic: '{}', level: {}, voice: {}",
                userId, topic, level, voice);

        // 1. Quota check & consume
        featureUsageService.checkAndConsumeQuota(userId, FeatureType.LISTENING);

        String passageText;
        byte[] audioBytes;
        Path audioFilePath = null;
        ListeningExercise exercise;

        try {
            // 2. Build prompt & generate passage via Gemini
            String prompt = buildPrompt(topic, level);
            passageText = geminiAIClientService.generateResponse(prompt, userId, false);

            if (passageText == null || passageText.trim().isEmpty()) {
                throw new IllegalStateException("Gemini trả về nội dung rỗng");
            }

            // Sanitize text: remove markdown code blocks or quotes
            passageText = sanitizePassage(passageText);

            // 3. Synthesize speech via TTS Grok
            audioBytes = xahTextToSpeechService.synthesizeRawAudio(passageText, voice);
            if (audioBytes == null || audioBytes.length == 0) {
                throw new IllegalStateException("Không thể tạo file âm thanh cho bài nghe");
            }

            // 4. Save MP3 audio to local disk
            Path listeningDir = Paths.get(basePath, "listening");
            Files.createDirectories(listeningDir);

            String filename = String.format("listening_%d_%d_%s.mp3",
                    userId, System.currentTimeMillis(), UUID.randomUUID().toString().substring(0, 8));
            audioFilePath = listeningDir.resolve(filename);
            Files.write(audioFilePath, audioBytes);

            // 5. Save exercise entity to database (inside try block for rollback & cleanup on failure)
            exercise = ListeningExercise.builder()
                    .userId(userId)
                    .topic(topic)
                    .level(level)
                    .voice(voice)
                    .passageText(passageText)
                    .audioPath(audioFilePath.toAbsolutePath().toString())
                    .status("CREATED")
                    .build();

            exercise = repository.save(exercise);
            log.info("Listening exercise created successfully - id: {}", exercise.getId());

        } catch (Exception e) {
            log.error("Failed to create listening exercise for user {}. Rolling back quota.", userId, e);
            featureUsageService.rollbackQuota(userId, FeatureType.LISTENING);
            if (audioFilePath != null) {
                try {
                    Files.deleteIfExists(audioFilePath);
                } catch (Exception ignored) {
                    // Do not mask original error
                }
            }
            throw new RuntimeException("Tạo bài luyện nghe thất bại: " + e.getMessage(), e);
        }

        // 6. Return response WITHOUT passageText (anti-cheat)
        return CreateListeningResponse.builder()
                .exerciseId(exercise.getId())
                .topic(exercise.getTopic())
                .level(exercise.getLevel())
                .voice(exercise.getVoice())
                .audioUrl("/api/ai/listening/audio/" + exercise.getId() + "/stream")
                .build();
    }

    @Override
    public byte[] getAudioBytes(Long exerciseId, Long userId) {
        ListeningExercise exercise = repository.findByIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("ListeningExercise", "id", exerciseId));

        try {
            Path path = Paths.get(exercise.getAudioPath());
            if (!Files.exists(path)) {
                log.warn("Audio file not found on disk: {}", exercise.getAudioPath());
                // Re-synthesize if missing
                byte[] audio = xahTextToSpeechService.synthesizeRawAudio(exercise.getPassageText(), exercise.getVoice());
                Files.createDirectories(path.getParent());
                Files.write(path, audio);
                return audio;
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Error reading audio file for exercise {}", exerciseId, e);
            throw new RuntimeException("Không thể đọc file audio bài nghe", e);
        }
    }

    @Override
    @Transactional
    public ListeningSubmitResponse submitExercise(Long exerciseId, Long userId, ListeningSubmitRequest request) {
        ListeningExercise exercise = repository.findByIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("ListeningExercise", "id", exerciseId));

        if ("COMPLETED".equalsIgnoreCase(exercise.getStatus())) {
            throw new DuplicateResourceException("Bài nghe này đã được chấm điểm");
        }

        String typedText = request != null && request.getTypedText() != null ? request.getTypedText().trim() : "";

        // Deterministic word matching evaluation (no LLM call)
        ListeningScoringHelper.ScoringResult result = ListeningScoringHelper.evaluate(
                exercise.getPassageText(),
                typedText
        );

        String wordResultsJson;
        try {
            wordResultsJson = objectMapper.writeValueAsString(result.wordResults);
        } catch (Exception e) {
            log.error("Failed to serialize word results to JSON", e);
            wordResultsJson = "[]";
        }

        exercise.setUserAnswer(typedText);
        exercise.setScore(result.score);
        exercise.setWordResults(wordResultsJson);
        exercise.setStatus("COMPLETED");
        exercise.setCompletedAt(LocalDateTime.now());

        exercise = repository.save(exercise);

        log.info("Listening exercise submitted - id: {}, userId: {}, score: {}",
                exerciseId, userId, result.score);

        return ListeningSubmitResponse.builder()
                .exerciseId(exercise.getId())
                .score(result.score)
                .totalWords(result.totalOriginalWords)
                .correctWords(result.correctWords)
                .nearMissWords(result.nearMissWords)
                .wordResults(result.wordResults)
                .transcript(exercise.getPassageText())
                .userAnswer(typedText)
                .completedAt(exercise.getCompletedAt())
                .build();
    }

    @Override
    public ListeningSubmitResponse getExerciseDetail(Long exerciseId, Long userId) {
        ListeningExercise exercise = repository.findByIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("ListeningExercise", "id", exerciseId));

        List<WordMatchResult> results = Collections.emptyList();
        if (exercise.getWordResults() != null && !exercise.getWordResults().isEmpty()) {
            try {
                results = objectMapper.readValue(
                        exercise.getWordResults(),
                        new TypeReference<List<WordMatchResult>>() {}
                );
            } catch (Exception e) {
                log.error("Failed to parse word results JSON for exercise {}", exerciseId, e);
            }
        }

        boolean isCompleted = "COMPLETED".equalsIgnoreCase(exercise.getStatus());

        return ListeningSubmitResponse.builder()
                .exerciseId(exercise.getId())
                .score(exercise.getScore())
                .totalWords(results.size())
                .correctWords((int) results.stream().filter(w -> "CORRECT".equalsIgnoreCase(w.getStatus())).count())
                .nearMissWords((int) results.stream().filter(w -> "NEAR_MISS".equalsIgnoreCase(w.getStatus())).count())
                .wordResults(results)
                .transcript(isCompleted ? exercise.getPassageText() : null) // Strictly hide before completed
                .userAnswer(exercise.getUserAnswer())
                .completedAt(exercise.getCompletedAt())
                .build();
    }

    @Override
    public Page<ListeningHistoryItemResponse> getExerciseHistory(Long userId, Pageable pageable) {
        Page<ListeningExercise> page = repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return page.map(e -> ListeningHistoryItemResponse.builder()
                .id(e.getId())
                .topic(e.getTopic())
                .level(e.getLevel())
                .voice(e.getVoice())
                .score(e.getScore())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .completedAt(e.getCompletedAt())
                .build());
    }

    private String buildPrompt(String topic, String level) {
        String constraints;
        switch (level) {
            case "A1":
                constraints = "2-3 short, simple sentences, total length between 25 and 35 words. Use simple present tense, basic everyday CEFR A1 words.";
                break;
            case "A2":
                constraints = "3-4 sentences, total length between 40 and 55 words. Everyday situations, simple past and present tenses, basic conjunctions.";
                break;
            case "B1":
                constraints = "4-5 sentences, total length between 60 and 80 words. Coherent narrative or explanation, moderate vocabulary, varied sentence structures.";
                break;
            case "B2":
                constraints = "5-6 sentences, total length between 85 and 110 words. More complex sentence structures, diverse vocabulary, cultural or professional context.";
                break;
            case "C1":
            default:
                constraints = "6-7 sentences, total length between 115 and 140 words. Sophisticated vocabulary, academic or nuanced expressions, natural idiomatic phrasing.";
                break;
        }

        return String.format(
                "You are an expert English language assessor. Write a natural, coherent English listening comprehension paragraph about the topic: \"%s\" for CEFR level %s.\n\n" +
                "Requirements:\n" +
                "- %s\n" +
                "- Style: Natural spoken English or clear monologue suitable for listening dictation.\n" +
                "- For A1-A2 levels: do not use numbers, dates, or years. From B1 and above, if numbers are used, write them out in words (e.g. \"twenty\", not \"20\").\n" +
                "- Do not use rare or difficult-to-spell proper names; only use very common names (e.g. John, London) or avoid proper names altogether.\n" +
                "- DO NOT include any title, label, preamble, markdown formatting, quotation marks, or explanations.\n" +
                "- Output ONLY the plain English paragraph text.",
                topic, level, constraints
        );
    }

    private String sanitizePassage(String raw) {
        String cleaned = raw.trim();
        // Remove code block markdown if present
        if (cleaned.startsWith("```") && cleaned.endsWith("```")) {
            cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
        }
        // Remove surrounding quotes if present
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
            (cleaned.startsWith("“") && cleaned.endsWith("”"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        // Remove any introductory boilerplate like "Here is the paragraph:"
        if (cleaned.toLowerCase(Locale.ROOT).startsWith("here is") && cleaned.contains("\n")) {
            cleaned = cleaned.substring(cleaned.indexOf("\n")).trim();
        }
        return cleaned;
    }
}

