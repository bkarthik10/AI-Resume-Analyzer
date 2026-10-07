package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
