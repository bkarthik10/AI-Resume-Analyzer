package com.resumeanalyzer.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Deterministic ATS scoring. Weights: Contact 10, Sections 15, Keywords 30, Skills 25, Formatting 10, Alignment 10.
 * No random values anywhere.
 */
public final class AtsScorer {

    public record Item(String section, int score, int max) {}

    public record Result(List<Item> items, int total) {}

    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");
    private static final Pattern PHONE = Pattern.compile("\\+?\\d[\\d\\s().-]{8,}\\d");
    private static final Pattern LINKS = Pattern.compile("linkedin\\.com|github\\.com");
    private static final String BULLETS = "\u2022\u25CF\u25AA\u25A0\u25E6-*\u2013\u00B7";

    private AtsScorer() {}

    public static boolean hasEmail(String text) { return EMAIL.matcher(text).find(); }

    /** A phone number is a run of digits/separators containing 10 to 13 digits (so "2021 - 2025" is not one). */
    public static boolean hasPhone(String text) {
        java.util.regex.Matcher m = PHONE.matcher(text);
        while (m.find()) {
            int digits = m.group().replaceAll("\\D", "").length();
            if (digits >= 10 && digits <= 13) return true;
        }
        return false;
    }

    public static boolean hasLinks(String lower) { return LINKS.matcher(lower).find(); }

    public static int wordCount(String lower) {
        String t = lower.trim();
        return t.isEmpty() ? 0 : t.split("\\s+").length;
    }

    public static int bulletCount(String text) {
        int n = 0;
        for (String line : text.split("\\R")) {
            String t = line.trim();
            if (t.length() > 1 && BULLETS.indexOf(t.charAt(0)) >= 0) n++;
        }
        return n;
    }

    public static boolean hasSection(Map<String, String> sections, String label) {
        String v = sections.get(label);
        return v != null && !v.isBlank();
    }

    /**
     * @param keywordCoverage    0..1 share of target keywords/skills found in the resume
     * @param topWeightCoverage  0..1 share of the highest-priority target skills found in the resume
     */
    public static Result score(String text, String lower, Map<String, String> sections, int skillMatchPct,
                               double keywordCoverage, double topWeightCoverage, String roleName) {
        // 1. Contact information (10)
        int contact = (hasEmail(text) ? 4 : 0) + (hasPhone(text) ? 3 : 0) + (hasLinks(lower) ? 3 : 0);

        // 2. Resume sections (15)
        int secs = 0;
        if (hasSection(sections, ResumeSections.EDUCATION)) secs += 4;
        if (hasSection(sections, ResumeSections.SKILLS)) secs += 4;
        if (hasSection(sections, ResumeSections.PROJECTS)) secs += 3;
        if (hasSection(sections, ResumeSections.EXPERIENCE)) secs += 2;
        if (hasSection(sections, ResumeSections.SUMMARY)) secs += 1;
        if (hasSection(sections, ResumeSections.CERTIFICATIONS)) secs += 1;

        // 3. Keyword coverage (30)
        int keywords = (int) Math.round(clamp(keywordCoverage) * 30);

        // 4. Skill coverage (25)
        int skills = (int) Math.round(skillMatchPct / 100.0 * 25);

        // 5. Formatting / structure (10)
        int words = wordCount(lower);
        int bullets = bulletCount(text);
        int found = 0;
        for (String k : sections.keySet()) {
            if (!ResumeSections.OTHER.equals(k) && hasSection(sections, k)) found++;
        }
        int formatting = 0;
        formatting += (words >= 250 && words <= 1200) ? 4 : (words >= 150 && words <= 1500) ? 2 : 0;
        formatting += bullets >= 5 ? 3 : bullets >= 2 ? 1 : 0;
        formatting += found >= 4 ? 3 : found >= 2 ? 1 : 0;

        // 6. Job alignment (10): role title words in resume (4) + coverage of top-priority skills (6)
        Set<String> generic = Set.of("developer", "engineer");
        List<String> tokens = new ArrayList<>();
        for (String t : roleName.toLowerCase(Locale.ROOT).split("\\s+")) {
            if (!generic.contains(t) && t.length() > 1) tokens.add(t);
        }
        if (tokens.isEmpty()) {
            for (String t : roleName.toLowerCase(Locale.ROOT).split("\\s+")) tokens.add(t);
        }
        int hit = 0;
        for (String t : tokens) if (SkillMatcher.contains(lower, t)) hit++;
        double titleShare = tokens.isEmpty() ? 0 : (double) hit / tokens.size();
        int alignment = (int) Math.round(titleShare * 4) + (int) Math.round(clamp(topWeightCoverage) * 6);

        List<Item> items = List.of(
                new Item("Contact Information", Math.min(contact, 10), 10),
                new Item("Resume Sections", Math.min(secs, 15), 15),
                new Item("Keyword Coverage", Math.min(keywords, 30), 30),
                new Item("Skill Coverage", Math.min(skills, 25), 25),
                new Item("Formatting / Structure", Math.min(formatting, 10), 10),
                new Item("Job Alignment", Math.min(alignment, 10), 10));
        int total = 0;
        for (Item i : items) total += i.score();
        return new Result(items, total);
    }

    private static double clamp(double v) { return Math.max(0, Math.min(1, v)); }
}
