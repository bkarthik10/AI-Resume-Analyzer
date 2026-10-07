package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.SimpleDtos.JobRoleDto;
import com.resumeanalyzer.dto.SimpleDtos.RoleSkillDto;
import com.resumeanalyzer.entity.JobRole;
import com.resumeanalyzer.entity.RoleSkill;
import com.resumeanalyzer.repository.JobRoleRepository;
import com.resumeanalyzer.repository.RoleSkillRepository;
import com.resumeanalyzer.service.NotFoundException;
import com.resumeanalyzer.service.SkillCatalog;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/job-roles")
public class JobRoleController {

    private final JobRoleRepository roles;
    private final RoleSkillRepository roleSkills;
    private final SkillCatalog catalog;

    public JobRoleController(JobRoleRepository roles, RoleSkillRepository roleSkills, SkillCatalog catalog) {
        this.roles = roles;
        this.roleSkills = roleSkills;
        this.catalog = catalog;
    }

    @GetMapping
    public List<JobRoleDto> list() {
        List<JobRoleDto> out = new ArrayList<>();
        for (JobRole r : roles.findAll()) out.add(new JobRoleDto(r.id, r.name, r.description));
        return out;
    }

    @GetMapping("/{id}")
    public JobRoleDto get(@PathVariable Long id) {
        JobRole r = roles.findById(id).orElseThrow(() -> new NotFoundException("Job role not found: " + id));
        return new JobRoleDto(r.id, r.name, r.description);
    }

    @GetMapping("/{id}/skills")
    public List<RoleSkillDto> skills(@PathVariable Long id) {
        if (!roles.existsById(id)) throw new NotFoundException("Job role not found: " + id);
        List<RoleSkillDto> out = new ArrayList<>();
        for (RoleSkill s : roleSkills.findByRoleId(id)) {
            String priority = s.weight >= 3 ? "High" : s.weight == 2 ? "Medium" : "Low";
            out.add(new RoleSkillDto(s.skillName, catalog.categoryOf(s.skillName), priority, s.weight));
        }
        out.sort(Comparator.comparingInt(RoleSkillDto::weight).reversed().thenComparing(RoleSkillDto::skill));
        return out;
    }
}
