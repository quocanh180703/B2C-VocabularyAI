package com.vocabularyai.document.entity;

public enum DocumentFileType {
    TXT,
    MD,
    PDF,
    DOCX,
    PPTX;

    public static DocumentFileType fromExtension(String ext) {
        if (ext == null) {
            throw new IllegalArgumentException("File extension cannot be null");
        }
        String clean = ext.replace(".", "").toUpperCase().trim();
        return valueOf(clean);
    }
}
