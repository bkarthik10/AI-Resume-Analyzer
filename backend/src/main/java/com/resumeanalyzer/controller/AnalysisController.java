package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.AnalysisResult;
import com.resumeanalyzer.dto.SimpleDtos.CreateAnalysisRequest;
import com.resumeanalyzer.service.AnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public AnalysisResult create(@RequestBody CreateAnalysisRequest req) {
        return service.create(req.resumeId(), req.jobRoleId(), req.jobDescription());
    }

    @GetMapping("/{id}")
    public AnalysisResult get(@PathVariable Long id) {
        return service.get(id);
    }
}
