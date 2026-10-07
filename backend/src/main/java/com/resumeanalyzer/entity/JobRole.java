package com.resumeanalyzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_role")
public class JobRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String name;

    @Column(length = 1000)
    public String description;

    /** comma separated role keywords (non-skill terms such as "scalable", "unit testing") */
    @Column(length = 2000)
    public String keywords;
}
