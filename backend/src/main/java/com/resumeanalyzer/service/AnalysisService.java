package com.resumeanalyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.core.AtsScorer;
import com.resumeanalyzer.core.ResumeSections;
import com.resumeanalyzer.core.SkillMatcher;
import com.resumeanalyzer.dto.AnalysisResult;
import com.resumeanalyzer.dto.AnalysisResult.*;
import com.resumeanalyzer.entity.Analysis;
import com.resumeanalyzer.entity.JobRole;
import com.resumeanalyzer.entity.Resume;
import com.resumeanalyzer.entity.RoleSkill;
import com.resumeanalyzer.repository.AnalysisRepository;
import com.resumeanalyzer.repository.JobRoleRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.repository.RoleSkillRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pipeline:
 * parsed text -> skill extraction -> normalization (aliases)
 * -> role requirements -> skill matching
 * -> ATS calculation -> missing skills/keywords -> recommendations.
 */
@Service
public class AnalysisService {

    /*
     * IMPORTANT:
     * "ability" was previously present twice in this Set.
     * Set.of() does NOT allow duplicate elements.
     * The duplicate has been removed.
     */
    private static final Set<String> STOP = Set.of(
            "experience", "years", "should", "ability", "strong", "knowledge", "working", "skills", "ensure",
            "responsibilities", "requirements", "required", "preferred", "candidate", "looking", "using",
            "about", "across", "within", "including", "development", "develop", "understanding", "good",
            "excellent", "communication", "company", "position", "opportunity", "environment", "related",
            "ideal", "plus", "must", "have", "will", "team", "teams", "role", "work", "proven",
            "degree", "bachelor", "equivalent", "solutions", "building", "design", "designing", "applications",
            "application", "software", "engineer", "developer", "services", "systems", "business", "highly",
            "technical", "technologies", "tools", "hands", "etc.", "such", "other", "their", "while"
    );

    private final ResumeRepository resumes;
    private final JobRoleRepository roles;
    private final RoleSkillRepository roleSkills;
    private final AnalysisRepository analyses;
    private final SkillCatalog catalog;
    private final RecommendationService recs;
    private final ObjectMapper mapper;

    public AnalysisService(
            ResumeRepository resumes,
            JobRoleRepository roles,
            RoleSkillRepository roleSkills,
            AnalysisRepository analyses,
            SkillCatalog catalog,
            RecommendationService recs,
            ObjectMapper mapper
    ) {
        this.resumes = resumes;
        this.roles = roles;
        this.roleSkills = roleSkills;
        this.analyses = analyses;
        this.catalog = catalog;
        this.recs = recs;
        this.mapper = mapper;
    }

    private record Hit(String name, int weight) {
    }

    public AnalysisResult create(Long resumeId, Long roleId, String jobDescription) {

        if (resumeId == null || roleId == null) {
            throw new IllegalArgumentException(
                    "resumeId and jobRoleId are required."
            );
        }

        Resume resume = resumes.findById(resumeId)
                .orElseThrow(() ->
                        new NotFoundException("Resume not found: " + resumeId)
                );

        JobRole role = roles.findById(roleId)
                .orElseThrow(() ->
                        new NotFoundException("Job role not found: " + roleId)
                );

        String text = resume.extractedText == null
                ? ""
                : resume.extractedText;

        String lower = SkillMatcher.normalize(text);

        Map<String, String> sections = ResumeSections.split(text);

        // ------------------------------------------------------------
        // 1. TARGET REQUIREMENTS
        // ------------------------------------------------------------

        Map<String, Integer> target = new LinkedHashMap<>();

        String source = "Selected Role";

        List<String> jdTerms = List.of();

        String jdLower = SkillMatcher.normalize(jobDescription);

        if (!jdLower.isBlank()) {

            Map<String, Integer> fromJd = new LinkedHashMap<>();

            for (SkillCatalog.Entry e : catalog.all()) {

                int c = SkillMatcher.count(
                        jdLower,
                        e.aliases()
                );

                if (c > 0) {
                    fromJd.put(
                            e.name(),
                            c >= 2 ? 3 : 2
                    );
                }
            }

            if (fromJd.size() >= 3) {

                target = fromJd;

                source = "Job Description";

                jdTerms = jdTerms(jdLower);
            }
        }

        if (target.isEmpty()) {

            for (RoleSkill rs : roleSkills.findByRoleId(role.id)) {

                target.put(
                        rs.skillName,
                        rs.weight
                );
            }
        }

        // ------------------------------------------------------------
        // 2. SKILL MATCHING
        // ------------------------------------------------------------

        int maxWeight = 1;

        for (int w : target.values()) {
            maxWeight = Math.max(maxWeight, w);
        }

        int totalWeight = 0;
        int matchedWeight = 0;

        int topTotal = 0;
        int topMatched = 0;

        List<Hit> matchedHits = new ArrayList<>();
        List<Hit> missingHits = new ArrayList<>();

        Map<String, String> foundIn = new HashMap<>();

        for (Map.Entry<String, Integer> t : target.entrySet()) {

            String skill = t.getKey();

            int weight = t.getValue();

            List<String> aliases =
                    catalog.aliasesOf(skill);

            boolean found =
                    SkillMatcher.containsAny(
                            lower,
                            aliases
                    );

            totalWeight += weight;

            if (weight == maxWeight) {
                topTotal++;
            }

            if (found) {

                matchedWeight += weight;

                if (weight == maxWeight) {
                    topMatched++;
                }

                matchedHits.add(
                        new Hit(skill, weight)
                );

                List<String> places =
                        new ArrayList<>();

                for (Map.Entry<String, String> sec :
                        sections.entrySet()) {

                    if (!ResumeSections.OTHER.equals(sec.getKey())
                            && SkillMatcher.containsAny(
                                    sec.getValue(),
                                    aliases
                            )) {

                        places.add(sec.getKey());
                    }
                }

                if (places.isEmpty()) {
                    places.add("Resume");
                }

                foundIn.put(
                        skill,
                        String.join(", ", places)
                );

            } else {

                missingHits.add(
                        new Hit(skill, weight)
                );
            }
        }

        Comparator<Hit> byPriority = (a, b) -> {

            int c = Integer.compare(
                    b.weight(),
                    a.weight()
            );

            return c != 0
                    ? c
                    : a.name().compareTo(b.name());
        };

        matchedHits.sort(byPriority);
        missingHits.sort(byPriority);

        int skillMatch =
                totalWeight == 0
                        ? 0
                        : (int) Math.round(
                                matchedWeight * 100.0 / totalWeight
                        );

        // ------------------------------------------------------------
        // 3. KEYWORDS
        // ------------------------------------------------------------

        List<String> keywordPool =
                new ArrayList<>();

        if (role.keywords != null
                && !role.keywords.isBlank()) {

            for (String k :
                    role.keywords.split(",")) {

                keywordPool.add(
                        k.trim().toLowerCase(Locale.ROOT)
                );
            }
        }

        keywordPool.addAll(jdTerms);

        LinkedHashSet<String> missingKeywordSet =
                new LinkedHashSet<>();

        for (Hit h : missingHits) {
            missingKeywordSet.add(h.name());
        }

        int keywordsFound =
                matchedHits.size();

        for (String k : keywordPool) {

            if (SkillMatcher.contains(lower, k)) {

                keywordsFound++;

            } else {

                missingKeywordSet.add(k);
            }
        }

        int keywordTotal =
                target.size()
                        + keywordPool.size();

        double keywordCoverage =
                keywordTotal == 0
                        ? 0
                        : (double) keywordsFound
                        / keywordTotal;

        List<String> missingKeywords =
                new ArrayList<>(
                        missingKeywordSet
                );

        if (missingKeywords.size() > 20) {

            missingKeywords =
                    new ArrayList<>(
                            missingKeywords.subList(0, 20)
                    );
        }

        // ------------------------------------------------------------
        // 4. ATS SCORE
        // ------------------------------------------------------------

        double topCoverage =
                topTotal == 0
                        ? 0
                        : (double) topMatched
                        / topTotal;

        AtsScorer.Result ats =
                AtsScorer.score(
                        text,
                        lower,
                        sections,
                        skillMatch,
                        keywordCoverage,
                        topCoverage,
                        role.name
                );

        // ------------------------------------------------------------
        // 5. BUILD RESPONSE
        // ------------------------------------------------------------

        List<MatchedSkill> matched =
                new ArrayList<>();

        for (Hit h : matchedHits) {

            matched.add(
                    new MatchedSkill(
                            h.name(),
                            catalog.categoryOf(h.name()),
                            foundIn.get(h.name())
                    )
            );
        }

        List<MissingSkill> missing =
                new ArrayList<>();

        List<String> missingNames =
                new ArrayList<>();

        for (Hit h : missingHits) {

            String priority =
                    h.weight() >= 3
                            ? "High"
                            : h.weight() == 2
                            ? "Medium"
                            : "Low";

            missing.add(
                    new MissingSkill(
                            h.name(),
                            priority,
                            "Not detected in uploaded resume."
                    )
            );

            missingNames.add(h.name());
        }

        List<AtsItem> breakdown =
                new ArrayList<>();

        for (AtsScorer.Item i : ats.items()) {

            breakdown.add(
                    new AtsItem(
                            i.section(),
                            i.score(),
                            i.max()
                    )
            );
        }

        List<Recommendation> recommendations =
                recs.recommendations(
                        role.name,
                        missingNames,
                        text,
                        lower,
                        sections
                );

        List<ProjectRec> projectRecs =
                recs.projects(
                        missingNames,
                        target.keySet()
                );

        List<LearningResource> learning =
                recs.learningResources(
                        missingNames
                );

        // ------------------------------------------------------------
        // 6. SAVE ANALYSIS
        // ------------------------------------------------------------

        Analysis a = new Analysis();

        a.resumeId = resumeId;

        a.jobRoleId = roleId;

        a.jobDescription = jobDescription;

        a.resultJson = "{}";

        a.createdAt =
                LocalDateTime.now();

        a = analyses.save(a);

        AnalysisResult result =
                new AnalysisResult(
                        a.id,
                        new ResumeInfo(
                                resume.fileName,
                                resume.fileType,
                                resume.fileSize
                        ),
                        new RoleInfo(
                                role.id,
                                role.name,
                                role.description
                        ),
                        source,
                        a.createdAt,
                        "Completed",
                        ats.total(),
                        skillMatch,
                        matched.size(),
                        missing.size(),
                        missingKeywords.size(),
                        matched,
                        missing,
                        missingKeywords,
                        recommendations,
                        projectRecs,
                        learning,
                        breakdown
                );

        try {

            a.resultJson =
                    mapper.writeValueAsString(result);

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Could not store analysis result",
                    e
            );
        }

        analyses.save(a);

        return result;
    }

    // ------------------------------------------------------------
    // GET STORED ANALYSIS
    // ------------------------------------------------------------

    public AnalysisResult get(Long id) {

        Analysis a =
                analyses.findById(id)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Analysis not found: " + id
                                )
                        );

        try {

            return mapper.readValue(
                    a.resultJson,
                    AnalysisResult.class
            );

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "Stored analysis is unreadable",
                    e
            );
        }
    }

    // ------------------------------------------------------------
    // JOB DESCRIPTION TERMS
    // ------------------------------------------------------------

    /**
     * Frequent non-skill words in the job description
     * (appearing at least twice).
     */
    private List<String> jdTerms(String jdLower) {

        Set<String> known =
                new HashSet<>();

        for (SkillCatalog.Entry e :
                catalog.all()) {

            known.addAll(
                    e.aliases()
            );
        }

        Map<String, Integer> freq =
                new HashMap<>();

        Matcher m =
                Pattern.compile(
                        "[a-z][a-z+#.-]{4,}"
                ).matcher(jdLower);

        while (m.find()) {

            String w =
                    m.group()
                            .replaceAll(
                                    "[.-]+$",
                                    ""
                            );

            if (w.length() < 5
                    || STOP.contains(w)
                    || known.contains(w)) {

                continue;
            }

            freq.merge(
                    w,
                    1,
                    Integer::sum
            );
        }

        List<Map.Entry<String, Integer>> entries =
                new ArrayList<>();

        for (Map.Entry<String, Integer> e :
                freq.entrySet()) {

            if (e.getValue() >= 2) {
                entries.add(e);
            }
        }

        entries.sort((x, y) -> {

            int c =
                    Integer.compare(
                            y.getValue(),
                            x.getValue()
                    );

            return c != 0
                    ? c
                    : x.getKey().compareTo(
                            y.getKey()
                    );
        });

        List<String> out =
                new ArrayList<>();

        for (Map.Entry<String, Integer> e :
                entries) {

            if (out.size() >= 6) {
                break;
            }

            out.add(e.getKey());
        }

        return out;
    }
}