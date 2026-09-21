package com.vocabularyai.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator implementation for @ValidAudioFile annotation.
 * Validates audio files before they are processed.
 */
@Slf4j
public class AudioFileValidator implements ConstraintValidator<ValidAudioFile, MultipartFile> {

    private long maxSize;
    private Set<String> allowedMimeTypes;
    private Set<String> allowedExtensions;

    @Override
    public void initialize(ValidAudioFile constraintAnnotation) {
        maxSize = constraintAnnotation.maxSize();
        allowedMimeTypes = new HashSet<>(Arrays.asList(constraintAnnotation.allowedMimeTypes()));
        allowedExtensions = new HashSet<>(Arrays.asList(constraintAnnotation.allowedExtensions()));
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Allow null files (use @NotNull for required validation)
        if (file == null) {
            return true;
        }

        // Check if file is empty
        if (file.isEmpty()) {
            addConstraintViolation(context, "Audio file cannot be empty");
            return false;
        }

        // Validate file size
        if (file.getSize() > maxSize) {
            addConstraintViolation(context, 
                String.format("Audio file size (%d bytes) exceeds maximum limit (%d bytes)", 
                    file.getSize(), maxSize));
            return false;
        }

        // Validate MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !allowedMimeTypes.contains(mimeType.toLowerCase())) {
            addConstraintViolation(context, 
                String.format("Audio file MIME type '%s' is not allowed. Allowed types: %s", 
                    mimeType, String.join(", ", allowedMimeTypes)));
            return false;
        }

        // Validate file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            addConstraintViolation(context, "Audio file must have a valid extension");
            return false;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            addConstraintViolation(context, 
                String.format("Audio file extension '.%s' is not allowed. Allowed extensions: %s", 
                    extension, String.join(", ", allowedExtensions)));
            return false;
        }

        log.debug("Audio file validation passed: {}", filename);
        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
