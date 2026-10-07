package com.resumeanalyzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.resumeanalyzer.config.DataFiles;
import com.resumeanalyzer.core.AtsScorer;
import com.resumeanalyzer.core.ResumeSections;
import com.resumeanalyzer.dto.AnalysisResult.LearningResource;
import com.resumeanalyzer.dto.AnalysisResult.ProjectRec;
import com.resumeanalyzer.dto.AnalysisResult.Recommendation;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/** Builds recommendations, project ideas and learning resources from the ACTUAL missing skills. */
@Service
public class RecommendationService {

    private final List<ProjectRec> projects;
    private final List<LearningResource> resources;
    private final SkillCatalog catalog;

    public RecommendationService(DataFiles files, SkillCatalog catalog) throws IOException {
        this.catalog = catalog;
        this.projects = files.read("projects.json", new TypeReference<List<ProjectRec>>() {});
        this.resources = files.read("resources.json", new TypeReference<List<LearningResource>>() {});
    }

    public List<Recommendation> recommendations(String roleName, List<String> missingSkills, String text,
                                                String lower, Map<String, String> sections) {
        List<Recommendation> out = new ArrayList<>();
        for (String skill : missingSkills) {
            if (out.size() >= 6) break;
            String category = catalog.categoryOf(skill);
            String desc;
            switch (category) {
                case "Cloud", "DevOps" ->
                        desc = "Learn " + skill + " basics and use it to build or deploy a small project for the "
                                + roleName + " role.";
                case "Database" ->
                        desc = "Practice " + skill + " by designing tables and writing queries in a project.";
                case "Messaging" ->
                        desc = "Learn " + skill + " and integrate it into a project that sends and receives messages.";
                default ->
                        desc = "Learn " + skill + " fundamentals and apply it in a project for the " + roleName + " role.";
            }
            out.add(new Recommendation("Learn " + skill, desc));
        }
        if (!AtsScorer.hasSection(sections, ResumeSections.PROJECTS)) {
            out.add(new Recommendation("Add a Projects section",
                    "List 2-3 projects with the technologies used and what you built."));
        }
        if (!AtsScorer.hasSection(sections, ResumeSections.SUMMARY)) {
            out.add(new Recommendation("Add a professional summary",
                    "Write 2-3 lines stating your target role and strongest skills."));
        }
        if (!AtsScorer.hasLinks(lower)) {
            out.add(new Recommendation("Add LinkedIn and GitHub links",
                    "Recruiters and ATS parsers look for profile links in the contact section."));
        }
        if (!AtsScorer.hasEmail(text) || !AtsScorer.hasPhone(text)) {
            out.add(new Recommendation("Complete your contact details",
                    "Include a professional email address and a phone number at the top."));
        }
        if (AtsScorer.bulletCount(text) < 5) {
            out.add(new Recommendation("Use bullet points",
                    "Describe projects and experience in short bullet points that start with action verbs."));
        }
        if (AtsScorer.wordCount(lower) < 250) {
            out.add(new Recommendation("Improve project descriptions",
                    "Add more detail: what you built, the technologies used and the result."));
        }
        return out.size() > 10 ? out.subList(0, 10) : out;
    }

    public List<ProjectRec> projects(List<String> missing, Collection<String> targetSkills) {
        Set<String> miss = lowerSet(missing);
        Set<String> target = lowerSet(targetSkills);
        record Scored(ProjectRec project, int missHits, int targetHits) {}
        List<Scored> scored = new ArrayList<>();
        for (ProjectRec p : projects) {
            int m = 0, t = 0;
            for (String s : p.skills()) {
                if (miss.contains(s.toLowerCase(Locale.ROOT))) m++;
                if (target.contains(s.toLowerCase(Locale.ROOT))) t++;
            }
            if (m > 0 || (miss.isEmpty() && t > 0)) scored.add(new Scored(p, m, t));
        }
        scored.sort((a, b) -> {
            int c = Integer.compare(b.missHits(), a.missHits());
            if (c != 0) return c;
            c = Integer.compare(b.targetHits(), a.targetHits());
            return c != 0 ? c : a.project().name().compareTo(b.project().name());
        });
        List<ProjectRec> out = new ArrayList<>();
        for (Scored s : scored) {
            if (out.size() >= 3) break;
            out.add(s.project());
        }
        return out;
    }

    public List<LearningResource> learningResources(List<String> missing) {
        List<LearningResource> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String skill : missing) {
            if (out.size() >= 8) break;
            for (LearningResource r : resources) {
                if (r.skill().equalsIgnoreCase(skill) && seen.add(r.skill())) {
                    out.add(r);
                    break;
                }
            }
        }
        return out;
    }

    private static Set<String> lowerSet(Collection<String> in) {
        Set<String> s = new HashSet<>();
        for (String x : in) s.add(x.toLowerCase(Locale.ROOT));
        return s;
    }
}
