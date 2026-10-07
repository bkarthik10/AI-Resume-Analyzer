package com.resumeanalyzer.core;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Whole-word matching of skills/keywords inside resume text (so "java" does not match "javascript"). */
public final class SkillMatcher {

    private static final Map<String, Pattern> CACHE = new ConcurrentHashMap<>();

    private SkillMatcher() {}

    public static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).replace('\u00a0', ' ');
    }

    private static Pattern pattern(String term) {
        return CACHE.computeIfAbsent(term,
                t -> Pattern.compile("(?<![a-z0-9+#])" + Pattern.quote(t) + "(?![a-z0-9+#])"));
    }

    public static boolean contains(String lowerText, String term) {
        return pattern(term.toLowerCase(Locale.ROOT)).matcher(lowerText).find();
    }

    public static boolean containsAny(String lowerText, Collection<String> terms) {
        for (String t : terms) {
            if (contains(lowerText, t)) return true;
        }
        return false;
    }

    public static int count(String lowerText, Collection<String> terms) {
        int total = 0;
        for (String t : terms) {
            Matcher m = pattern(t.toLowerCase(Locale.ROOT)).matcher(lowerText);
            while (m.find()) total++;
        }
        return total;
    }
}
