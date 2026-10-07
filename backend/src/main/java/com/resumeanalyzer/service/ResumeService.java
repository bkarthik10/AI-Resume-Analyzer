package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.SimpleDtos.ResumeDto;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class ResumeService {

    private final ResumeRepository resumes;
    private final TextExtractor extractor;
    private final Path uploadDir;

    public ResumeService(ResumeRepository resumes, TextExtractor extractor,
                         @Value("${app.upload-dir:../uploads}") String uploadDir) {
        this.resumes = resumes;
        this.extractor = extractor;
        this.uploadDir = Path.of(uploadDir);
    }

    public ResumeDto upload(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Please choose a resume file.");
        String name = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String lower = name.toLowerCase(Locale.ROOT);
        String type;
        if (lower.endsWith(".pdf")) type = "PDF";
        else if (lower.endsWith(".docx")) type = "DOCX";
        else throw new IllegalArgumentException("Only PDF or DOCX resumes are supported.");

        try {
            byte[] data = file.getBytes();
            String text = extractor.extract(data, type);
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException(
                        "No text could be extracted. Scanned/image-only resumes are not supported.");
            }
            Files.createDirectories(uploadDir);
            Path stored = uploadDir.resolve(UUID.randomUUID() + "." + type.toLowerCase(Locale.ROOT));
            Files.write(stored, data);

            Resume r = new Resume();
            r.fileName = name;
            r.fileType = type;
            r.fileSize = file.getSize();
            r.storedPath = stored.toString();
            r.extractedText = text;
            r.uploadedAt = LocalDateTime.now();
            r = resumes.save(r);
            return new ResumeDto(r.id, r.fileName, r.fileType, r.fileSize);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded file: " + e.getMessage());
        }
    }
}
