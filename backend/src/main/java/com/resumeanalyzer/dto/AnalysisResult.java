package com.resumeanalyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

/** JSON returned by POST /api/analysis/create and GET /api/analysis/{id}. */
public record AnalysisResult(
        Long id,
        ResumeInfo resume,
        RoleInfo role,
        String source,
        LocalDateTime analyzedAt,
        String status,
        int atsScore,
        int skillMatch,
        int matchedSkillsCount,
        int missingSkillsCount,
        int missingKeywordsCount,
        List<MatchedSkill> matchedSkills,
        List<MissingSkill> missingSkills,
        List<String> missingKeywords,
        List<Recommendation> recommendations,
        List<ProjectRec> projectRecommendations,
        List<LearningResource> learningResources,
        List<AtsItem> atsBreakdown) {

    public record ResumeInfo(String fileName, String fileType, long fileSize) {}

    public record RoleInfo(Long id, String name, String description) {}

    public record MatchedSkill(String skill, String category, String foundIn) {}

    public record MissingSkill(String skill, String priority, String reason) {}

    public record Recommendation(String title, String description) {}

    public record ProjectRec(String name, String description, List<String> skills, String difficulty) {}

    public record LearningResource(String skill, String resource, String provider, String link) {}

    public record AtsItem(String section, int score, int max) {}
}
