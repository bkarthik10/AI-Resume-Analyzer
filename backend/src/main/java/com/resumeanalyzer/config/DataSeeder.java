package com.resumeanalyzer.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.resumeanalyzer.entity.JobRole;
import com.resumeanalyzer.entity.RoleSkill;
import com.resumeanalyzer.repository.JobRoleRepository;
import com.resumeanalyzer.repository.RoleSkillRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Loads data/job-roles.json into MySQL the first time the application starts. */
@Component
public class DataSeeder implements CommandLineRunner {

    public record SkillRef(String name, int weight) {}

    public record RoleDef(String name, String description, List<String> keywords, List<SkillRef> skills) {}

    private final DataFiles files;
    private final JobRoleRepository roles;
    private final RoleSkillRepository roleSkills;

    public DataSeeder(DataFiles files, JobRoleRepository roles, RoleSkillRepository roleSkills) {
        this.files = files;
        this.roles = roles;
        this.roleSkills = roleSkills;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roles.count() > 0) return;
        List<RoleDef> defs = files.read("job-roles.json", new TypeReference<List<RoleDef>>() {});
        for (RoleDef d : defs) {
            JobRole r = new JobRole();
            r.name = d.name();
            r.description = d.description();
            r.keywords = String.join(",", d.keywords());
            r = roles.save(r);
            for (SkillRef s : d.skills()) {
                RoleSkill rs = new RoleSkill();
                rs.roleId = r.id;
                rs.skillName = s.name();
                rs.weight = s.weight();
                roleSkills.save(rs);
            }
        }
    }
}
