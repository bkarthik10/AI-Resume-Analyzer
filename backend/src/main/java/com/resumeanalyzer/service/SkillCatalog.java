package com.resumeanalyzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.resumeanalyzer.config.DataFiles;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/** Skill dictionary (data/skills.json): canonical names, categories and aliases used for normalization. */
@Component
public class SkillCatalog {

    public record Entry(String name, String category, List<String> aliases) {}

    private final Map<String, Entry> byName = new LinkedHashMap<>();

    public SkillCatalog(DataFiles files) throws IOException {
        List<Entry> list = files.read("skills.json", new TypeReference<List<Entry>>() {});
        for (Entry e : list) {
            List<String> aliases = new ArrayList<>();
            aliases.add(e.name().toLowerCase(Locale.ROOT));
            for (String a : e.aliases()) aliases.add(a.toLowerCase(Locale.ROOT));
            byName.put(e.name().toLowerCase(Locale.ROOT), new Entry(e.name(), e.category(), aliases));
        }
    }

    public Collection<Entry> all() {
        return byName.values();
    }

    public Entry get(String name) {
        return byName.get(name.toLowerCase(Locale.ROOT));
    }

    public List<String> aliasesOf(String name) {
        Entry e = get(name);
        return e != null ? e.aliases() : List.of(name.toLowerCase(Locale.ROOT));
    }

    public String categoryOf(String name) {
        Entry e = get(name);
        return e != null ? e.category() : "General";
    }
}
