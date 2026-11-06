package com.vocabularyai.document.service;

import com.vocabularyai.document.entity.DocumentFileType;

import java.io.InputStream;

public interface DocumentExtractorService {

    /**
     * Extracts text content from an uploaded document stream.
     * Maximum character count extracted is 100,000 characters.
     *
     * @param inputStream File input stream
     * @param fileType Supported document type (TXT, MD, PDF, DOCX)
     * @return Extracted raw text
     */
    String extractText(InputStream inputStream, DocumentFileType fileType);
}
