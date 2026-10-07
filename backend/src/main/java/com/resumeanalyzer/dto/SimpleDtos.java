package com.resumeanalyzer.dto;

public final class SimpleDtos {
    private SimpleDtos() {}

    public record JobRoleDto(Long id, String name, String description) {}

    public record RoleSkillDto(String skill, String category, String priority, int weight) {}

    public record ResumeDto(Long id, String fileName, String fileType, long fileSize) {}

    public record CreateAnalysisRequest(Long resumeId, Long jobRoleId, String jobDescription) {}
}
