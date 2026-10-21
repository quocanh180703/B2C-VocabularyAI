package com.vocabularyai.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.admin.dto.request.AiVocabApproveRequest;
import com.vocabularyai.admin.dto.response.AiGeneratedWordDto;
import com.vocabularyai.admin.dto.response.AiMissingWordDto;
import com.vocabularyai.admin.dto.response.AiVocabAuditResponse;
import com.vocabularyai.admin.service.AdminAiVocabularyService;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.entity.PartOfSpeech;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAiVocabularyServiceImpl implements AdminAiVocabularyService {

    private final CefrWordRepository cefrWordRepository;
    private final WordRepository wordRepository;
    private final GeminiAIClientService geminiAIClientService;
    private final ObjectMapper objectMapper;

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

        // Completeness formula based on 4 essential fields: 4 * total items
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
                .pronunciation(cw.getPronunciation())
                .partOfSpeech(posStr)
                .level(lvlStr)
                .meaning(cw.getVietnameseMeaning())
                .definition(cw.getDefinition())
                .example(cw.getExample())
                .categoryName("CEFR " + lvlStr)
                .build();
    }

    @Override
    public AiGeneratedWordDto generateWordContent(String word, String levelHint) {
        String cleanWord = word != null ? word.trim() : "";
        if (cleanWord.isEmpty()) {
            throw new IllegalArgumentException("Word text cannot be empty");
        }

        // Check if word already exists in CEFR vocabulary
        Optional<CefrWord> existingOpt = cefrWordRepository.findByLemma(cleanWord.toLowerCase());
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
        // Cap at 10 words per batch for fast response
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

            // 1. Check by ID if known
            if (dto.getExistingWordId() != null && !dto.getExistingWordId().trim().isEmpty()) {
                cefrToSave = cefrWordRepository.findById(dto.getExistingWordId().trim()).orElse(null);
            }

            // 2. Check by lemma string
            if (cefrToSave == null) {
                cefrToSave = cefrWordRepository.findByLemma(cleanLemma).orElse(null);
            }

            if (cefrToSave != null) {
                if (request.isOverwriteExisting()) {
                    // Update existing CEFR word with enriched AI attributes
                    if (dto.getMeaning() != null && !dto.getMeaning().trim().isEmpty()) {
                        cefrToSave.setVietnameseMeaning(dto.getMeaning().trim());
                    }
                    if (dto.getPronunciation() != null && !dto.getPronunciation().trim().isEmpty()) {
                        cefrToSave.setPronunciation(dto.getPronunciation().trim());
                    }
                    if (dto.getPartOfSpeech() != null && !dto.getPartOfSpeech().trim().isEmpty()) {
                        cefrToSave.setPartOfSpeech(PartOfSpeech.fromString(dto.getPartOfSpeech().trim()));
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
                // Create brand new CEFR word entry
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

            // Sync to words table as well for multi-table consistency
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

        return savedCount;
    }

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
}
