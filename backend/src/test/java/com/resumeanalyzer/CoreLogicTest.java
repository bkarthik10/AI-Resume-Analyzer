package com.resumeanalyzer;

import com.resumeanalyzer.core.AtsScorer;
import com.resumeanalyzer.core.ResumeSections;
import com.resumeanalyzer.core.SkillMatcher;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CoreLogicTest {

    private static final String RESUME = String.join("\n",
            "Karthik R", "karthik@example.com | +91 98765 43210 | github.com/karthik",
            "Summary", "Java developer.", "Skills", "Java, Spring Boot, MySQL, Git, JavaScript",
            "Projects", "\u2022 Built a REST API using Spring Boot", "Education", "B.Tech 2021-2025");

    @Test
    void wholeWordMatchingOnly() {
        String l = SkillMatcher.normalize("I know JavaScript and MySQL");
        assertTrue(SkillMatcher.contains(l, "javascript"));
        assertFalse(SkillMatcher.contains(l, "java"));
        assertFalse(SkillMatcher.contains(l, "sql"));
        assertFalse(SkillMatcher.contains(l, "docker"));
    }

    @Test
    void sectionsAreDetected() {
        Map<String, String> s = ResumeSections.split(RESUME);
        assertTrue(AtsScorer.hasSection(s, ResumeSections.SKILLS));
        assertTrue(AtsScorer.hasSection(s, ResumeSections.PROJECTS));
        assertTrue(s.get(ResumeSections.SKILLS).contains("spring boot"));
    }

    @Test
    void phoneDetectionIgnoresYearRanges() {
        assertTrue(AtsScorer.hasPhone("+91 98765 43210"));
        assertFalse(AtsScorer.hasPhone("2021 - 2025"));
    }

    @Test
    void atsScoreIsDeterministicAndBoundedByWeights() {
        String l = SkillMatcher.normalize(RESUME);
        Map<String, String> s = ResumeSections.split(RESUME);
        AtsScorer.Result a = AtsScorer.score(RESUME, l, s, 60, 0.5, 0.5, "Java Backend Developer");
        AtsScorer.Result b = AtsScorer.score(RESUME, l, s, 60, 0.5, 0.5, "Java Backend Developer");
        assertEquals(a.total(), b.total());
        assertTrue(a.total() >= 0 && a.total() <= 100);
        assertEquals(100, a.items().stream().mapToInt(AtsScorer.Item::max).sum());
    }
}
