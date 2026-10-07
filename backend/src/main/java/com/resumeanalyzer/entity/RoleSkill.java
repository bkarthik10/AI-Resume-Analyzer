package com.resumeanalyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role_skill")
public class RoleSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "role_id", nullable = false)
    public Long roleId;

    @Column(name = "skill_name", nullable = false)
    public String skillName;

    /** 3 = high priority, 2 = medium, 1 = low */
    public int weight;
}
