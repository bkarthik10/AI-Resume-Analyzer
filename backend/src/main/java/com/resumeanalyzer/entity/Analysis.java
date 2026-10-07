package com.resumeanalyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis")
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long resumeId;
    public Long jobRoleId;

    @Column(columnDefinition = "LONGTEXT")
    public String jobDescription;

    /** full analysis result serialized as JSON */
    @Column(columnDefinition = "LONGTEXT")
    public String resultJson;

    public LocalDateTime createdAt;
}
