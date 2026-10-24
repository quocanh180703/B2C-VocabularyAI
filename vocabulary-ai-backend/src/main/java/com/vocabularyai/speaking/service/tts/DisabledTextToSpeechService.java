package com.vocabularyai.speaking.service.tts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * No-op Text-to-Speech service.
 * Used when TTS is disabled or not needed.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "speaking.text-to-speech.provider", havingValue = "disabled", matchIfMissing = false)
public class DisabledTextToSpeechService implements TextToSpeechService {

    @Override
    public TextToSpeechResult synthesize(String text) {
        log.debug("TTS is disabled");
        return buildDisabledResult(text);
    }

    @Override
    public TextToSpeechResult synthesize(String text, String languageCode) {
        log.debug("TTS is disabled");
        return buildDisabledResult(text);
    }

    @Override
    public TextToSpeechResult synthesize(String text, String languageCode, String voice) {
        log.debug("TTS is disabled");
        return buildDisabledResult(text);
    }

    @Override
    public String getProviderType() {
        return "disabled";
    }

    @Override
    public boolean isHealthy() {
        return true; // Disabled service is always "healthy"
    }

    private TextToSpeechResult buildDisabledResult(String text) {
        return TextToSpeechResult.builder()
            .text(text)
            .audioUrl(null)
            .audioFormat(null)
            .durationSeconds(null)
            .voice(null)
            .languageCode(null)
            .provider("disabled")
            .processingTimeMs(0L)
            .success(false)
            .errorMessage("TTS service is disabled")
            .build();
    }
}
