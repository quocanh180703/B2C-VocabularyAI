package com.vocabularyai.admin.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.admin.dto.request.AiVocabApproveRequest;
import com.vocabularyai.admin.dto.request.AiVocabTagApproveItem;
import com.vocabularyai.admin.dto.request.AiVocabTagApproveRequest;
import com.vocabularyai.admin.dto.request.AiVocabTagBackfillRequest;
import com.vocabularyai.admin.dto.response.*;
import com.vocabularyai.admin.entity.TopicTagQueue;
import com.vocabularyai.admin.repository.TopicTagQueueRepository;
import com.vocabularyai.admin.service.AdminAiVocabularyService;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.entity.PartOfSpeech;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.entity.WordTopic;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import com.vocabularyai.vocabulary.repository.WordRepository;
import com.vocabularyai.vocabulary.service.CefrWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAiVocabularyServiceImpl implements AdminAiVocabularyService {

    private final CefrWordRepository cefrWordRepository;
    private final WordRepository wordRepository;
    private final GeminiAIClientService geminiAIClientService;
    private final ObjectMapper objectMapper;
    private final TopicTagQueueRepository topicTagQueueRepository;
    private final CefrWordService cefrWordService;

    private final Map<String, AiVocabTagBackfillStatusResponse> backfillJobs = new ConcurrentHashMap<>();
    private final AtomicBoolean isBackfillRunning = new AtomicBoolean(false);

    @Override
    @Transactional(readOnly = true)
    public AiVocabAuditResponse auditQuality() {
        long total = cefrWordRepository.count();
        if (total == 0) {
            return AiVocabAuditResponse.builder()
                    .totalWords(0)
                    .wordsMissingMeaning(0)
                    .wordsMissingExample(0)
                    .wordsMissingPronunciation(0)
                    .wordsMissingDefinition(0)
                    .completenessRate(100.0)
                    .build();
        }

        long missingMeaning = cefrWordRepository.countWordsMissingMeaning();
        long missingExample = cefrWordRepository.countWordsMissingExample();
        long missingPronunciation = cefrWordRepository.countWordsMissingPronunciation();
        long missingDefinition = cefrWordRepository.countWordsMissingDefinition();

        long totalFields = total * 4;
        long totalMissingFields = missingMeaning + missingExample + missingPronunciation + missingDefinition;
        double completePct = ((double) (totalFields - totalMissingFields) / totalFields) * 100.0;
        double roundedRate = Math.round(completePct * 10.0) / 10.0;

        return AiVocabAuditResponse.builder()
                .totalWords(total)
                .wordsMissingMeaning(missingMeaning)
                .wordsMissingExample(missingExample)
                .wordsMissingPronunciation(missingPronunciation)
                .wordsMissingDefinition(missingDefinition)
                .completenessRate(roundedRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiMissingWordDto> getWordsNeedingEnrichment(String filterType, int limit) {
        int boundedLimit = Math.min(Math.max(limit, 1), 30);
        PageRequest pr = PageRequest.of(0, boundedLimit);

        Page<CefrWord> page;
        if ("meaning".equalsIgnoreCase(filterType)) {
            page = cefrWordRepository.findWordsMissingMeaning(pr);
        } else if ("pronunciation".equalsIgnoreCase(filterType)) {
            page = cefrWordRepository.findWordsMissingPronunciation(pr);
        } else if ("definition".equalsIgnoreCase(filterType)) {
            page = cefrWordRepository.findWordsMissingDefinition(pr);
        } else {
            page = cefrWordRepository.findWordsMissingExample(pr);
        }

        return page.getContent().stream()
                .map(this::mapCefrToMissingWordDto)
                .toList();
    }

    private AiMissingWordDto mapCefrToMissingWordDto(CefrWord cw) {
        String posStr = cw.getPartOfSpeech() != null ? cw.getPartOfSpeech().name() : "NOUN";
        String lvlStr = cw.getCefrLevel() != null ? cw.getCefrLevel().name() : "B1";

        return AiMissingWordDto.builder()
                .id(cw.getId())
                .word(cw.getLemma())
                .partOfSpeech(posStr)
                .level(lvlStr)
                .meaning(cw.getVietnameseMeaning())
                .pronunciation(cw.getPronunciation())
                .definition(cw.getDefinition())
                .example(cw.getExample())
                .build();
    }

    @Override
    public AiGeneratedWordDto generateWordContent(String word, String levelHint) {
        if (word == null || word.trim().isEmpty()) {
            throw new IllegalArgumentException("Word cannot be empty");
        }
        String cleanWord = word.trim().toLowerCase();

        Optional<CefrWord> existingOpt = cefrWordRepository.findByLemma(cleanWord);
        String existingId = existingOpt.map(CefrWord::getId).orElse(null);
        boolean isDuplicate = existingOpt.isPresent();

        String promptLevel = levelHint;
        if (promptLevel == null || promptLevel.isBlank()) {
            promptLevel = existingOpt.map(cw -> cw.getCefrLevel() != null ? cw.getCefrLevel().name() : "B1").orElse("B1");
        }

        String prompt = buildPrompt(cleanWord, promptLevel);

        try {
            log.info("[AI VOCAB STUDIO] Requesting content generation for word: '{}' (levelHint={})", cleanWord, promptLevel);
            String rawJson = geminiAIClientService.generateResponse(prompt, null);
            AiGeneratedWordDto dto = parseGeminiResponse(rawJson, cleanWord);
            dto.setExistingWordId(existingId);
            dto.setDuplicate(isDuplicate);
            return dto;
        } catch (Exception e) {
            log.warn("[AI VOCAB STUDIO] AI call failed or returned invalid format: {}, using smart fallback", e.getMessage());
            return generateLocalFallback(cleanWord, promptLevel, existingId, isDuplicate);
        }
    }

    @Override
    public List<AiGeneratedWordDto> batchGenerateWords(List<String> words, String levelHint) {
        if (words == null || words.isEmpty()) {
            return Collections.emptyList();
        }

        List<AiGeneratedWordDto> results = new ArrayList<>();
        int count = 0;
        for (String w : words) {
            if (count >= 10) break;
            if (w != null && !w.trim().isEmpty()) {
                results.add(generateWordContent(w.trim(), levelHint));
                count++;
            }
        }
        return results;
    }

    @Override
    @Transactional
    public int approveAndSaveWords(AiVocabApproveRequest request) {
        if (request == null || request.getWords() == null || request.getWords().isEmpty()) {
            return 0;
        }

        int savedCount = 0;
        for (AiGeneratedWordDto dto : request.getWords()) {
            if (dto.getWord() == null || dto.getWord().trim().isEmpty()) continue;
            String cleanLemma = dto.getWord().trim().toLowerCase();

            CefrWord cefrToSave = null;

            if (dto.getExistingWordId() != null && !dto.getExistingWordId().trim().isEmpty()) {
                cefrToSave = cefrWordRepository.findById(dto.getExistingWordId().trim()).orElse(null);
            }

            if (cefrToSave == null) {
                cefrToSave = cefrWordRepository.findByLemma(cleanLemma).orElse(null);
            }

            if (cefrToSave != null) {
                if (request.isOverwriteExisting()) {
                    if (dto.getMeaning() != null && !dto.getMeaning().trim().isEmpty()) {
                        cefrToSave.setVietnameseMeaning(dto.getMeaning().trim());
                    }
                    if (dto.getPronunciation() != null && !dto.getPronunciation().trim().isEmpty()) {
                        cefrToSave.setPronunciation(dto.getPronunciation().trim());
                    }
                    if (dto.getPartOfSpeech() != null && !dto.getPartOfSpeech().trim().isEmpty()) {
                        PartOfSpeech pos = PartOfSpeech.fromString(dto.getPartOfSpeech().trim());
                        if (pos != null) cefrToSave.setPartOfSpeech(pos);
                    }
                    if (dto.getDefinition() != null && !dto.getDefinition().trim().isEmpty()) {
                        cefrToSave.setDefinition(dto.getDefinition().trim());
                    }
                    if (dto.getExample() != null && !dto.getExample().trim().isEmpty()) {
                        cefrToSave.setExample(dto.getExample().trim());
                    }
                    if (dto.getExampleTranslation() != null && !dto.getExampleTranslation().trim().isEmpty()) {
                        cefrToSave.setExampleTranslation(dto.getExampleTranslation().trim());
                    }
                    if (dto.getLevel() != null && !dto.getLevel().trim().isEmpty()) {
                        CEFRLevel lvl = CEFRLevel.fromString(dto.getLevel().trim());
                        if (lvl != null) cefrToSave.setCefrLevel(lvl);
                    }
                    cefrToSave.setUpdatedAt(LocalDateTime.now());
                    cefrWordRepository.save(cefrToSave);
                    savedCount++;
                    log.info("[AI VOCAB STUDIO] Updated CEFR word '{}' (id: {})", cefrToSave.getLemma(), cefrToSave.getId());
                }
            } else {
                CEFRLevel lvl = CEFRLevel.fromString(dto.getLevel());
                if (lvl == null) lvl = CEFRLevel.B1;
                PartOfSpeech pos = PartOfSpeech.fromString(dto.getPartOfSpeech());

                CefrWord newCefrWord = CefrWord.builder()
                        .id(UUID.randomUUID().toString())
                        .lemma(cleanLemma)
                        .language("en")
                        .cefrLevel(lvl)
                        .partOfSpeech(pos)
                        .pronunciation(dto.getPronunciation())
                        .definition(dto.getDefinition())
                        .vietnameseMeaning(dto.getMeaning())
                        .example(dto.getExample())
                        .exampleTranslation(dto.getExampleTranslation())
                        .source("gemini_ai_studio")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                cefrWordRepository.save(newCefrWord);
                savedCount++;
                log.info("[AI VOCAB STUDIO] Inserted new CEFR word '{}' (id: {})", newCefrWord.getLemma(), newCefrWord.getId());
            }

            try {
                Optional<Word> optWord = wordRepository.findByWord(cleanLemma);
                Word wordEntity = optWord.orElseGet(() -> Word.builder()
                        .word(cleanLemma)
                        .build());
                if (dto.getMeaning() != null && !dto.getMeaning().trim().isEmpty()) wordEntity.setMeaning(dto.getMeaning().trim());
                if (dto.getPronunciation() != null && !dto.getPronunciation().trim().isEmpty()) {
                    wordEntity.setPronunciation(dto.getPronunciation().trim());
                    wordEntity.setPhonetic(dto.getPronunciation().trim());
                }
                if (dto.getPartOfSpeech() != null && !dto.getPartOfSpeech().trim().isEmpty()) wordEntity.setPartOfSpeech(dto.getPartOfSpeech().trim().toUpperCase());
                if (dto.getDefinition() != null && !dto.getDefinition().trim().isEmpty()) wordEntity.setDefinition(dto.getDefinition().trim());
                if (dto.getExample() != null && !dto.getExample().trim().isEmpty()) wordEntity.setExample(dto.getExample().trim());
                if (dto.getLevel() != null && !dto.getLevel().trim().isEmpty()) wordEntity.setLevel(dto.getLevel().trim().toUpperCase());
                wordRepository.save(wordEntity);
            } catch (Exception syncEx) {
                log.warn("[AI VOCAB STUDIO] Auxiliary words table sync skipped: {}", syncEx.getMessage());
            }
        }

        if (savedCount > 0) {
            cefrWordService.evictCefrWordsCache();
        }

        return savedCount;
    }

    // ==================== Topic Tagging & Backfill Pipeline ====================

    @Override
    public List<AiVocabTagClassifyItemDto> classifyWordTopics(List<String> lemmas) {
        if (lemmas == null || lemmas.isEmpty()) {
            return Collections.emptyList();
        }

        List<CefrWord> wordsToClassify = new ArrayList<>();
        for (String rawLemma : lemmas) {
            if (rawLemma == null || rawLemma.trim().isEmpty()) continue;
            String clean = rawLemma.trim().toLowerCase();
            Optional<CefrWord> opt = cefrWordRepository.findByLemma(clean);
            if (opt.isPresent()) {
                wordsToClassify.add(opt.get());
            } else {
                wordsToClassify.add(CefrWord.builder()
                        .id(null)
                        .lemma(clean)
                        .language("en")
                        .build());
            }
        }

        if (wordsToClassify.isEmpty()) {
            return Collections.emptyList();
        }

        return classifyBatchWithGemini(wordsToClassify);
    }

    private boolean isFunctionalPos(PartOfSpeech pos) {
        if (pos == null) return false;
        return pos == PartOfSpeech.PREPOSITION ||
               pos == PartOfSpeech.CONJUNCTION ||
               pos == PartOfSpeech.PRONOUN ||
               pos == PartOfSpeech.DETERMINER ||
               pos == PartOfSpeech.INTERJECTION ||
               pos == PartOfSpeech.AUXILIARY;
    }

    private List<AiVocabTagClassifyItemDto> classifyBatchWithGemini(List<CefrWord> words) {
        List<AiVocabTagClassifyItemDto> results = new ArrayList<>();
        List<CefrWord> wordsForGemini = new ArrayList<>();

        // Rule 2.3: POS pre-filter for functional grammar words
        for (CefrWord w : words) {
            if (isFunctionalPos(w.getPartOfSpeech())) {
                results.add(AiVocabTagClassifyItemDto.builder()
                        .existingWordId(w.getId())
                        .word(w.getLemma())
                        .topic(null)
                        .confidence(0.0)
                        .reason("Từ chức năng / từ chung (luật từ loại: " + w.getPartOfSpeech().name() + ")")
                        .build());
            } else {
                wordsForGemini.add(w);
            }
        }

        if (wordsForGemini.isEmpty()) {
            log.info("[AI VOCAB STUDIO] All {} words pre-filtered as functional POS. Skipping Gemini call.", words.size());
            return results;
        }

        String prompt = buildTopicClassificationPrompt(wordsForGemini);
        try {
            log.info("[AI VOCAB STUDIO] Classifying topics for {} words via Gemini ({} functional words pre-filtered)",
                    wordsForGemini.size(), results.size());
            // Call Gemini without local conversational fallback (allowFallback = false)
            String rawJson = geminiAIClientService.generateResponse(prompt, null, false);
            List<AiVocabTagClassifyItemDto> aiResults = parseGeminiTopicResponse(rawJson, wordsForGemini);
            results.addAll(aiResults);
            return results;
        } catch (Exception e) {
            log.warn("[AI VOCAB STUDIO] Gemini topic classification failed: {}", e.getMessage());
            for (CefrWord w : wordsForGemini) {
                results.add(AiVocabTagClassifyItemDto.builder()
                        .existingWordId(w.getId())
                        .word(w.getLemma())
                        .topic(null)
                        .confidence(0.0)
                        .reason("Lỗi phân loại AI: " + e.getMessage())
                        .build());
            }
            return results;
        }
    }

    @Override
    @Transactional
    public int approveAndSaveTopics(AiVocabTagApproveRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return 0;
        }

        int updated = 0;
        for (AiVocabTagApproveItem item : request.getItems()) {
            if (item.getWordId() == null || item.getWordId().trim().isEmpty()) continue;
            WordTopic topic = WordTopic.fromString(item.getTopic());
            if (topic == null) continue;

            Optional<CefrWord> opt = cefrWordRepository.findById(item.getWordId().trim());
            if (opt.isPresent()) {
                CefrWord word = opt.get();
                if (word.getTopic() == null || request.isOverwrite()) {
                    word.setTopic(topic);
                    word.setUpdatedAt(LocalDateTime.now());
                    cefrWordRepository.save(word);
                    updated++;

                    Optional<TopicTagQueue> queueOpt = topicTagQueueRepository.findByCefrWordIdAndStatus(word.getId(), "PENDING");
                    if (queueOpt.isPresent()) {
                        TopicTagQueue q = queueOpt.get();
                        q.setStatus("APPROVED");
                        q.setSuggestedTopic(topic);
                        topicTagQueueRepository.save(q);
                    }
                }
            }
        }

        if (updated > 0) {
            cefrWordService.evictCefrWordsCache();
            log.info("[AI VOCAB STUDIO] Successfully approved and updated topics for {} words", updated);
        }
        return updated;
    }

    @Override
    public AiVocabTagBackfillStatusResponse startTopicBackfill(AiVocabTagBackfillRequest request) {
        if (isBackfillRunning.get()) {
            return backfillJobs.values().stream()
                    .filter(j -> "RUNNING".equals(j.getStatus()))
                    .findFirst()
                    .orElse(AiVocabTagBackfillStatusResponse.builder()
                            .jobId("current")
                            .status("RUNNING")
                            .message("Một tiến trình quét nền đang chạy. Vui lòng chờ.")
                            .build());
        }

        String scope = request != null && request.getScope() != null ? request.getScope().trim().toUpperCase() : "IDS";
        int totalMissing = 0;

        if ("IDS".equals(scope)) {
            List<String> ids = request != null && request.getWordIds() != null ? request.getWordIds() : Collections.emptyList();
            List<CefrWord> wordsToTag = ids.isEmpty() ? Collections.emptyList() : cefrWordRepository.findWordsMissingTopicByIds(ids);
            totalMissing = wordsToTag.size();
        } else if ("IMPORT".equals(scope)) {
            totalMissing = (int) cefrWordRepository.countWordsMissingTopicByImport(request.getCreatedAfter(), request.getSource());
        } else {
            totalMissing = (int) cefrWordRepository.countWordsMissingTopic();
        }

        String jobId = UUID.randomUUID().toString();
        String message = totalMissing == 0
                ? "Không tìm thấy từ nào còn thiếu chủ đề trong phạm vi đã chọn."
                : String.format("Bắt đầu gắn tag phạm vi [%s] cho %d từ...", scope, totalMissing);

        AiVocabTagBackfillStatusResponse initialStatus = AiVocabTagBackfillStatusResponse.builder()
                .jobId(jobId)
                .scope(scope)
                .total(totalMissing)
                .processed(0)
                .autoSaved(0)
                .queued(0)
                .skipped(0)
                .skippedIntentional(0)
                .failed(0)
                .lastError(null)
                .status(totalMissing == 0 ? "COMPLETED" : "RUNNING")
                .message(message)
                .build();

        backfillJobs.put(jobId, initialStatus);

        if (totalMissing > 0 && isBackfillRunning.compareAndSet(false, true)) {
            final AiVocabTagBackfillRequest finalReq = request != null ? request : AiVocabTagBackfillRequest.builder().scope("IDS").build();
            CompletableFuture.runAsync(() -> runBackfillProcess(jobId, finalReq));
        }

        return initialStatus;
    }

    private void runBackfillProcess(String jobId, AiVocabTagBackfillRequest request) {
        String scope = request.getScope() != null ? request.getScope().trim().toUpperCase() : "IDS";
        int batchSize = request.getBatchSize() != null ? request.getBatchSize() : 20;
        double threshold = request.getConfidenceThreshold() != null ? request.getConfidenceThreshold() : 0.8;
        int subBatchSize = (batchSize > 0 && batchSize <= 20) ? batchSize : 15;

        log.info("[AI VOCAB BACKFILL] Starting job {} - scope={}, batchSize={}, threshold={}", jobId, scope, batchSize, threshold);
        AiVocabTagBackfillStatusResponse state = backfillJobs.get(jobId);

        try {
            if ("IDS".equals(scope)) {
                List<String> ids = request.getWordIds() != null ? request.getWordIds() : Collections.emptyList();
                List<CefrWord> allWords = ids.isEmpty() ? Collections.emptyList() : cefrWordRepository.findWordsMissingTopicByIds(ids);

                for (int i = 0; i < allWords.size() && isBackfillRunning.get(); i += subBatchSize) {
                    List<CefrWord> batch = allWords.subList(i, Math.min(i + subBatchSize, allWords.size()));
                    processBatch(batch, state, threshold);
                    respectRateLimit();
                }
            } else if ("IMPORT".equals(scope)) {
                String lastId = null;
                while (isBackfillRunning.get()) {
                    List<CefrWord> words = cefrWordRepository.findWordsMissingTopicByImportAfterId(
                            request.getCreatedAfter(), request.getSource(), lastId, PageRequest.of(0, subBatchSize));
                    if (words.isEmpty()) break;
                    lastId = words.get(words.size() - 1).getId();
                    processBatch(words, state, threshold);
                    respectRateLimit();
                }
            } else {
                // Legacy / emergency ALL untagged scan
                String lastId = null;
                while (isBackfillRunning.get()) {
                    List<CefrWord> words = cefrWordRepository.findWordsMissingTopicAfterId(lastId, PageRequest.of(0, subBatchSize));
                    if (words.isEmpty()) break;
                    lastId = words.get(words.size() - 1).getId();
                    processBatch(words, state, threshold);
                    respectRateLimit();
                }
            }

            cefrWordService.evictCefrWordsCache();
            if (!"STOPPED".equals(state.getStatus())) {
                state.setStatus("COMPLETED");
                state.setMessage(String.format("Hoàn tất! Đã xử lý %d từ. Tự lưu: %d, Chờ duyệt: %d, Bỏ qua chủ đích: %d, Lỗi: %d.",
                        state.getProcessed(), state.getAutoSaved(), state.getQueued(), state.getSkippedIntentional(), state.getFailed()));
                log.info("[AI VOCAB BACKFILL] Job {} completed successfully", jobId);
            }

        } catch (Exception e) {
            log.error("[AI VOCAB BACKFILL] Job {} failed: {}", jobId, e.getMessage(), e);
            state.setStatus("FAILED");
            state.setLastError(e.getMessage());
            state.setMessage("Tiến trình gặp lỗi: " + e.getMessage());
        } finally {
            isBackfillRunning.set(false);
        }
    }

    private void processBatch(List<CefrWord> words, AiVocabTagBackfillStatusResponse state, double threshold) {
        List<AiVocabTagClassifyItemDto> classified = classifyBatchWithGemini(words);
        Map<String, AiVocabTagClassifyItemDto> classifiedMap = new HashMap<>();
        for (AiVocabTagClassifyItemDto item : classified) {
            if (item.getWord() != null) {
                classifiedMap.put(item.getWord().toLowerCase(), item);
            }
        }

        int batchAutoSaved = 0;
        int batchQueued = 0;
        int batchSkipped = 0;
        int batchFailed = 0;

        for (CefrWord word : words) {
            AiVocabTagClassifyItemDto dto = classifiedMap.get(word.getLemma().toLowerCase());
            WordTopic topic = dto != null ? WordTopic.fromString(dto.getTopic()) : null;
            Double conf = dto != null ? dto.getConfidence() : 0.0;
            String reason = dto != null ? dto.getReason() : null;

            boolean isAiError = reason != null && reason.startsWith("Lỗi phân loại AI");

            if (isAiError) {
                state.setFailed(state.getFailed() + 1);
                state.setLastError(reason);
                batchFailed++;
            } else if (topic != null && conf != null && conf >= threshold) {
                word.setTopic(topic);
                word.setUpdatedAt(LocalDateTime.now());
                cefrWordRepository.save(word);
                state.setAutoSaved(state.getAutoSaved() + 1);
                batchAutoSaved++;
            } else if (topic != null) {
                // Mơ hồ (có topic dự đoán nhưng confidence < threshold) -> Vào hàng chờ duyệt tay
                if (!topicTagQueueRepository.existsByCefrWordIdAndStatus(word.getId(), "PENDING")) {
                    TopicTagQueue q = TopicTagQueue.builder()
                            .cefrWordId(word.getId())
                            .lemma(word.getLemma())
                            .suggestedTopic(topic)
                            .confidence(conf != null ? conf : 0.0)
                            .reason(reason != null ? reason : "Độ tin cậy thấp")
                            .status("PENDING")
                            .build();
                    topicTagQueueRepository.save(q);
                }
                state.setQueued(state.getQueued() + 1);
                batchQueued++;
            } else {
                // Rule 2.2: topic == null (Từ chức năng HOẶC từ chung chung không thuộc 12 chủ đề)
                // -> BỎ QUA CHỦ ĐÍCH, KHÔNG VÀO HÀNG CHỜ!
                state.setSkipped(state.getSkipped() + 1);
                state.setSkippedIntentional(state.getSkippedIntentional() + 1);
                batchSkipped++;
            }
            state.setProcessed(state.getProcessed() + 1);
        }

        log.info("[AI VOCAB BACKFILL] Processed {} words -> AutoSaved: {}, Queued: {}, Skipped: {}, Failed: {}",
                words.size(), batchAutoSaved, batchQueued, batchSkipped, batchFailed);

        state.setMessage(String.format("Đã xử lý %d/%d từ (Tự lưu: %d, Chờ duyệt: %d, Bỏ qua chủ đích: %d, Lỗi: %d)",
                state.getProcessed(), state.getTotal(), state.getAutoSaved(), state.getQueued(), state.getSkippedIntentional(), state.getFailed()));
    }

    private void respectRateLimit() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public AiVocabTagBackfillStatusResponse stopTopicBackfill() {
        if (!isBackfillRunning.get()) {
            return getTopicBackfillStatus(null);
        }
        isBackfillRunning.set(false);
        AiVocabTagBackfillStatusResponse activeJob = backfillJobs.values().stream()
                .filter(j -> "RUNNING".equals(j.getStatus()))
                .findFirst()
                .orElse(null);
        if (activeJob != null) {
            activeJob.setStatus("STOPPED");
            activeJob.setMessage(String.format("Tiến trình đã được dừng bởi quản trị viên. Đã xử lý %d/%d từ.",
                    activeJob.getProcessed(), activeJob.getTotal()));
            return activeJob;
        }
        return getTopicBackfillStatus(null);
    }

    @Override
    public AiVocabTagBackfillStatusResponse getTopicBackfillStatus(String jobId) {
        if (jobId == null || jobId.trim().isEmpty() || "current".equalsIgnoreCase(jobId)) {
            return backfillJobs.values().stream()
                    .filter(j -> "RUNNING".equals(j.getStatus()))
                    .findFirst()
                    .orElseGet(() -> backfillJobs.values().stream()
                            .max(Comparator.comparing(AiVocabTagBackfillStatusResponse::getJobId, Comparator.nullsLast(String::compareTo)))
                            .orElse(AiVocabTagBackfillStatusResponse.builder()
                                    .jobId("none")
                                    .status("IDLE")
                                    .message("Chưa có tiến trình quét nào đang chạy.")
                                    .build()));
        }

        AiVocabTagBackfillStatusResponse st = backfillJobs.get(jobId);
        if (st == null) {
            return AiVocabTagBackfillStatusResponse.builder()
                    .jobId(jobId)
                    .status("IDLE")
                    .message("Không tìm thấy tiến trình với mã: " + jobId)
                    .build();
        }
        return st;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TopicTagQueueDto> getTopicTagQueue(String status, Pageable pageable) {
        String st = (status != null && !status.trim().isEmpty()) ? status.trim().toUpperCase() : "PENDING";
        Page<TopicTagQueue> page = topicTagQueueRepository.findByStatusOrderByCreatedAtDesc(st, pageable);
        return page.map(q -> TopicTagQueueDto.builder()
                .id(q.getId())
                .cefrWordId(q.getCefrWordId())
                .lemma(q.getLemma())
                .suggestedTopic(q.getSuggestedTopic() != null ? q.getSuggestedTopic().name() : null)
                .confidence(q.getConfidence())
                .reason(q.getReason())
                .status(q.getStatus())
                .createdAt(q.getCreatedAt())
                .build());
    }

    // ==================== Helpers & Prompt Builders ====================

    private String buildPrompt(String word, String levelHint) {
        return "You are an expert English lexicographer and bilingual tutor for Vietnamese English learners.\n" +
                "Generate a high-quality vocabulary dictionary entry for the English word: \"" + word + "\".\n" +
                (levelHint != null && !levelHint.isBlank() ? "Target CEFR level: " + levelHint + ".\n" : "") +
                "Requirements:\n" +
                "- pronunciation: Accurate IPA transcription with slashes, e.g. /ˈbjuːtɪfəl/\n" +
                "- partOfSpeech: Exactly one of NOUN, VERB, ADJECTIVE, ADVERB, PREPOSITION, CONJUNCTION\n" +
                "- meaning: Natural, concise Vietnamese meaning\n" +
                "- definition: Accurate, clear English definition\n" +
                "- example: A memorable, natural English sentence demonstrating the word\n" +
                "- exampleTranslation: Fluent Vietnamese translation of the example sentence\n" +
                "- level: Estimated CEFR level (A1, A2, B1, B2, or C1)\n" +
                "- synonyms: Array of 2-3 common synonyms\n" +
                "- antonyms: Array of 1-2 antonyms (or empty array if not applicable)\n\n" +
                "Output ONLY a raw, valid JSON object with NO markdown formatting, NO backticks, NO explanation text:\n" +
                "{\n" +
                "  \"word\": \"" + word + "\",\n" +
                "  \"pronunciation\": \"/.../\",\n" +
                "  \"partOfSpeech\": \"ADJECTIVE\",\n" +
                "  \"meaning\": \"...\",\n" +
                "  \"definition\": \"...\",\n" +
                "  \"example\": \"...\",\n" +
                "  \"exampleTranslation\": \"...\",\n" +
                "  \"level\": \"B1\",\n" +
                "  \"synonyms\": [\"...\"],\n" +
                "  \"antonyms\": [\"...\"]\n" +
                "}";
    }

    private String buildTopicClassificationPrompt(List<CefrWord> words) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert English lexicographer and bilingual tutor for Vietnamese English learners.\n");
        sb.append("Classify each English vocabulary entry into exactly ONE of the following 12 fixed topic codes:\n\n");
        sb.append("1. WORK (Công việc & Văn phòng: nghề nghiệp, kinh doanh, nơi làm việc, phỏng vấn)\n");
        sb.append("2. TRAVEL (Du lịch & Di chuyển: giao thông, phương tiện, khách sạn, điểm đến)\n");
        sb.append("3. ACADEMIC (Học thuật & Giáo dục: trường học, thi cử, đại học, nghiên cứu, khoa học)\n");
        sb.append("4. TECHNOLOGY (Công nghệ: máy tính, internet, phần mềm, thiết bị điện tử, AI)\n");
        sb.append("5. HEALTH (Sức khỏe: y tế, bệnh viện, cơ thể, dinh dưỡng, lối sống lành mạnh)\n");
        sb.append("6. FOOD (Ăn uống: món ăn, đồ uống, nhà hàng, nấu nướng, gia vị)\n");
        sb.append("7. MONEY (Mua sắm & Tiền bạc: tài chính, ngân hàng, chi tiêu, giá cả, mua bán)\n");
        sb.append("8. FAMILY (Gia đình & Bạn bè: các mối quan hệ, người thân, hôn nhân, tình cảm)\n");
        sb.append("9. SPORTS (Thể thao & Giải trí: trò chơi, sở thích, tập luyện, âm nhạc, phim ảnh)\n");
        sb.append("10. ENVIRONMENT (Môi trường: thiên nhiên, thời tiết, động vật, thực vật, sinh thái)\n");
        sb.append("11. SOCIETY (Xã hội & Pháp luật: chính quyền, luật pháp, cộng đồng, quyền con người)\n");
        sb.append("12. ARTS (Nghệ thuật & Văn hóa: văn hóa truyền thống, hội họa, kiến trúc, văn học)\n\n");
        sb.append("RULES:\n");
        sb.append("- MUST select strictly ONE of the 12 codes above, OR return topic: null.\n");
        sb.append("- Tag according to the stored Vietnamese meaning and English definition in the entry, NOT word form alone. (e.g. 'bank' as ngân hàng -> MONEY, 'bank' as bờ sông -> ENVIRONMENT).\n");
        sb.append("- Nếu từ chắc chắn không thuộc về 12 chủ đề nào (từ chung chung, trừu tượng, nghĩa quá rộng như 'difference', 'growth', 'importance', 'aspect', 'thing') thì trả topic: null, confidence: 0.0, reason: 'Không thuộc chủ đề nào'.\n");
        sb.append("- Chỉ trả null khi chắc chắn không thuộc chủ đề nào; nếu từ có thể thuộc 1–2 chủ đề nhưng không chắc thì vẫn trả topic nghiêng về nhất kèm confidence thấp (< 0.8) để vào hàng chờ duyệt tay.\n");
        sb.append("- confidence must be between 0.0 and 1.0.\n\n");
        sb.append("List of words to classify:\n");

        for (int i = 0; i < words.size(); i++) {
            CefrWord w = words.get(i);
            sb.append(i + 1).append(". Lemma: \"").append(w.getLemma()).append("\"\n");
            sb.append("   POS: ").append(w.getPartOfSpeech() != null ? w.getPartOfSpeech().name() : "N/A").append("\n");
            sb.append("   Meaning: ").append(w.getVietnameseMeaning() != null ? w.getVietnameseMeaning() : "N/A").append("\n");
            sb.append("   Definition: ").append(w.getDefinition() != null ? w.getDefinition() : "N/A").append("\n");
        }

        sb.append("\nOutput ONLY a raw JSON array of objects with NO markdown formatting, NO backticks, NO extra commentary:\n");
        sb.append("[\n");
        sb.append("  {\"word\": \"example\", \"topic\": \"WORK\", \"confidence\": 0.95, \"reason\": \"Giải thích ngắn\"}\n");
        sb.append("]\n");

        return sb.toString();
    }

    private List<AiVocabTagClassifyItemDto> parseGeminiTopicResponse(String rawJson, List<CefrWord> originalWords) {
        String cleanJson = rawJson.trim();
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        Map<String, CefrWord> wordMap = new HashMap<>();
        for (CefrWord w : originalWords) {
            wordMap.put(w.getLemma().toLowerCase(), w);
        }

        List<AiVocabTagClassifyItemDto> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(cleanJson);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    String lemma = node.path("word").asText("");
                    String topicStr = node.path("topic").asText(null);
                    double confidence = node.path("confidence").asDouble(0.0);
                    String reason = node.path("reason").asText("");

                    WordTopic validTopic = WordTopic.fromString(topicStr);
                    if (validTopic == null) {
                        topicStr = null;
                        confidence = 0.0;
                    }

                    CefrWord orig = wordMap.get(lemma.toLowerCase());
                    results.add(AiVocabTagClassifyItemDto.builder()
                            .existingWordId(orig != null ? orig.getId() : null)
                            .word(lemma)
                            .topic(validTopic != null ? validTopic.name() : null)
                            .confidence(confidence)
                            .reason(reason)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Gemini topic classification JSON: {}. Response: {}", e.getMessage(), cleanJson);
        }

        // If results didn't cover all original words, fill missing
        Set<String> processedLemmas = new HashSet<>();
        for (AiVocabTagClassifyItemDto dto : results) {
            processedLemmas.add(dto.getWord().toLowerCase());
        }
        for (CefrWord w : originalWords) {
            if (!processedLemmas.contains(w.getLemma().toLowerCase())) {
                results.add(AiVocabTagClassifyItemDto.builder()
                        .existingWordId(w.getId())
                        .word(w.getLemma())
                        .topic(null)
                        .confidence(0.0)
                        .reason("Không có phản hồi hợp lệ từ AI")
                        .build());
            }
        }

        return results;
    }

    private AiGeneratedWordDto parseGeminiResponse(String responseText, String fallbackWord) {
        String cleanJson = responseText.trim();
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        try {
            return objectMapper.readValue(cleanJson, AiGeneratedWordDto.class);
        } catch (Exception e) {
            log.warn("Failed to parse Gemini JSON: {}. Response was: {}", e.getMessage(), cleanJson);
            throw new RuntimeException("Could not parse AI response into vocabulary structure: " + e.getMessage());
        }
    }

    private AiGeneratedWordDto generateLocalFallback(String word, String levelHint, String existingId, boolean isDuplicate) {
        return AiGeneratedWordDto.builder()
                .existingWordId(existingId)
                .isDuplicate(isDuplicate)
                .word(word)
                .pronunciation("/" + word.toLowerCase() + "/")
                .partOfSpeech("NOUN")
                .meaning("Từ tiếng Anh " + word)
                .definition("The term referring to " + word)
                .example("This is an example sentence using the word " + word + ".")
                .exampleTranslation("Đây là câu ví dụ sử dụng từ " + word + ".")
                .level(levelHint != null ? levelHint : "B2")
                .synonyms(List.of(word))
                .antonyms(List.of())
                .build();
    }

    @Override
    public List<String> getDistinctWordSources() {
        return cefrWordRepository.findDistinctSources();
    }
}
