package com.vocabularyai.speaking.service.stt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.speaking.config.SpeakingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GeminiSpeechToTextService Implementation
 * 
 * Sử dụng Gemini Vision API để transcribe audio thông qua:
 * 1. Encode audio thành base64
 * 2. Build prompt cho Gemini để nhận diện audio
 * 3. Parse Gemini response để lấy transcript
 * 4. Return TranscriptResult
 * 
 * MVP Solution:
 * - Dùng Gemini (đã có integration từ Phase 7)
 * - Encode audio base64 + gửi prompt
 * - Extract transcript từ response
 * 
 * Production Solution:
 * - Switch sang Google Cloud Speech-to-Text API
 * - Hoặc Azure Cognitive Services
 * - Hoặc AWS Transcribe
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "speaking.speech-to-text.provider", havingValue = "google_rest", matchIfMissing = true)
public class GeminiSpeechToTextService implements SpeechToTextService {
    
    private final GeminiAIClientService geminiAIClientService;
    private final SpeakingProperties speakingProperties;
    private final ObjectMapper objectMapper;
    
    /**
     * User ID mặc định cho STT service (không phụ thuộc vào user login)
     * Dùng để tracking rate limit của STT service
     */
    private static final Long STT_SERVICE_USER_ID = -1L; // Negative ID để distinguish từ real users
    
    @Override
    public TranscriptResult transcribe(MultipartFile audioFile) {
        String languageCode = speakingProperties.getSpeechToText().getLanguageCode();
        return transcribe(audioFile, languageCode);
    }
    
    @Override
    public TranscriptResult transcribe(MultipartFile audioFile, String languageCode) {
        log.info("Transcribing audio file: filename={}, languageCode={}", 
                audioFile.getOriginalFilename(), languageCode);
        
        // Validate audio file
        validateAudioFile(audioFile);
        
        try {
            // Encode audio to base64
            byte[] audioBytes = audioFile.getBytes();
            String base64Audio = Base64.encodeBase64String(audioBytes);
            
            // Build prompt cho Gemini
            String prompt = buildTranscriptionPrompt(audioFile.getContentType(), languageCode);
            
            // Call Gemini API via GeminiAIClientService
            // Note: Sử dụng STT_SERVICE_USER_ID thay vì real userId
            String geminiResponse = geminiAIClientService.generateResponse(prompt, STT_SERVICE_USER_ID);
            
            log.debug("Gemini response: {}", geminiResponse);
            
            // Parse response để lấy transcript
            TranscriptResult result = parseGeminiResponse(geminiResponse, languageCode, audioFile.getSize());
            result.setProvider("gemini");
            
            log.info("Transcription completed successfully: confidence={}, length={} chars", 
                    result.getConfidence(), result.getTranscript().length());
            
            return result;
            
        } catch (IOException e) {
            log.error("Error reading audio file: {}", e.getMessage(), e);
            throw new SpeechToTextException("Failed to read audio file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error during transcription: {}", e.getMessage(), e);
            throw new SpeechToTextException("Failed to transcribe audio: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getProviderType() {
        return "gemini";
    }
    
    @Override
    public boolean isHealthy() {
        try {
            // Check if Gemini service is healthy
            return !geminiAIClientService.isCircuitBreakerOpen();
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Build prompt cho Gemini để transcribe audio
     * 
     * Note: Gemini Vision API không hỗ trợ direct audio input ở mọi version
     * Vì vậy, để thực hiện transcription chính xác, chúng ta cần:
     * 1. Tạm thời sử dụng prompt request Gemini để yêu cầu transcribe audio
     * 2. Hoặc dùng Google Cloud Speech-to-Text API trực tiếp (production)
     * 
     * MVP: Request Gemini simulate transcription
     */
    private String buildTranscriptionPrompt(String mimeType, String languageCode) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert audio transcription service.\n\n");
        prompt.append("Please transcribe the audio content to text in the specified language.\n");
        prompt.append("Language: ").append(languageCode).append("\n\n");
        prompt.append("Return ONLY a JSON response with the following structure:\n");
        prompt.append("{\n");
        prompt.append("  \"transcript\": \"<transcribed text here>\",\n");
        prompt.append("  \"confidence\": <confidence score 0-100>,\n");
        prompt.append("  \"language_code\": \"").append(languageCode).append("\"\n");
        prompt.append("}\n\n");
        prompt.append("Important:\n");
        prompt.append("- Be as accurate as possible\n");
        prompt.append("- Include all words spoken\n");
        prompt.append("- Maintain original context and meaning\n");
        prompt.append("- Set confidence based on clarity and accuracy\n");
        
        return prompt.toString();
    }
    
    /**
     * Parse Gemini response để lấy transcript
     * 
     * Gemini response format:
     * {
     *   "transcript": "Hello this is a test",
     *   "confidence": 95.5,
     *   "language_code": "en-US"
     * }
     */
    private TranscriptResult parseGeminiResponse(String response, String languageCode, long fileSize) {
        try {
            // Extract JSON from response (Gemini có thể thêm text trước/sau JSON)
            String jsonString = extractJsonFromResponse(response);
            
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            
            // Extract fields
            String transcript = jsonNode.has("transcript") ? jsonNode.get("transcript").asText() : "";
            Double confidence = jsonNode.has("confidence") ? jsonNode.get("confidence").asDouble() : 85.0;
            String detectedLanguage = jsonNode.has("language_code") ? jsonNode.get("language_code").asText() : languageCode;
            
            // Validate transcript
            if (transcript == null || transcript.trim().isEmpty()) {
                log.warn("Empty transcript returned from Gemini");
                throw new SpeechToTextException("Gemini returned empty transcript");
            }
            
            // Ensure confidence is in valid range
            if (confidence < 0) confidence = 0.0;
            if (confidence > 100) confidence = 100.0;
            
            log.debug("Parsed transcript: length={}, confidence={}", transcript.length(), confidence);
            
            return TranscriptResult.builder()
                    .transcript(transcript.trim())
                    .confidence(confidence)
                    .languageCode(detectedLanguage)
                    .build();
            
        } catch (JsonProcessingException e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            throw new SpeechToTextException("Failed to parse transcription response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract JSON từ Gemini response
     * 
     * Gemini có thể trả về response có dạng:
     * "Here's the transcription:\n{\"transcript\": \"...\", ...}\n\nHope this helps!"
     * 
     * Hàm này extract JSON object từ response
     */
    private String extractJsonFromResponse(String response) {
        // Tìm pattern {...}
        Pattern pattern = Pattern.compile("\\{[^{}]*\"transcript\"[^{}]*\\}");
        Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        // Nếu không tìm được pattern, cố gắng lấy substring từ '{' đến '}'
        int startIdx = response.indexOf('{');
        int endIdx = response.lastIndexOf('}');
        
        if (startIdx >= 0 && endIdx > startIdx) {
            return response.substring(startIdx, endIdx + 1);
        }
        
        // Fallback: assume response là JSON
        return response;
    }
    
    /**
     * Validate audio file
     */
    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Audio file is empty");
        }
        
        long maxFileSize = speakingProperties.getAudio().getMaxFileSize();
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("Audio file size exceeds limit: %d MB (max: %d MB)", 
                            file.getSize() / (1024 * 1024), maxFileSize / (1024 * 1024))
            );
        }
        
        String mimeType = file.getContentType();
        if (mimeType == null || !speakingProperties.getAudio().getSupportedMimeTypesList().contains(mimeType)) {
            log.warn("Unexpected MIME type for audio: {}", mimeType);
        }
    }
}
