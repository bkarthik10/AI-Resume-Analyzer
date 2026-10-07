package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.AnalysisResult.Recommendation;
import com.resumeanalyzer.service.AnalysisService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final AnalysisService service;

    public RecommendationController(AnalysisService service) {
        this.service = service;
    }

    @GetMapping("/{analysisId}")
    public List<Recommendation> get(@PathVariable Long analysisId) {
        return service.get(analysisId).recommendations();
    }
}
