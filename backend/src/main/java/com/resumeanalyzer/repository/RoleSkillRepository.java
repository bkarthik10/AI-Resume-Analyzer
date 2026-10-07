package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.RoleSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleSkillRepository extends JpaRepository<RoleSkill, Long> {
    List<RoleSkill> findByRoleId(Long roleId);
}
