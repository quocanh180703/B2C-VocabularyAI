package com.vocabularyai.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validator for audio files.
 * Validates format, size, and duration of uploaded audio files.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AudioFileValidator.class)
@Documented
public @interface ValidAudioFile {

    String message() default "Audio file validation failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Maximum file size in bytes (default: 52MB = 52428800 bytes)
     */
    long maxSize() default 52428800;

    /**
     * Allowed MIME types
     */
    String[] allowedMimeTypes() default {
        "audio/wav",
        "audio/mpeg",
        "audio/mp4",
        "audio/webm",
        "audio/ogg",
        "audio/flac",
        "audio/x-wav",
        "audio/x-m4a"
    };

    /**
     * Allowed file extensions
     */
    String[] allowedExtensions() default {
        "wav", "mp3", "m4a", "webm", "ogg", "flac"
    };
}
