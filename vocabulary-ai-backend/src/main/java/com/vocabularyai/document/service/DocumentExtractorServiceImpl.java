package com.vocabularyai.document.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.document.entity.DocumentFileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class DocumentExtractorServiceImpl implements DocumentExtractorService {

    private static final int MAX_EXTRACTED_CHARS = 100_000;

    @Override
    public String extractText(InputStream inputStream, DocumentFileType fileType) {
        if (inputStream == null || fileType == null) {
            throw new BadRequestException("Tập tin hoặc định dạng không hợp lệ");
        }

        try {
            String rawText;
            switch (fileType) {
                case TXT:
                case MD:
                    rawText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    break;
                case PDF:
                    try (PDDocument document = PDDocument.load(inputStream)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        rawText = stripper.getText(document);
                    }
                    break;
                case DOCX:
                    try (XWPFDocument document = new XWPFDocument(inputStream);
                         XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                        rawText = extractor.getText();
                    }
                    break;
                default:
                    throw new BadRequestException("Định dạng tập tin " + fileType + " chưa được hỗ trợ trích xuất.");
            }

            if (rawText == null || rawText.isBlank()) {
                throw new BadRequestException("Không thể trích xuất nội dung văn bản từ tập tin (văn bản trống hoặc là file ảnh scan).");
            }

            rawText = rawText.trim();
            if (rawText.length() > MAX_EXTRACTED_CHARS) {
                log.info("Document text length ({}) exceeded {}, truncating...", rawText.length(), MAX_EXTRACTED_CHARS);
                rawText = rawText.substring(0, MAX_EXTRACTED_CHARS) + "\n...[Nội dung tài liệu đã được cắt bớt do vượt quá 100,000 ký tự]...";
            }

            return rawText;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract text from document of type {}", fileType, e);
            throw new BadRequestException("Lỗi khi đọc tài liệu: " + e.getMessage());
        }
    }
}
