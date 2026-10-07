package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.SimpleDtos.ResumeDto;
import com.resumeanalyzer.service.ResumeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService service;

    public ResumeController(ResumeService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeDto upload(@RequestParam("file") MultipartFile file) {
        return service.upload(file);
    }
}
