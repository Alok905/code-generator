package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

// it'll be helpful for tracking quota of the current subscription
// one user can interact with a single project multiple times, so here (userid, projectid) will not make a primary key

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class UsageLog {
    Long id;

    User user;

    Project project;

    String action; // may be creating, updating ..etc

    Integer tokensUsed;
    Integer durationMs;

    String metadata; // JSON of (model_udes, prompt_used}

    Instant createdAt;
}
