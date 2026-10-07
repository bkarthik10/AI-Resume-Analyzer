package com.resumeanalyzer.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/** PDF / DOCX parser. */
@Component
public class TextExtractor {

    public String extract(byte[] data, String fileType) throws IOException {
        if ("PDF".equals(fileType)) {
            try (PDDocument doc = Loader.loadPDF(data)) {
                return new PDFTextStripper().getText(doc);
            }
        }
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(data));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }
}
