package com.vocabularyai.document;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.document.entity.DocumentFileType;
import com.vocabularyai.document.service.DocumentExtractorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DocumentExtractorUnitTest {

    private DocumentExtractorServiceImpl extractorService;

    @BeforeEach
    void setUp() {
        extractorService = new DocumentExtractorServiceImpl();
    }

    @Test
    @DisplayName("Trích xuất text từ file TXT thành công")
    void testExtractTxtSuccessfully() {
        String content = "Hello world! This is an English lesson about vocabulary.";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = extractorService.extractText(in, DocumentFileType.TXT);
        assertEquals(content, result);
    }

    @Test
    @DisplayName("Trích xuất text từ file MD thành công")
    void testExtractMdSuccessfully() {
        String content = "# Chapter 1\n\nEnglish grammar rules and examples.";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        String result = extractorService.extractText(in, DocumentFileType.MD);
        assertEquals(content, result);
    }

    @Test
    @DisplayName("Nội dung vượt quá 100k ký tự thì được cắt gọn")
    void testTruncateWhenExceeding100kChars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110_000; i++) {
            sb.append("A");
        }
        ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));

        String result = extractorService.extractText(in, DocumentFileType.TXT);
        assertTrue(result.length() <= 100_100);
        assertTrue(result.contains("cắt bớt do vượt quá 100,000 ký tự"));
    }

    @Test
    @DisplayName("Nội dung trống thì ném BadRequestException")
    void testEmptyContentThrowsBadRequest() {
        ByteArrayInputStream in = new ByteArrayInputStream("   ".getBytes(StandardCharsets.UTF_8));
        assertThrows(BadRequestException.class, () -> extractorService.extractText(in, DocumentFileType.TXT));
    }
}
