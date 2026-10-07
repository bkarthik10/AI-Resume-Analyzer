package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String fileName;
    public String fileType;
    public long fileSize;
    public String storedPath;

    @Column(columnDefinition = "LONGTEXT")
    public String extractedText;

    public LocalDateTime uploadedAt;
}
