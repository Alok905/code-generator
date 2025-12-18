package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;


@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class User {

    @Id
    Long id;

    String email;

    String passwordHash;

    String name;

    String avatarUrl;

    Instant createdAt;
    Instant updatedAt;
    Instant deletedAt; // soft delete (database entry will not be deleted)

    @OneToMany(mappedBy = "user")
    List<Subscription> subscriptions;

    @OneToMany(mappedBy = "user")
    List<UsageLog> usageLogs;

    @ManyToMany
    @JoinTable(
            name = "user_prjoect",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    List<Project> projects;

    @OneToMany(mappedBy = "owner")
    List<Project> ownedProjects;
}
