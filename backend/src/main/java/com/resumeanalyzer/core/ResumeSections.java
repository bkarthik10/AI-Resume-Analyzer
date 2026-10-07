package com.resumeanalyzer.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Splits resume text into labelled sections (Skills, Projects, Experience, ...). Values are lower-case. */
public final class ResumeSections {

    public static final String SKILLS = "Skills";
    public static final String PROJECTS = "Projects";
    public static final String EXPERIENCE = "Experience";
    public static final String EDUCATION = "Education";
    public static final String SUMMARY = "Summary";
    public static final String CERTIFICATIONS = "Certifications";
    public static final String OTHER = "Other";

    private static final Map<String, String> HEADINGS = new HashMap<>();

    static {
        add(SKILLS, "skills", "technical skills", "key skills", "core competencies", "technologies", "tech stack",
                "skills summary", "technical proficiency", "skills and tools", "skills tools", "technical expertise");
        add(PROJECTS, "projects", "project", "academic projects", "personal projects", "key projects",
                "project experience", "major projects");
        add(EXPERIENCE, "experience", "work experience", "professional experience", "internship", "internships",
                "internship experience", "employment history", "work history", "employment");
        add(EDUCATION, "education", "academic background", "educational qualifications", "education qualification",
                "academic qualifications", "academics");
        add(SUMMARY, "summary", "professional summary", "objective", "career objective", "profile",
                "about me", "career summary", "professional profile");
        add(CERTIFICATIONS, "certifications", "certificates", "certification", "courses", "licenses",
                "certifications and courses", "achievements");
    }

    private ResumeSections() {}

    private static void add(String label, String... names) {
        for (String n : names) HEADINGS.put(n, label);
    }

    private static String clean(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", " ").replaceAll("\\s+", " ").trim();
    }

    public static Map<String, String> split(String text) {
        Map<String, StringBuilder> map = new LinkedHashMap<>();
        String current = OTHER;
        if (text == null) return new LinkedHashMap<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String remainder = null;
            String label = null;
            if (trimmed.length() <= 40) label = HEADINGS.get(clean(trimmed));
            if (label == null) {
                int idx = trimmed.indexOf(':');
                if (idx > 0 && idx <= 30) {
                    label = HEADINGS.get(clean(trimmed.substring(0, idx)));
                    if (label != null) remainder = trimmed.substring(idx + 1);
                }
            }
            if (label != null) {
                current = label;
                map.computeIfAbsent(current, k -> new StringBuilder());
                if (remainder != null) map.get(current).append(remainder.toLowerCase(Locale.ROOT)).append('\n');
                continue;
            }
            map.computeIfAbsent(current, k -> new StringBuilder()).append(trimmed.toLowerCase(Locale.ROOT)).append('\n');
        }
        Map<String, String> out = new LinkedHashMap<>();
        map.forEach((k, v) -> out.put(k, v.toString()));
        return out;
    }
}
